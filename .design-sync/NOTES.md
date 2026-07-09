# design-sync notes — gradingscale2

- This repo is a Kotlin Multiplatform Compose app; the synced design system is `design-web/`, a hand-authored React mirror of the Compose Material 3 theme. It is NOT the app's real UI code — fidelity flows from `design-web/scripts/gen-tokens.mjs`, which regenerates `src/tokens.css` from `composeApp/src/commonMain/kotlin/de/felixlf/gradingscale2/theme/{Color,Type}.kt` on every `npm run build` (wired as `prebuild`). If the Kotlin theme changes shape (≠35 roles × 6 schemes), the generator fails loudly — update it, don't bypass it.
- Node is managed by fnm and NOT on the default PATH (npm works, but harness-launched shells print a PATH warning — harmless).
- Build: `npm --prefix design-web run build` from the repo root (config `buildCmd`). Requires `npm install` inside `design-web/` once per clone.
- Converter invocation: `--node-modules design-web/node_modules --entry ./design-web/dist/index.js`. React 18 is a devDependency of design-web, so vendoring resolves from there.
- Contrast tiers (`*MediumContrast`/`*HighContrast` in Color.kt) are intentionally NOT exported — they come from an older palette generation and would misrepresent the app. Android Material You dynamic color is also unrepresentable statically.
- `provider` = `AppTheme` (components assume the themed root; tokens also live at `:root` so cards render either way).
- Overlay overrides: Dialog + Snackbar + Scaffold are `cardMode: single` (fixed-position/full-viewport), NavigationSuite `column` (full-width bar).
- Playwright/chromium is NOT installed on this machine; the user declined the ~200MB install (2026-07-09). Render checks ran with `--no-render-check`; every card was instead manually verified via the Claude Preview browser (both `prefers-color-scheme` values, per-card screenshots). If a future sync can install playwright, drop the flag.
- No machine grades exist in `.design-sync/.cache/review/` (capture needs playwright). First anchored upload is the durable verification baseline.

## Known render warns
- `[RENDER_SKIPPED]` — expected while playwright remains uninstalled (user-accepted).

## Re-sync risks
- `tokens.css` is generated but committed; if someone edits it by hand the next build silently overwrites it — the generator is the only source.
- The React components are hand-maintained: a new Compose component or a visual redesign in the KMP app does NOT propagate automatically; only colors/typography do (via gen-tokens). Component-level drift must be ported by hand into `design-web/src/components/`.
- Demo page (`design-web/demo/`) is the manual verification harness; keep it rendering every component or manual checks lose coverage.
- Renders were never machine-checked (no playwright) — treat the first playwright-enabled re-sync as a full re-verify.
