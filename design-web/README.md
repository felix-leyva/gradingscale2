# gradingscale-design-web

A small React + CSS mirror of the GradingScale2 app's Compose Material 3 theme, built so [Claude Design](https://claude.ai/design) can produce designs that look like the real app. It is **not** used by the app itself.

## How it stays true to the app

`scripts/gen-tokens.mjs` parses the Kotlin theme sources (read-only):

- `composeApp/.../theme/Color.kt` → `--md-sys-color-*` custom properties (base light + dark schemes; the medium/high-contrast tiers are intentionally skipped — they predate the current palette — and Android-only Material You dynamic color can't be represented statically)
- `composeApp/.../theme/Type.kt` → `--md-sys-typescale-*` (3 overrides merged over the M3 defaults; generic serif/sans stacks — the app bundles no font files)

It runs automatically as `prebuild`, so `src/tokens.css` (committed, `@generated`) can never drift from the Kotlin source. Change a color in Compose → rebuild → the web mirror follows. If `Color.kt` stops matching the expected shape (35 roles × 6 schemes) the generator fails loudly.

Shape radii are the M3 defaults (the app defines no custom `Shapes`); spacing/elevation/blur are small static scales matching the app's inline dp usage.

## Components (12)

`AppTheme` (root wrapper), `Scaffold`, `NavigationSuite`, `Button`, `Card`, `TextField`, `Select`, `ListItem`, `Dialog`, `Snackbar`, `Divider`, `LoadingIndicator` — mirroring the app's actual vocabulary (`PersistentScaffold`, `AppNavigationSuite`, `DropboxSelector`, `GradeScaleListItem`, the upsert dialogs, …). React 18 peers, zero runtime dependencies, plain CSS under the `.gs-` namespace.

## Commands

```sh
npm install          # once
npm run build        # gen-tokens → esbuild (ESM + styles) → tsc declarations
npm run demo         # build + serve demo/ — every component, light & dark side by side
```

## Syncing to Claude Design

Handled by the repo-root `.design-sync/` config via the `/design-sync` skill. Conventions the design agent reads live in `.design-sync/conventions.md`.
