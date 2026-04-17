export function EmptyState({
  title,
  hint,
}: {
  title: string
  hint?: string
}) {
  return (
    <div className="empty-state">
      <p className="empty-state-title">{title}</p>
      {hint ? <p className="muted" style={{ margin: 0 }}>{hint}</p> : null}
    </div>
  )
}
