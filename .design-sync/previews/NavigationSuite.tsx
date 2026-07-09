import { NavigationSuite } from "gradingscale-design-web";

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
const ScaleIcon = (
  <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor">
    <path d="M12 3a2 2 0 0 1 2 2h5v2h-2.2l2.7 6.3a3.5 3.5 0 0 1-6.9.7L15.8 7H14v12h4v2H6v-2h4V7H8.2l3.2 7a3.5 3.5 0 0 1-6.9-.7L7.2 7H5V5h5a2 2 0 0 1 2-2z" />
  </svg>
);

const items = [
  { key: "calculator", label: "Calculator", icon: CalcIcon },
  { key: "list", label: "Grade scales", icon: ListIcon },
  { key: "weighted", label: "Weighted", icon: ScaleIcon },
];

export const Bar = () => (
  <div style={{ width: 420 }}>
    <NavigationSuite layout="bar" items={items} selectedKey="calculator" onSelect={() => {}} />
  </div>
);

export const Rail = () => (
  <div style={{ height: 320, display: "flex" }}>
    <NavigationSuite layout="rail" items={items} selectedKey="list" onSelect={() => {}} />
  </div>
);
