export interface SnackbarProps {
  open: boolean;
  message: string;
  actionLabel?: string;
  onAction?: () => void;
}

/** M3 snackbar, mirrors the app's PersistentSnackbarHost messages. */
export function Snackbar({ open, message, actionLabel, onAction }: SnackbarProps) {
  if (!open) return null;
  return (
    <div className="gs-snackbar" role="status">
      <span className="gs-snackbar__message">{message}</span>
      {actionLabel && (
        <button className="gs-snackbar__action" onClick={onAction}>
          {actionLabel}
        </button>
      )}
    </div>
  );
}
