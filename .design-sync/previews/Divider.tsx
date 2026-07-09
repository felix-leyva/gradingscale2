import { Divider } from "gradingscale-design-web";

export const Horizontal = () => (
  <div style={{ width: 280 }}>
    <div style={{ padding: 8 }}>Calculator</div>
    <Divider />
    <div style={{ padding: 8 }}>Grade scales</div>
  </div>
);

export const Inset = () => (
  <div style={{ width: 280 }}>
    <div style={{ padding: 8 }}>German (1.0–6.0)</div>
    <Divider inset />
    <div style={{ padding: 8 }}>Swiss (6–1)</div>
  </div>
);

export const Vertical = () => (
  <div style={{ display: "flex", alignItems: "stretch", gap: 16, height: 48 }}>
    <span style={{ alignSelf: "center" }}>Points: 15.5</span>
    <Divider orientation="vertical" />
    <span style={{ alignSelf: "center" }}>Grade: 1.7</span>
  </div>
);
