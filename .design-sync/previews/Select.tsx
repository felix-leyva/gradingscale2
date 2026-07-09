import { Select } from "gradingscale-design-web";

const scales = [
  { value: "german", label: "German (1.0–6.0)" },
  { value: "swiss", label: "Swiss (6–1)" },
  { value: "us", label: "US letter grades" },
];

export const GradeScale = () => (
  <Select label="Grade scale" options={scales} value="german" onChange={() => {}} />
);

export const Disabled = () => (
  <Select label="Grade scale" options={scales} value="swiss" onChange={() => {}} disabled />
);
