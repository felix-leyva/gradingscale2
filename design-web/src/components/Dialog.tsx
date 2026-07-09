import { ReactNode } from "react";
import { Button } from "./Button";

export interface DialogProps {
  open: boolean;
  title: string;
  children?: ReactNode;
  /** Confirming action label (e.g. "Save"). Rendered as a text button. */
  confirmLabel?: string;
  /** Dismissing action label (e.g. "Cancel"). Rendered as a text button. */
  dismissLabel?: string;
  onConfirm?: () => void;
  /** Called on scrim click and on the dismiss action. */
  onClose: () => void;
}

/** M3 basic dialog, mirrors the app's Upsert/Edit dialogs. */
export function Dialog({ open, title, children, confirmLabel, dismissLabel, onConfirm, onClose }: DialogProps) {
  if (!open) return null;
  return (
    <div className="gs-dialog__scrim" onClick={onClose}>
      <div className="gs-dialog" role="dialog" aria-modal="true" aria-label={title} onClick={(e) => e.stopPropagation()}>
        <h2 className="gs-dialog__title">{title}</h2>
        {children && <div className="gs-dialog__content">{children}</div>}
        {(confirmLabel || dismissLabel) && (
          <div className="gs-dialog__actions">
            {dismissLabel && (
              <Button variant="text" onClick={onClose}>
                {dismissLabel}
              </Button>
            )}
            {confirmLabel && (
              <Button variant="text" onClick={onConfirm}>
                {confirmLabel}
              </Button>
            )}
          </div>
        )}
      </div>
    </div>
  );
}
