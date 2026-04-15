export default function LoadingSpinner({ size = 8 }) {
  return (
    <div
      className={`w-${size} h-${size} border-4 border-twitter-border border-t-twitter-blue
                  rounded-full animate-spin`}
      role="status"
      aria-label="Loading"
    />
  );
}
