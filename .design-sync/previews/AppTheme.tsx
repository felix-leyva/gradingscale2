import { AppTheme, Button, Card } from "gradingscale-design-web";

export const Light = () => (
  <AppTheme theme="light">
    <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 12 }}>
      <Card variant="elevated">
        <strong>Grade average</strong>
        <div>2.3 — across 12 graded assignments</div>
      </Card>
      <Button variant="filled">Save grade</Button>
    </div>
  </AppTheme>
);

export const Dark = () => (
  <AppTheme theme="dark">
    <div style={{ padding: 16, display: "flex", flexDirection: "column", gap: 12 }}>
      <Card variant="elevated">
        <strong>Grade average</strong>
        <div>2.3 — across 12 graded assignments</div>
      </Card>
      <Button variant="filled">Save grade</Button>
    </div>
  </AppTheme>
);
