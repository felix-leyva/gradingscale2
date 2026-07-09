import { ReactNode } from "react";

export interface ScaffoldProps {
  /** Top app bar title. */
  title: string;
  /** Elements on the right side of the top bar (icon buttons, menus). */
  topBarActions?: ReactNode;
  /** A NavigationSuite element; sits at the bottom on compact widths, left rail from 600px. */
  navigation?: ReactNode;
  /** Floating action button element, pinned bottom-right. */
  fab?: ReactNode;
  /** A Snackbar element. */
  snackbar?: ReactNode;
  children: ReactNode;
}

/**
 * App shell mirroring PersistentScaffold: blurred top app bar,
 * scrollable content on the app background, adaptive navigation slot.
 */
export function Scaffold({ title, topBarActions, navigation, fab, snackbar, children }: ScaffoldProps) {
  return (
    <div className="gs-scaffold">
      {navigation && <div className="gs-scaffold__nav">{navigation}</div>}
      <div className="gs-scaffold__body">
        <header className="gs-scaffold__top-bar">
          <h1 className="gs-scaffold__title">{title}</h1>
          {topBarActions && <div className="gs-scaffold__actions">{topBarActions}</div>}
        </header>
        <main className="gs-scaffold__content">{children}</main>
      </div>
      {fab && <div className="gs-scaffold__fab">{fab}</div>}
      {snackbar}
    </div>
  );
}
