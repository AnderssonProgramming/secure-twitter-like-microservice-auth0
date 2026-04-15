export default function LoadingSpinner({ size = 8 }) {
  const px = size * 4;

  return (
    <div
      className="border-4 border-twitter-border border-t-twitter-blue rounded-full animate-spin"
      style={{ width: `${px}px`, height: `${px}px` }}
      role="status"
      aria-label="Loading"
    />
  );
}
