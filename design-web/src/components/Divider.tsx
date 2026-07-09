export interface DividerProps {
  orientation?: "horizontal" | "vertical";
  /** Indent from the leading edge to align with list content. */
  inset?: boolean;
}

/** M3 divider; vertical mirrors the app's VerticalDivider. */
export function Divider({ orientation = "horizontal", inset = false }: DividerProps) {
  return (
    <hr
      className={`gs-divider gs-divider--${orientation}${inset ? " gs-divider--inset" : ""}`}
      aria-orientation={orientation}
    />
  );
}
