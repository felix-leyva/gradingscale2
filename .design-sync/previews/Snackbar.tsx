import { Snackbar } from "gradingscale-design-web";

export const WithAction = () => (
  <Snackbar open message="Grade scale imported" actionLabel="Undo" onAction={() => {}} />
);
