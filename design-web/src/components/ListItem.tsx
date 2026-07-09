import { ReactNode } from "react";

export interface ListItemProps {
  /** Primary text. */
  headline: string;
  /** Secondary line below the headline. */
  supporting?: string;
  /** Leading element (icon, avatar). */
  leading?: ReactNode;
  /** Trailing element (value text, icon button, menu). */
  trailing?: ReactNode;
  /** Selected state — secondary-container tint, like the app's grade-scale list. */
  selected?: boolean;
  onClick?: () => void;
}

/** M3 list item mirroring GradeScaleListItem / selectable list rows. */
export function ListItem({ headline, supporting, leading, trailing, selected, onClick }: ListItemProps) {
  const classes = [
    "gs-list-item",
    selected ? "gs-list-item--selected" : "",
    onClick ? "gs-list-item--clickable" : "",
  ]
    .filter(Boolean)
    .join(" ");
  return (
    <div
      className={classes}
      onClick={onClick}
      role={onClick ? "button" : undefined}
      tabIndex={onClick ? 0 : undefined}
      aria-selected={selected}
    >
      {leading && <span className="gs-list-item__leading">{leading}</span>}
      <span className="gs-list-item__text">
        <span className="gs-list-item__headline">{headline}</span>
        {supporting && <span className="gs-list-item__supporting">{supporting}</span>}
      </span>
      {trailing && <span className="gs-list-item__trailing">{trailing}</span>}
    </div>
  );
}
