// Generates src/tokens.css from the Compose theme sources (read-only):
//   ../composeApp/src/commonMain/kotlin/de/felixlf/gradingscale2/theme/Color.kt
//   ../composeApp/src/commonMain/kotlin/de/felixlf/gradingscale2/theme/Type.kt
// Run via `npm run gen-tokens` (also wired as prebuild). Zero dependencies, Node >= 18.
//
// Only the base light/dark schemes are exported. The medium/high-contrast tiers in
// Color.kt come from an older palette generation and Claude Design has no contrast
// toggle; they are parsed for the shape assertion, then discarded.

import { readFileSync, writeFileSync, mkdirSync } from "node:fs";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const themeDir = resolve(here, "../../composeApp/src/commonMain/kotlin/de/felixlf/gradingscale2/theme");
const outFile = resolve(here, "../src/tokens.css");

const EXPECTED_ROLES = 35;
const EXPECTED_SCHEMES = 6;

// ---------- Colors ----------

const colorKt = readFileSync(resolve(themeDir, "Color.kt"), "utf8");
const colorRe = /val\s+(\w+?)(Light|Dark)(MediumContrast|HighContrast)?\s*=\s*Color\(0x[Ff]{2}([0-9A-Fa-f]{6})\)/g;

/** @type {Record<string, Map<string, string>>} scheme key -> (role -> #hex) */
const schemes = {};
for (const m of colorKt.matchAll(colorRe)) {
  const [, role, mode, contrast, hex] = m;
  const key = mode + (contrast ?? "");
  (schemes[key] ??= new Map()).set(role, "#" + hex.toLowerCase());
}

const schemeKeys = Object.keys(schemes);
if (schemeKeys.length !== EXPECTED_SCHEMES) {
  throw new Error(`Color.kt shape changed: expected ${EXPECTED_SCHEMES} schemes, found ${schemeKeys.length} (${schemeKeys.join(", ")})`);
}
for (const [key, map] of Object.entries(schemes)) {
  if (map.size !== EXPECTED_ROLES) {
    throw new Error(`Color.kt shape changed: scheme "${key}" has ${map.size} roles, expected ${EXPECTED_ROLES}`);
  }
}

const kebab = (s) => s.replace(/([a-z0-9])([A-Z])/g, "$1-$2").toLowerCase();
const colorVars = (map, indent) =>
  [...map.entries()].map(([role, hex]) => `${indent}--md-sys-color-${kebab(role)}: ${hex};`).join("\n");

// ---------- Typography ----------

const typeKt = readFileSync(resolve(themeDir, "Type.kt"), "utf8");

// Material 3 default typescale (font, size, line-height, weight, tracking in px).
const M3_TYPESCALE = {
  "display-large": { font: "sans", size: 57, line: 64, weight: 400, tracking: -0.25 },
  "display-medium": { font: "sans", size: 45, line: 52, weight: 400, tracking: 0 },
  "display-small": { font: "sans", size: 36, line: 44, weight: 400, tracking: 0 },
  "headline-large": { font: "sans", size: 32, line: 40, weight: 400, tracking: 0 },
  "headline-medium": { font: "sans", size: 28, line: 36, weight: 400, tracking: 0 },
  "headline-small": { font: "sans", size: 24, line: 32, weight: 400, tracking: 0 },
  "title-large": { font: "sans", size: 22, line: 28, weight: 400, tracking: 0 },
  "title-medium": { font: "sans", size: 16, line: 24, weight: 500, tracking: 0.15 },
  "title-small": { font: "sans", size: 14, line: 20, weight: 500, tracking: 0.1 },
  "body-large": { font: "sans", size: 16, line: 24, weight: 400, tracking: 0.5 },
  "body-medium": { font: "sans", size: 14, line: 20, weight: 400, tracking: 0.25 },
  "body-small": { font: "sans", size: 12, line: 16, weight: 400, tracking: 0.4 },
  "label-large": { font: "sans", size: 14, line: 20, weight: 500, tracking: 0.1 },
  "label-medium": { font: "sans", size: 12, line: 16, weight: 500, tracking: 0.5 },
  "label-small": { font: "sans", size: 11, line: 16, weight: 500, tracking: 0.5 },
};

const FONT_FAMILY_MAP = { Serif: "serif", SansSerif: "sans", Monospace: "mono", Cursive: "serif", Default: "sans" };
const FONT_WEIGHT_MAP = {
  Thin: 100, ExtraLight: 200, Light: 300, Normal: 400, Medium: 500,
  SemiBold: 600, Bold: 700, ExtraBold: 800, Black: 900,
};

const styleRe = /(\w+)\s*=\s*TextStyle\(([\s\S]*?)\n\s{4}\),/g;
let overrideCount = 0;
for (const m of typeKt.matchAll(styleRe)) {
  const [, kotlinName, body] = m;
  const cssName = kebab(kotlinName);
  const style = M3_TYPESCALE[cssName];
  if (!style) throw new Error(`Type.kt overrides unknown text style "${kotlinName}"`);
  overrideCount++;
  const family = body.match(/fontFamily\s*=\s*FontFamily\.(\w+)/)?.[1];
  if (family) {
    const mapped = FONT_FAMILY_MAP[family];
    if (!mapped) throw new Error(`Unmapped FontFamily.${family} in Type.kt`);
    style.font = mapped;
  }
  const weight = body.match(/fontWeight\s*=\s*FontWeight\.(\w+)/)?.[1];
  if (weight) {
    if (!(weight in FONT_WEIGHT_MAP)) throw new Error(`Unmapped FontWeight.${weight} in Type.kt`);
    style.weight = FONT_WEIGHT_MAP[weight];
  }
  const num = (prop) => {
    const v = body.match(new RegExp(`${prop}\\s*=\\s*\\(?(-?[\\d.]+)\\)?\\.sp`))?.[1];
    return v === undefined ? undefined : Number(v);
  };
  const size = num("fontSize");
  const line = num("lineHeight");
  const tracking = num("letterSpacing");
  if (size !== undefined) style.size = size;
  if (line !== undefined) style.line = line;
  if (tracking !== undefined) style.tracking = tracking;
}
if (overrideCount === 0) throw new Error("Type.kt shape changed: no TextStyle blocks parsed");

const typescaleVars = Object.entries(M3_TYPESCALE)
  .map(([name, s]) =>
    [
      `  --md-sys-typescale-${name}-font: var(--gs-font-${s.font});`,
      `  --md-sys-typescale-${name}-size: ${s.size}px;`,
      `  --md-sys-typescale-${name}-line-height: ${s.line}px;`,
      `  --md-sys-typescale-${name}-weight: ${s.weight};`,
      `  --md-sys-typescale-${name}-tracking: ${s.tracking}px;`,
    ].join("\n"),
  )
  .join("\n");

// ---------- Assemble ----------

const light = schemes.Light;
const dark = schemes.Dark;

const css = `/* @generated by scripts/gen-tokens.mjs from Color.kt + Type.kt — do not edit.
 * Source of truth: composeApp/src/commonMain/kotlin/de/felixlf/gradingscale2/theme/
 * Base light/dark schemes only (contrast tiers intentionally not exported). */

:root {
  color-scheme: light dark;

  /* Font stacks — Compose uses generic FontFamily.Serif / FontFamily.SansSerif. */
  --gs-font-serif: Georgia, 'Times New Roman', serif;
  --gs-font-sans: system-ui, Roboto, 'Helvetica Neue', sans-serif;
  --gs-font-mono: ui-monospace, Menlo, monospace;

  /* Material 3 typescale (Type.kt overrides merged over M3 defaults). */
${typescaleVars}

  /* Shape — Material 3 default corner scale (no custom Shapes in the app). */
  --md-sys-shape-corner-none: 0px;
  --md-sys-shape-corner-extra-small: 4px;
  --md-sys-shape-corner-small: 8px;
  --md-sys-shape-corner-medium: 12px;
  --md-sys-shape-corner-large: 16px;
  --md-sys-shape-corner-extra-large: 28px;
  --md-sys-shape-corner-full: 9999px;

  /* Spacing scale (covers the app's inline 4/8/12/16/20/24dp usage). */
  --gs-space-1: 4px;
  --gs-space-2: 8px;
  --gs-space-3: 12px;
  --gs-space-4: 16px;
  --gs-space-5: 20px;
  --gs-space-6: 24px;

  /* Elevation shadows (M3 levels 1-3). */
  --gs-elevation-1: 0 1px 2px rgba(0, 0, 0, 0.3), 0 1px 3px 1px rgba(0, 0, 0, 0.15);
  --gs-elevation-2: 0 1px 2px rgba(0, 0, 0, 0.3), 0 2px 6px 2px rgba(0, 0, 0, 0.15);
  --gs-elevation-3: 0 1px 3px rgba(0, 0, 0, 0.3), 0 4px 8px 3px rgba(0, 0, 0, 0.15);

  /* Haze top-bar blur radius (Theme.kt transparentHaze). */
  --gs-blur: 8px;
}

/* Base light scheme (default). */
:root,
[data-theme="light"] {
${colorVars(light, "  ")}
}

/* Explicit dark override — set data-theme="dark" on :root or any container. */
[data-theme="dark"] {
${colorVars(dark, "  ")}
}

/* OS dark mode, unless explicitly overridden to light. */
@media (prefers-color-scheme: dark) {
  :root:not([data-theme="light"]) {
${colorVars(dark, "    ")}
  }
}
`;

mkdirSync(dirname(outFile), { recursive: true });
writeFileSync(outFile, css);
console.log(
  `tokens.css written: ${light.size} light + ${dark.size} dark color roles, ` +
  `${Object.keys(M3_TYPESCALE).length} typescale styles (${overrideCount} overridden from Type.kt).`,
);
