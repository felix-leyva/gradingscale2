import { Button, Card, NavigationSuite, Scaffold } from "gradingscale-design-web";

const CalcIcon = (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
    <path d="M7 2h10a2 2 0 0 1 2 2v16a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm0 2v4h10V4H7zm0 6v2h2v-2H7zm4 0v2h2v-2h-2zm4 0v2h2v-2h-2zM7 14v2h2v-2H7zm4 0v2h2v-2h-2zm4 0v6h2v-6h-2zM7 18v2h2v-2H7zm4 0v2h2v-2h-2z" />
  </svg>
);
const ListIcon = (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
    <path d="M3 5h2v2H3V5zm4 0h14v2H7V5zM3 11h2v2H3v-2zm4 0h14v2H7v-2zM3 17h2v2H3v-2zm4 0h14v2H7v-2z" />
  </svg>
);

export const AppShell = () => (
  <Scaffold
    title="Grading Scale"
    topBarActions={<Button variant="text">Help</Button>}
    navigation={
      <NavigationSuite
        layout="bar"
        items={[
          { key: "calculator", label: "Calculator", icon: CalcIcon },
          { key: "list", label: "Grade scales", icon: ListIcon },
        ]}
        selectedKey="calculator"
        onSelect={() => {}}
      />
    }
  >
    <Card variant="elevated">
      <strong>Grade calculator</strong>
      <div>Enter points to see the matching grade.</div>
    </Card>
  </Scaffold>
);
