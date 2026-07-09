import { Dialog, TextField } from "gradingscale-design-web";

export const EditGrade = () => (
  <Dialog
    open
    title="Edit grade"
    confirmLabel="Save"
    dismissLabel="Cancel"
    onConfirm={() => {}}
    onClose={() => {}}
  >
    <div style={{ display: "flex", flexDirection: "column", gap: 16, paddingTop: 8 }}>
      <TextField label="Grade name" value="Sehr gut" onChange={() => {}} />
      <TextField label="Percentage" value="95" onChange={() => {}} supportingText="0–100" />
    </div>
  </Dialog>
);
