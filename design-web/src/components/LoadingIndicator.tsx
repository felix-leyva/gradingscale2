export interface LoadingIndicatorProps {
  /** Diameter in px. Default 40. */
  size?: number;
}

/** Circular progress spinner, mirrors the app's LoadingContent state. */
export function LoadingIndicator({ size = 40 }: LoadingIndicatorProps) {
  return (
    <span
      className="gs-loading"
      style={{ width: size, height: size }}
      role="progressbar"
      aria-label="Loading"
    />
  );
}
