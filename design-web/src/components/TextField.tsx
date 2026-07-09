import { ReactNode, useId } from "react";

export interface TextFieldProps {
  /** Floating label, always required for the outlined M3 look. */
  label: string;
  value: string;
  onChange?: (value: string) => void;
  /** Error state: outline, label and supporting text turn error-colored. */
  error?: boolean;
  /** Helper or error message below the field. */
  supportingText?: string;
  /** Trailing element (icon button, unit suffix, etc.). */
  trailing?: ReactNode;
  /** HTML input type, e.g. "text", "number", "email". Default "text". */
  type?: string;
  placeholder?: string;
  disabled?: boolean;
}

/** Outlined Material 3 text field (the only variant used by the app). */
export function TextField({
  label,
  value,
  onChange,
  error,
  supportingText,
  trailing,
  type = "text",
  placeholder,
  disabled,
}: TextFieldProps) {
  const id = useId();
  const classes = [
    "gs-text-field",
    error ? "gs-text-field--error" : "",
    disabled ? "gs-text-field--disabled" : "",
    value !== "" ? "gs-text-field--populated" : "",
  ]
    .filter(Boolean)
    .join(" ");
  return (
    <div className={classes}>
      <div className="gs-text-field__box">
        <input
          id={id}
          type={type}
          value={value}
          placeholder={placeholder}
          disabled={disabled}
          onChange={(e) => onChange?.(e.target.value)}
        />
        <label htmlFor={id}>{label}</label>
        {trailing && <span className="gs-text-field__trailing">{trailing}</span>}
      </div>
      {supportingText && <div className="gs-text-field__supporting">{supportingText}</div>}
    </div>
  );
}
