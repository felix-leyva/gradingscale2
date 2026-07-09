import { useState } from "react";
import { createRoot } from "react-dom/client";
import {
  AppTheme,
  Button,
  Card,
  Dialog,
  Divider,
  ListItem,
  LoadingIndicator,
  NavigationSuite,
  Scaffold,
  Select,
  Snackbar,
  TextField,
} from "../src/index";

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

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <section style={{ marginBottom: 24 }}>
      <h3 style={{ margin: "0 0 8px", fontSize: 14, opacity: 0.7 }}>{title}</h3>
      <div style={{ display: "flex", flexWrap: "wrap", gap: 12, alignItems: "center" }}>{children}</div>
    </section>
  );
}

function Gallery() {
  const [text, setText] = useState("15.5");
  const [scale, setScale] = useState("german");
  const [dialogOpen, setDialogOpen] = useState(false);
  return (
    <div style={{ padding: 16 }}>
      <Section title="Buttons">
        <Button variant="filled">Save grade</Button>
        <Button variant="tonal">Import</Button>
        <Button variant="outlined">Export</Button>
        <Button variant="text">Cancel</Button>
        <Button variant="filled" disabled>
          Disabled
        </Button>
        <Button variant="filled" icon={CalcIcon}>
          Calculate
        </Button>
      </Section>

      <Section title="Cards">
        <Card variant="filled">
          <strong>Filled card</strong>
          <div>Grade average: 2.3</div>
        </Card>
        <Card variant="outlined">
          <strong>Outlined card</strong>
          <div>12 grades recorded</div>
        </Card>
        <Card variant="elevated" onClick={() => {}}>
          <strong>Elevated card</strong>
          <div>Click me</div>
        </Card>
      </Section>

      <Section title="Text field + Select">
        <TextField label="Points" value={text} onChange={setText} supportingText="Max. 20 points" />
        <TextField label="Grade name" value="" onChange={() => {}} placeholder="e.g. Sehr gut" />
        <TextField label="Weight" value="150" error supportingText="Must be between 0 and 100" onChange={() => {}} />
        <Select
          label="Grade scale"
          value={scale}
          onChange={setScale}
          options={[
            { value: "german", label: "German (1.0–6.0)" },
            { value: "swiss", label: "Swiss (6–1)" },
            { value: "us", label: "US letter grades" },
          ]}
        />
      </Section>

      <Section title="List">
        <Card variant="outlined">
          <div style={{ minWidth: 300, margin: -16 }}>
            <ListItem headline="German (1.0–6.0)" supporting="15 grades" selected onClick={() => {}} trailing="1.0" />
            <Divider inset />
            <ListItem headline="Swiss (6–1)" supporting="11 grades" onClick={() => {}} trailing="5.5" />
            <Divider inset />
            <ListItem headline="US letter grades" supporting="13 grades" onClick={() => {}} trailing="A+" />
          </div>
        </Card>
      </Section>

      <Section title="Dialog + Snackbar + Loading">
        <Button variant="outlined" onClick={() => setDialogOpen(true)}>
          Open dialog
        </Button>
        <LoadingIndicator />
        <LoadingIndicator size={24} />
      </Section>

      <Dialog
        open={dialogOpen}
        title="Edit grade"
        confirmLabel="Save"
        dismissLabel="Cancel"
        onConfirm={() => setDialogOpen(false)}
        onClose={() => setDialogOpen(false)}
      >
        <div style={{ display: "flex", flexDirection: "column", gap: 16, paddingTop: 8 }}>
          <TextField label="Grade name" value="Sehr gut" onChange={() => {}} />
          <TextField label="Percentage" value="95" onChange={() => {}} supportingText="0–100" />
        </div>
      </Dialog>
    </div>
  );
}

function ScaffoldDemo() {
  const [tab, setTab] = useState("calculator");
  return (
    <div style={{ height: 480, overflow: "hidden", borderRadius: 12, border: "1px solid #8884", position: "relative" }}>
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
            selectedKey={tab}
            onSelect={setTab}
          />
        }
      >
        <Card variant="elevated">Content area — {tab}</Card>
      </Scaffold>
    </div>
  );
}

function App() {
  const [snackbarOpen] = useState(true);
  return (
    <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", minHeight: "100vh" }}>
      <AppTheme theme="light">
        <div style={{ padding: 16 }}>
          <h2 style={{ marginTop: 0 }}>Light</h2>
          <Gallery />
          <ScaffoldDemo />
        </div>
      </AppTheme>
      <AppTheme theme="dark">
        <div style={{ padding: 16 }}>
          <h2 style={{ marginTop: 0 }}>Dark</h2>
          <Gallery />
          <ScaffoldDemo />
        </div>
      </AppTheme>
      <Snackbar open={snackbarOpen} message="Grade scale imported" actionLabel="Undo" onAction={() => {}} />
    </div>
  );
}

createRoot(document.getElementById("root")!).render(<App />);
