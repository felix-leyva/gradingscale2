import { Button } from "gradingscale-design-web";

const CalcIcon = (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
    <path d="M7 2h10a2 2 0 0 1 2 2v16a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V4a2 2 0 0 1 2-2zm0 2v4h10V4H7zm0 6v2h2v-2H7zm4 0v2h2v-2h-2zm4 0v2h2v-2h-2zM7 14v2h2v-2H7zm4 0v2h2v-2h-2zm4 0v6h2v-6h-2zM7 18v2h2v-2H7zm4 0v2h2v-2h-2z" />
  </svg>
);

export const Filled = () => <Button variant="filled">Save grade</Button>;
export const Tonal = () => <Button variant="tonal">Import scale</Button>;
export const Outlined = () => <Button variant="outlined">Export</Button>;
export const Text = () => <Button variant="text">Cancel</Button>;
export const WithIcon = () => (
  <Button variant="filled" icon={CalcIcon}>
    Calculate
  </Button>
);
export const Disabled = () => (
  <Button variant="filled" disabled>
    Save grade
  </Button>
);
