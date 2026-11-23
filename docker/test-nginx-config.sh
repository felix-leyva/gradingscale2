#!/usr/bin/env bash
#
# Reproduction / regression harness for docker/nginx.conf.
#
# Spins up the real nginx:alpine image with the given config and a fixture
# WasmJS distribution (index.html, composeApp.js, skiko.js, firebase-config.js,
# styles.css, skiko.wasm, icon.svg) and verifies:
#   A8-01  exactly one Content-Type header per response, correct MIME values
#   A8-02  security headers on every location (incl. assets, index.html, 404s)
#   A8-03  no unsafe caching for unhashed entry files; no SPA/HTML fallback
#          for missing static assets
#
# Usage:
#   docker/test-nginx-config.sh [path-to-nginx.conf]
#
# Environment:
#   PORT            host port to bind (default: ephemeral)
#   LISTEN_ADDR     address nginx binds inside the container (default: 127.0.0.1,
#                   so the fixture is not exposed off-box)
#   NGINX_IMAGE     image to use (default: nginx:alpine)
#   PODMAN_STORE    reuse this rootless podman store instead of a private mktemp
#                   one (a private store is created and removed per run when
#                   docker is down)
#
# Exit code: 0 = all checks pass, 1 = at least one check failed, 2 = setup error.
#
set -uo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONF="${1:-$SCRIPT_DIR/nginx.conf}"
PORT="${PORT:-}"
LISTEN_ADDR="${LISTEN_ADDR:-127.0.0.1}"

if [[ ! -f "$CONF" ]]; then
  echo "config not found: $CONF" >&2
  exit 2
fi

# --- container runtime -------------------------------------------------------
# Prefer a working docker daemon; otherwise fall back to rootless podman with a
# private vfs store (hosts where the daemon is not running / overlayfs is
# unavailable).
if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
  RT=(docker)
  IMAGE="${NGINX_IMAGE:-nginx:alpine}"
elif command -v podman >/dev/null 2>&1; then
  # Private per-run store: a shared, predictable path lets concurrent runs
  # collide or reuse another run's (possibly corrupt) storage state.
  OWN_PODMAN_STORE=0
  if [[ -z "${PODMAN_STORE:-}" ]]; then
    PODMAN_STORE="$(mktemp -d "${TMPDIR:-/tmp}/nginx-repro-podman.XXXXXX")" || exit 2
    OWN_PODMAN_STORE=1
  fi
  mkdir -p "$PODMAN_STORE/root" "$PODMAN_STORE/run"
  RT=(podman --root "$PODMAN_STORE/root" --runroot "$PODMAN_STORE/run" --storage-driver=vfs)
  IMAGE="${NGINX_IMAGE:-docker.io/library/nginx:alpine}"
else
  echo "no usable container runtime (docker daemon down and podman missing)" >&2
  exit 2
fi

WORK="$(mktemp -d "${TMPDIR:-/tmp}/nginx-repro.XXXXXX")"
NAME="nginx-repro-$$"
cleanup() {
  "${RT[@]}" rm -f "$NAME" >/dev/null 2>&1 || true
  rm -rf "$WORK"
  if [[ "${OWN_PODMAN_STORE:-0}" == 1 ]]; then
    # Userns-owned files cannot be removed by plain rm; delete their contents
    # as userns root, then drop the (user-owned) shell directory.
    "${RT[@]}" unshare rm -rf "$PODMAN_STORE"/root/* "$PODMAN_STORE"/run/* >/dev/null 2>&1 || true
    rm -rf "$PODMAN_STORE" >/dev/null 2>&1 || true
  fi
}
trap cleanup EXIT

# --- fixtures ----------------------------------------------------------------
SITE="$WORK/site"
mkdir -p "$SITE/assets" "$SITE/fonts"

cat > "$SITE/index.html" <<'HTML'
<!DOCTYPE html>
<html lang="en">
<head><meta charset="UTF-8"><title>GradingScale2 repro</title></head>
<body>
  <canvas id="ComposeTarget"></canvas>
  <script src="skiko.js"></script>
  <script src="composeApp.js"></script>
</body>
</html>
HTML

printf 'console.log("composeApp");\n' > "$SITE/composeApp.js"
printf 'console.log("skiko");\n'          > "$SITE/skiko.js"
printf 'window.firebaseConfig = {};\n'    > "$SITE/firebase-config.js"
printf 'body { color: #000; }\n'          > "$SITE/styles.css"
printf '\x00asm\x01\x00\x00\x00'          > "$SITE/skiko.wasm"
printf '<svg xmlns="http://www.w3.org/2000/svg"/>\n' > "$SITE/icon.svg"
printf 'wOF2fixture'                      > "$SITE/fonts/app.woff2"
chmod -R a+rX "$WORK"

if [[ -z "$PORT" ]]; then
  PORT="$(python3 -c 'import socket; s=socket.socket(); s.bind(("127.0.0.1",0)); print(s.getsockname()[1]); s.close()')"
fi

# The production config listens on 80; rootless containers cannot bind
# privileged ports, so rewrite only the listen port for the harness and bind
# it to host loopback so the fixture server is never reachable off-box.
sed -E "s/^([[:space:]]*listen[[:space:]]+)(\[::\]:)?[0-9]+([[:space:]]*;)/\1${LISTEN_ADDR}:${PORT}\3/" \
  "$CONF" > "$WORK/default.conf"
if ! grep -q "listen[[:space:]]*${LISTEN_ADDR}:${PORT}" "$WORK/default.conf"; then
  echo "could not rewrite listen port in $CONF" >&2
  exit 2
fi

echo "== nginx -t (image: $IMAGE)"
if ! "${RT[@]}" run --rm --network=none \
      -v "$WORK/default.conf:/etc/nginx/conf.d/default.conf:ro" \
      -v "$SITE:/usr/share/nginx/html:ro" \
      "$IMAGE" nginx -t; then
  echo "[FAIL] nginx -t rejected the configuration" >&2
  exit 1
fi

echo "== starting server on ${LISTEN_ADDR}:$PORT"
# Host networking is required where rootless pasta cannot get /dev/net/tun, so
# port publishing is not an option. The container therefore joins the host
# network, but nginx itself binds the loopback address only (see LISTEN_ADDR
# rewrite above), so the fixture is not reachable from other interfaces.
CID="$("${RT[@]}" run -d --rm --name "$NAME" --network=host \
      -v "$WORK/default.conf:/etc/nginx/conf.d/default.conf:ro" \
      -v "$SITE:/usr/share/nginx/html:ro" \
      "$IMAGE")" || { echo "failed to start container" >&2; exit 2; }

ready=0
for _ in $(seq 1 50); do
  if curl -fsS -o /dev/null --max-time 1 "http://127.0.0.1:$PORT/index.html"; then
    ready=1; break
  fi
  sleep 0.2
done
if [[ "$ready" != 1 ]]; then
  echo "server did not become ready" >&2
  "${RT[@]}" logs "$CID" >&2 || true
  exit 2
fi

# --- helpers -----------------------------------------------------------------
FAILS=0
CHECKS=0

pass() { CHECKS=$((CHECKS + 1)); printf '[PASS] %s\n' "$*"; }
fail() { CHECKS=$((CHECKS + 1)); FAILS=$((FAILS + 1)); printf '[FAIL] %s\n' "$*"; }

HDR="$WORK/hdr"
BODY="$WORK/body"
STATUS=""
request() { # path
  STATUS="$(curl -sS --max-time 5 -o "$BODY" -D "$HDR" -w '%{http_code}' \
              "http://127.0.0.1:$PORT$1" 2>/dev/null)" || STATUS=000
}

header_values() { # header-name  (case-insensitive, preserves duplicates)
  awk -v want="$(printf '%s' "$1" | tr '[:upper:]' '[:lower:]')" '
    { line=$0; sub(/\r$/, "", line)
      p=index(tolower(line), want ":")
      if (p==1) { v=substr(line, length(want)+2); sub(/^[ \t]+/, "", v); print v } }' "$HDR"
}

header_count() { header_values "$1" | grep -c . ; }

contains_ci() { printf '%s' "$1" | grep -qi -- "$2"; }

expect_single_content_type() { # path expected-regex
  local path="$1" want="$2" n ct
  request "$path"
  n="$(header_count 'Content-Type')"
  ct="$(header_values 'Content-Type' | paste -sd' | ' -)"
  if [[ "$STATUS" != 200 ]]; then
    fail "A8-01 $path: expected 200, got $STATUS"
    return
  fi
  if [[ "$n" != 1 ]]; then
    fail "A8-01 $path: expected exactly 1 Content-Type header, got $n ($ct)"
  elif ! printf '%s' "$ct" | grep -Eq "$want"; then
    fail "A8-01 $path: expected Content-Type matching '$want', got '$ct'"
  else
    pass "A8-01 $path: single Content-Type '$ct'"
  fi
}

expect_single_cache_control() { # path
  local path="$1" n cc
  request "$path"
  n="$(header_count 'Cache-Control')"
  cc="$(header_values 'Cache-Control' | paste -sd' | ' -)"
  if [[ "$n" != 1 ]]; then
    fail "A8-03 $path: expected exactly 1 Cache-Control header, got $n ($cc)"
  else
    pass "A8-03 $path: single Cache-Control '$cc'"
  fi
}

expect_revalidate_cache() { # path description
  local path="$1" label="$2" cc
  request "$path"
  cc="$(header_values 'Cache-Control' | paste -sd', ' -)"
  if [[ "$STATUS" != 200 ]]; then
    fail "A8-03 $label: expected 200, got $STATUS"
  elif ! contains_ci "$cc" 'no-cache\|no-store\|must-revalidate\|max-age=0'; then
    fail "A8-03 $label: expected a revalidating Cache-Control, got '${cc:-<none>}'"
  elif contains_ci "$cc" 'immutable\|max-age=31536000\|max-age=3600'; then
    fail "A8-03 $label: unsafe long-lived Cache-Control '${cc}' for an unhashed file"
  else
    pass "A8-03 $label: revalidating Cache-Control '$cc'"
  fi
}

expect_404() { # path
  local path="$1" ct
  request "$path"
  ct="$(header_values 'Content-Type' | paste -sd' | ' -)"
  if [[ "$STATUS" != 404 ]]; then
    fail "A8-03 $path: expected 404 for a missing asset, got $STATUS (Content-Type: ${ct:-<none>})"
  else
    pass "A8-03 $path: missing asset returns 404"
  fi
}

expect_exact_cache_control() { # path expected-value
  local path="$1" want="$2" n cc
  request "$path"
  n="$(header_count 'Cache-Control')"
  cc="$(header_values 'Cache-Control' | paste -sd' | ' -)"
  if [[ "$STATUS" != 200 ]]; then
    fail "A8-03 $path: expected 200, got $STATUS"
  elif [[ "$n" != 1 ]]; then
    fail "A8-03 $path: expected exactly 1 Cache-Control header, got $n ($cc)"
  elif [[ "$cc" != "$want" ]]; then
    fail "A8-03 $path: expected exact Cache-Control '$want', got '$cc'"
  else
    pass "A8-03 $path: exact Cache-Control '$cc'"
  fi
}

expect_spa_shell() { # path description
  local path="$1" label="${2:-$1}" ct
  request "$path"
  ct="$(header_values 'Content-Type' | paste -sd' | ' -)"
  if [[ "$STATUS" != 200 ]]; then
    fail "A8-03 $label: dotted client route must serve the SPA shell, got $STATUS"
  elif ! contains_ci "$ct" '^text/html'; then
    fail "A8-03 $label: expected text/html SPA shell, got '$ct'"
  elif ! grep -q 'id="ComposeTarget"' "$BODY"; then
    fail "A8-03 $label: response body is not the SPA shell (index.html)"
  else
    pass "A8-03 $label: dotted client route serves the SPA shell"
  fi
}

# Expected exact values. Presence alone is not enough: a header with an empty or
# wrong value is as broken as a missing one. Value, not just name, is asserted.
SECURITY_HEADERS=(
  "X-Frame-Options|SAMEORIGIN"
  "X-Content-Type-Options|nosniff"
  "X-XSS-Protection|1; mode=block"
  "Referrer-Policy|strict-origin-when-cross-origin"
)

expect_security_headers() { # path expected-status
  local path="$1" want="$2" bad="" name value actual n spec
  request "$path"
  for spec in "${SECURITY_HEADERS[@]}"; do
    name="${spec%%|*}"
    value="${spec#*|}"
    n="$(header_count "$name")"
    actual="$(header_values "$name" | paste -sd' | ' -)"
    if [[ "$n" == 0 ]]; then
      bad+="$name=<missing> "
    elif [[ "$n" != 1 ]]; then
      bad+="$name='$actual' (duplicated) "
    elif [[ "$actual" != "$value" ]]; then
      bad+="$name='$actual' (want '$value') "
    fi
  done
  if [[ "$STATUS" != "$want" ]]; then
    fail "A8-02 $path: expected status $want, got $STATUS"
  elif [[ -n "$bad" ]]; then
    fail "A8-02 $path: wrong/missing security header(s): ${bad% }"
  else
    pass "A8-02 $path: all security headers have their exact required values (status $STATUS)"
  fi
}

# --- A8-01: Content-Type via mime table, never add_header --------------------
expect_single_content_type /skiko.wasm      '^application/wasm$'
expect_single_content_type /composeApp.js   '^(application|text)/javascript$'
expect_single_content_type /skiko.js        '^(application|text)/javascript$'
expect_single_content_type /firebase-config.js '^(application|text)/javascript$'
expect_single_content_type /styles.css      '^text/css$'
expect_single_content_type /index.html      '^text/html$'
expect_single_content_type /icon.svg        '^image/svg\+xml$'

# --- A8-03: caching policy ---------------------------------------------------
expect_single_cache_control /icon.svg
expect_single_cache_control /styles.css
expect_revalidate_cache /skiko.wasm          '/skiko.wasm'
expect_revalidate_cache /composeApp.js       '/composeApp.js'
expect_revalidate_cache /skiko.js            '/skiko.js'
expect_revalidate_cache /firebase-config.js  '/firebase-config.js'
expect_revalidate_cache /styles.css          '/styles.css'
expect_revalidate_cache /index.html          '/index.html'
# The SPA shell (and every client route that resolves to it) must not be cached.
expect_exact_cache_control /                 'no-cache'
expect_exact_cache_control /index.html       'no-cache'
# Media assets are unhashed: an exact, short, non-immutable TTL is required.
expect_exact_cache_control /icon.svg         'max-age=604800'
expect_exact_cache_control /fonts/app.woff2  'max-age=604800'

# --- A8-03: missing assets must not fall back to the SPA shell ---------------
expect_404 /missing.wasm
expect_404 /missing.js
expect_404 /assets/missing.json
expect_404 /assets/missing.css.map
expect_404 /fonts/missing.woff2
expect_404 /missing.asset

# --- A8-02: security headers on every kind of location ----------------------
expect_security_headers /                    200
expect_security_headers /index.html          200
expect_security_headers /skiko.wasm          200
expect_security_headers /composeApp.js       200
expect_security_headers /styles.css          200
expect_security_headers /icon.svg            200
expect_security_headers /missing.wasm        404
expect_security_headers /assets/missing.json 404

# --- guard: legitimate SPA routes still serve the shell ----------------------
request /dashboard/overview
if [[ "$STATUS" == 200 ]] && contains_ci "$(header_values 'Content-Type')" 'text/html'; then
  pass "A8-03 /dashboard/overview: extensionless SPA route still serves index.html"
else
  fail "A8-03 /dashboard/overview: extensionless SPA route should serve index.html, got $STATUS"
fi

# Dotted client routes (decimal/version segments) are routes, not filenames.
# They must reach the SPA shell, not the missing-asset 404 path.
expect_spa_shell /grade/2.5
expect_spa_shell /grade/2.5/details
expect_exact_cache_control /grade/2.5 'no-cache'
expect_security_headers /grade/2.5 200

echo
if [[ "$FAILS" -eq 0 ]]; then
  echo "RESULT: PASS ($CHECKS checks)"
  exit 0
fi
echo "RESULT: FAIL ($FAILS of $CHECKS checks failed)"
exit 1
