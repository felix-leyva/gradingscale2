import { Card } from "gradingscale-design-web";

export const Filled = () => (
  <Card variant="filled">
    <strong>Grade average</strong>
    <div>2.3 — across 12 graded assignments</div>
  </Card>
);

export const Outlined = () => (
  <Card variant="outlined">
    <strong>German (1.0–6.0)</strong>
    <div>15 grades recorded</div>
  </Card>
);

export const Elevated = () => (
  <Card variant="elevated" onClick={() => {}}>
    <strong>Weighted calculator</strong>
    <div>Tap to open the weighted grade calculator</div>
  </Card>
);
