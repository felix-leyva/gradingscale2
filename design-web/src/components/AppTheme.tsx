import { ReactNode } from "react";

export interface AppThemeProps {
  /** "light" | "dark" force a scheme; "system" (default) follows the OS / host theme. */
  theme?: "light" | "dark" | "system";
  children: ReactNode;
}

/**
 * Root theme wrapper. Wrap every page in this — it applies the app
 * background, text color and base typography, and scopes the color
 * scheme via `data-theme`.
 */
export function AppTheme({ theme = "system", children }: AppThemeProps) {
  return (
    <div className="gs-root" data-theme={theme === "system" ? undefined : theme}>
      {children}
    </div>
  );
}
