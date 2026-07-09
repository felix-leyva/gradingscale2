import { Divider, ListItem } from "gradingscale-design-web";

export const Selected = () => (
  <ListItem headline="German (1.0–6.0)" supporting="15 grades" trailing="1.0" selected onClick={() => {}} />
);

export const Default = () => (
  <ListItem headline="Swiss (6–1)" supporting="11 grades" trailing="5.5" onClick={() => {}} />
);

export const SelectionList = () => (
  <div style={{ minWidth: 320 }}>
    <ListItem headline="German (1.0–6.0)" supporting="15 grades" trailing="1.0" selected onClick={() => {}} />
    <Divider inset />
    <ListItem headline="Swiss (6–1)" supporting="11 grades" trailing="5.5" onClick={() => {}} />
    <Divider inset />
    <ListItem headline="US letter grades" supporting="13 grades" trailing="A+" onClick={() => {}} />
  </div>
);
