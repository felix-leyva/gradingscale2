# GradingScale design system — build conventions

This library is a React mirror of the GradingScale2 app's Compose Material 3 theme (teal primary, amber secondary, indigo tertiary, warm off-white surfaces). Every design must look like that app.

## Wrap every page in AppTheme

`AppTheme` applies the app background, text color, base typography, and the color scheme. Without it, content renders on an unthemed background with browser-default text.

```jsx
<AppTheme theme="system">          {/* "light" | "dark" | "system" */}
  <Scaffold title="Grading Scale" navigation={…}>…</Scaffold>
</AppTheme>
```

Dark mode: `AppTheme theme="dark"`, or leave `"system"` — tokens also respond to `prefers-color-scheme` and to a `data-theme="dark"` attribute on any ancestor.

## Styling idiom: CSS custom properties, never invented classes

There are **no utility classes**. The `gs-*` classes in the stylesheet belong to the components — do not add new ones or reuse them on your own markup. Style your own layout glue with inline styles (or a style block) that reference the tokens:

- **Color** — `var(--md-sys-color-<role>)`, full Material 3 role set, light/dark aware. Most used: `primary`, `on-primary`, `primary-container`, `secondary-container`, `on-secondary-container`, `tertiary`, `error`, `background`, `on-background`, `surface`, `on-surface`, `surface-variant`, `on-surface-variant`, `surface-container`, `surface-container-low`, `surface-container-high`, `surface-container-highest`, `outline`, `outline-variant`, `inverse-surface`, `inverse-on-surface`. Never hard-code hex — the values swap per theme.
- **Typography** — `var(--md-sys-typescale-<style>-{font,size,line-height,weight,tracking})` for the 15 M3 styles (`display-large` … `label-small`). `display-large` and `headline-small` are serif (`--gs-font-serif`); body text is sans (`--gs-font-sans`). No webfonts ship — generic stacks only.
- **Shape** — `var(--md-sys-shape-corner-{extra-small,small,medium,large,extra-large,full})` = 4/8/12/16/28/9999 px.
- **Spacing** — `var(--gs-space-1)` … `var(--gs-space-6)` = 4/8/12/16/20/24 px.
- **Elevation** — `box-shadow: var(--gs-elevation-1|2|3)`.

## Where the truth lives

Read `styles.css` (it `@import`s `_ds_bundle.css`, which carries every token definition and all component CSS) before styling anything custom. Per-component API and usage: `components/general/<Name>/` (`<Name>.d.ts` is the props contract).

## Components

`AppTheme`, `Scaffold` (app shell: blurred top bar, `navigation`/`fab`/`snackbar` slots), `NavigationSuite` (adaptive bar/rail), `Button` (filled/tonal/outlined/text), `Card` (filled/outlined/elevated), `TextField` (outlined, floating label), `Select`, `ListItem` (selected state = secondary-container tint), `Dialog`, `Snackbar`, `Divider`, `LoadingIndicator`. Icons are not bundled — pass inline SVGs (24×24, `fill="currentColor"`) to `icon` props.

## Idiomatic page skeleton

```jsx
<AppTheme theme="system">
  <Scaffold
    title="Grading Scale"
    navigation={
      <NavigationSuite
        layout="auto"
        items={[{ key: "calculator", label: "Calculator", icon: CalcIcon }]}
        selectedKey="calculator"
        onSelect={() => {}}
      />
    }
  >
    <div style={{ display: "flex", flexDirection: "column", gap: "var(--gs-space-4)" }}>
      <Card variant="elevated">
        <strong>Grade average</strong>
        <div style={{ color: "var(--md-sys-color-on-surface-variant)" }}>2.3 — across 12 assignments</div>
      </Card>
      <TextField label="Points" value="15.5" onChange={() => {}} supportingText="Max. 20 points" />
      <Button variant="filled">Save grade</Button>
    </div>
  </Scaffold>
</AppTheme>
```
