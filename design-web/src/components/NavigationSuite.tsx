import { ReactNode } from "react";

export interface NavigationItem {
  key: string;
  label: string;
  /** Icon element (inline SVG, 24x24). */
  icon?: ReactNode;
}

export interface NavigationSuiteProps {
  items: NavigationItem[];
  selectedKey: string;
  onSelect?: (key: string) => void;
  /**
   * "bar" = horizontal bottom bar, "rail" = vertical side rail,
   * "auto" (default) = bar below 600px viewport width, rail above —
   * mirrors the app's adaptive AppNavigationSuite.
   */
  layout?: "bar" | "rail" | "auto";
}

/** Adaptive navigation: M3 navigation bar / rail. */
export function NavigationSuite({ items, selectedKey, onSelect, layout = "auto" }: NavigationSuiteProps) {
  return (
    <nav className={`gs-nav gs-nav--${layout}`}>
      {items.map((item) => (
        <button
          key={item.key}
          className={`gs-nav__item${item.key === selectedKey ? " gs-nav__item--selected" : ""}`}
          onClick={() => onSelect?.(item.key)}
          aria-current={item.key === selectedKey ? "page" : undefined}
        >
          {item.icon && <span className="gs-nav__indicator">{item.icon}</span>}
          <span className="gs-nav__label">{item.label}</span>
        </button>
      ))}
    </nav>
  );
}
