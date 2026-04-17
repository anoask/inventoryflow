const STATUS_CLASS: Record<string, string> = {
  CREATED: 'status-created',
  RECEIVED: 'status-received',
  COMPLETED: 'status-completed',
  CANCELLED: 'status-cancelled',
}

export function StatusBadge({ status }: { status: string }) {
  const cls = STATUS_CLASS[status] ?? 'status-default'
  return <span className={`status-badge ${cls}`}>{status}</span>
}
