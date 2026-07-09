import { MouseEventHandler, ReactNode } from "react";

export interface CardProps {
  /** M3 card style. Default "filled". */
  variant?: "filled" | "outlined" | "elevated";
  /** Makes the card interactive (hover/press states, pointer cursor). */
  onClick?: MouseEventHandler<HTMLDivElement>;
  children: ReactNode;
}

/** Material 3 card: filled / outlined / elevated. */
export function Card({ variant = "filled", onClick, children }: CardProps) {
  return (
    <div
      className={`gs-card gs-card--${variant}${onClick ? " gs-card--clickable" : ""}`}
      onClick={onClick}
      role={onClick ? "button" : undefined}
      tabIndex={onClick ? 0 : undefined}
    >
      {children}
    </div>
  );
}
