import { TextField } from "gradingscale-design-web";

export const Populated = () => (
  <TextField label="Points" value="15.5" onChange={() => {}} supportingText="Max. 20 points" />
);

export const Empty = () => (
  <TextField label="Grade name" value="" onChange={() => {}} placeholder="e.g. Sehr gut" />
);

export const Error = () => (
  <TextField label="Weight" value="150" error supportingText="Must be between 0 and 100" onChange={() => {}} />
);

export const WithTrailing = () => (
  <TextField label="Percentage" value="87" onChange={() => {}} trailing={<span>%</span>} />
);
