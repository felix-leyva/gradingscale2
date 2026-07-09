import { useId } from "react";

export interface SelectOption {
  value: string;
  label: string;
}

export interface SelectProps {
  /** Floating label above the field. */
  label: string;
  options: SelectOption[];
  value: string;
  onChange?: (value: string) => void;
  disabled?: boolean;
}

/**
 * Exposed dropdown menu styled like the outlined text field —
 * the DropboxSelector / AdaptiveGradeScaleSelector equivalent.
 * Native <select> underneath, so it needs no popup positioning.
 */
export function Select({ label, options, value, onChange, disabled }: SelectProps) {
  const id = useId();
  return (
    <div className={`gs-select${disabled ? " gs-select--disabled" : ""}`}>
      <div className="gs-select__box">
        <select id={id} value={value} disabled={disabled} onChange={(e) => onChange?.(e.target.value)}>
          {options.map((o) => (
            <option key={o.value} value={o.value}>
              {o.label}
            </option>
          ))}
        </select>
        <label htmlFor={id}>{label}</label>
        <span className="gs-select__arrow" aria-hidden="true">
          <svg width="10" height="6" viewBox="0 0 10 6">
            <path d="M1 1l4 4 4-4" fill="none" stroke="currentColor" strokeWidth="1.5" />
          </svg>
        </span>
      </div>
    </div>
  );
}
