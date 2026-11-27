export function ErrorNote({ error }) {
  if (!error) return null;
  return (
    <p className="error-note" role="alert">
      {error.message}
    </p>
  );
}

export function Notice({ children }) {
  if (!children) return null;
  return (
    <p className="notice" role="status">
      {children}
    </p>
  );
}

export function Field({ label, hint, children }) {
  return (
    <label className="field">
      <span className="field__label">{label}</span>
      {children}
      {hint && <span className="field__hint">{hint}</span>}
    </label>
  );
}

export function Loading() {
  return <p className="muted">불러오는 중…</p>;
}

export function Empty({ children }) {
  return <p className="empty">{children}</p>;
}
