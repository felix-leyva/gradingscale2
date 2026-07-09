import { MouseEventHandler, ReactNode } from "react";

export interface ButtonProps {
  /** M3 button style. Default "filled". */
  variant?: "filled" | "tonal" | "outlined" | "text";
  disabled?: boolean;
  /** Optional leading icon (any ReactNode, e.g. an inline SVG sized 18x18). */
  icon?: ReactNode;
  onClick?: MouseEventHandler<HTMLButtonElement>;
  type?: "button" | "submit" | "reset";
  children: ReactNode;
}

/** Material 3 button: filled / tonal / outlined / text. */
export function Button({ variant = "filled", disabled, icon, onClick, type = "button", children }: ButtonProps) {
  return (
    <button
      className={`gs-button gs-button--${variant}${icon ? " gs-button--with-icon" : ""}`}
      disabled={disabled}
      onClick={onClick}
      type={type}
    >
      {icon && <span className="gs-button__icon">{icon}</span>}
      {children}
    </button>
  );
}
