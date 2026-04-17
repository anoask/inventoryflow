import { useCallback, useEffect, useState } from 'react'
import { getErrorMessage } from '../api/errors'
import { productsApi } from '../api/products'
import { salesOrdersApi } from '../api/salesOrders'
import type { SalesOrderItemCreateRequest } from '../types/salesOrder'
import type { SalesOrderResponse } from '../types/salesOrder'
import type { ProductResponse } from '../types/product'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { LoadingState } from '../components/LoadingState'
import { EmptyState } from '../components/EmptyState'
import { StatusBadge } from '../components/StatusBadge'

type Line = { key: string; productId: number; quantity: number }

function newLine(): Line {
  return {
    key: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    productId: 0,
    quantity: 1,
  }
}

const CATALOG_PAGE = { page: 0, size: 200, sort: 'sku,asc' as const }

export function SalesOrdersPage() {
  const { isAdmin } = useAuth()
  const toast = useToast()
  const [orders, setOrders] = useState<SalesOrderResponse[]>([])
  const [products, setProducts] = useState<ProductResponse[]>([])
  const [listLoading, setListLoading] = useState(false)
  const [metaLoading, setMetaLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [createError, setCreateError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [lines, setLines] = useState<Line[]>([newLine()])

  const loadProducts = useCallback(async () => {
    setMetaLoading(true)
    setError(null)
    try {
      const page = await productsApi.list(CATALOG_PAGE)
      setProducts(page.content)
    } catch (e) {
      const msg = getErrorMessage(e)
      setError(msg)
      toast.push(msg, 'error')
    } finally {
      setMetaLoading(false)
    }
  }, [toast])

  const loadOrders = useCallback(async () => {
    if (!isAdmin) return
    setListLoading(true)
    setError(null)
    try {
      setOrders(await salesOrdersApi.list())
    } catch (e) {
      const msg = getErrorMessage(e)
      setError(msg)
      toast.push(msg, 'error')
    } finally {
      setListLoading(false)
    }
  }, [isAdmin, toast])

  useEffect(() => {
    void loadProducts()
  }, [loadProducts])

  useEffect(() => {
    void loadOrders()
  }, [loadOrders])

  function validateLines(): string | null {
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      if (!line.productId) return `Line ${i + 1}: select a product.`
      if (!Number.isFinite(line.quantity) || line.quantity < 1) {
        return `Line ${i + 1}: quantity must be at least 1.`
      }
    }
    return null
  }

  async function onCreate(e: React.FormEvent) {
    e.preventDefault()
    const v = validateLines()
    if (v) {
      setCreateError(v)
      toast.push(v, 'error')
      return
    }
    setBusy(true)
    setCreateError(null)
    try {
      const items: SalesOrderItemCreateRequest[] = lines.map((l) => ({
        productId: l.productId,
        quantity: l.quantity,
      }))
      await salesOrdersApi.create({ items })
      setLines([newLine()])
      toast.push('Sales order submitted', 'success')
      if (isAdmin) await loadOrders()
    } catch (err) {
      const msg = getErrorMessage(err)
      setCreateError(msg)
      toast.push(msg, 'error')
    } finally {
      setBusy(false)
    }
  }

  if (metaLoading) return <LoadingState />

  return (
    <div className="stack">
      <h1 className="page-title">Sales orders</h1>
      {error ? <div className="alert alert-error">{error}</div> : null}

      {!isAdmin ? (
        <div className="alert alert-info">
          <strong>STAFF</strong> can create sales orders. Listing existing orders
          requires <strong>ADMIN</strong> (backend restriction).
        </div>
      ) : null}

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Create sales order</h2>
        {createError ? (
          <div className="alert alert-error" role="alert">
            {createError}
          </div>
        ) : null}
        <p className="muted" style={{ marginTop: 0 }}>
          If quantity exceeds available stock, the API returns{' '}
          <strong>409 Conflict</strong> with a clear message.
        </p>
        <form onSubmit={onCreate} className="stack">
          {lines.map((line, idx) => (
            <div
              key={line.key}
              className="row-actions"
              style={{ alignItems: 'flex-end' }}
            >
              <div className="field" style={{ flex: 2, marginBottom: 0 }}>
                <label>Product</label>
                <select
                  value={line.productId || ''}
                  onChange={(e) => {
                    const v = Number(e.target.value)
                    setLines((ls) =>
                      ls.map((l, i) =>
                        i === idx ? { ...l, productId: v } : l
                      )
                    )
                  }}
                  required
                >
                  <option value="">Select…</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.sku} — stock {p.quantityInStock}
                    </option>
                  ))}
                </select>
              </div>
              <div className="field" style={{ flex: 1, marginBottom: 0 }}>
                <label>Quantity</label>
                <input
                  type="number"
                  min={1}
                  value={line.quantity}
                  onChange={(e) => {
                    const v = Number(e.target.value)
                    setLines((ls) =>
                      ls.map((l, i) =>
                        i === idx ? { ...l, quantity: v } : l
                      )
                    )
                  }}
                  required
                />
              </div>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setLines((ls) => ls.filter((_, i) => i !== idx))}
                disabled={lines.length <= 1}
              >
                Remove
              </button>
            </div>
          ))}
          <div className="row-actions">
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setLines((ls) => [...ls, newLine()])}
            >
              Add line
            </button>
            <button type="submit" className="btn btn-primary" disabled={busy}>
              Submit order
            </button>
          </div>
        </form>
      </div>

      {isAdmin ? (
        <div className="card">
          <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>All orders</h2>
          {listLoading ? (
            <LoadingState label="Loading orders…" />
          ) : orders.length === 0 ? (
            <EmptyState
              title="No sales orders yet"
              hint="Submitted orders from staff and admins appear here."
            />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Status</th>
                    <th>Created</th>
                    <th>Lines</th>
                  </tr>
                </thead>
                <tbody>
                  {orders.map((o) => (
                    <tr key={o.id}>
                      <td>{o.id}</td>
                      <td>
                        <StatusBadge status={o.status} />
                      </td>
                      <td>{new Date(o.createdAt).toLocaleString()}</td>
                      <td>
                        {o.items.map((i) => (
                          <div key={`${o.id}-${i.productId}-${i.sku}`}>
                            {i.sku} × {i.quantity} @ {i.unitPrice}
                          </div>
                        ))}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      ) : null}
    </div>
  )
}
