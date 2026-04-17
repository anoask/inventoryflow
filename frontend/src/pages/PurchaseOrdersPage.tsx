import { useCallback, useEffect, useState } from 'react'
import { getErrorMessage } from '../api/errors'
import { productsApi } from '../api/products'
import { purchaseOrdersApi } from '../api/purchaseOrders'
import { suppliersApi } from '../api/suppliers'
import type { PurchaseOrderItemCreateRequest } from '../types/purchaseOrder'
import type { PurchaseOrderResponse } from '../types/purchaseOrder'
import type { ProductResponse } from '../types/product'
import type { SupplierResponse } from '../types/supplier'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { LoadingState } from '../components/LoadingState'
import { EmptyState } from '../components/EmptyState'
import { StatusBadge } from '../components/StatusBadge'
import { ConfirmDialog } from '../components/ConfirmDialog'

type Line = {
  key: string
  productId: number
  quantity: number
  unitPriceStr: string
}

function newLine(): Line {
  return {
    key: `${Date.now()}-${Math.random().toString(36).slice(2)}`,
    productId: 0,
    quantity: 1,
    unitPriceStr: '',
  }
}

const CATALOG_PAGE = { page: 0, size: 200, sort: 'sku,asc' as const }
const SUPPLIER_PAGE = { page: 0, size: 200, sort: 'name,asc' as const }

export function PurchaseOrdersPage() {
  const { isAdmin } = useAuth()
  const toast = useToast()
  const [orders, setOrders] = useState<PurchaseOrderResponse[]>([])
  const [suppliers, setSuppliers] = useState<SupplierResponse[]>([])
  const [products, setProducts] = useState<ProductResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [busy, setBusy] = useState(false)
  const [supplierId, setSupplierId] = useState(0)
  const [lines, setLines] = useState<Line[]>([newLine()])
  const [receiveTarget, setReceiveTarget] = useState<PurchaseOrderResponse | null>(
    null
  )

  const load = useCallback(async () => {
    if (!isAdmin) return
    setLoading(true)
    setError(null)
    try {
      const [o, s, p] = await Promise.all([
        purchaseOrdersApi.list(),
        suppliersApi.list(SUPPLIER_PAGE),
        productsApi.list(CATALOG_PAGE),
      ])
      setOrders(o)
      setSuppliers(s.content)
      setProducts(p.content)
      setSupplierId((id) => id || s.content[0]?.id || 0)
    } catch (e) {
      const msg = getErrorMessage(e)
      setError(msg)
      toast.push(msg, 'error')
    } finally {
      setLoading(false)
    }
  }, [isAdmin, toast])

  useEffect(() => {
    void load()
  }, [load])

  function validateLines(): string | null {
    if (!supplierId) return 'Choose a supplier.'
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
      setError(v)
      toast.push(v, 'error')
      return
    }
    setBusy(true)
    setError(null)
    try {
      const items: PurchaseOrderItemCreateRequest[] = lines.map((line) => {
        const row: PurchaseOrderItemCreateRequest = {
          productId: line.productId,
          quantity: line.quantity,
        }
        if (line.unitPriceStr.trim() !== '') {
          const n = Number(line.unitPriceStr)
          if (!Number.isNaN(n)) row.unitPrice = n
        }
        return row
      })
      await purchaseOrdersApi.create({ supplierId, items })
      setLines([newLine()])
      toast.push('Purchase order created', 'success')
      await load()
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      toast.push(msg, 'error')
    } finally {
      setBusy(false)
    }
  }

  async function confirmReceive() {
    if (!receiveTarget) return
    setBusy(true)
    setError(null)
    try {
      await purchaseOrdersApi.receive(receiveTarget.id)
      toast.push(`PO #${receiveTarget.id} marked received`, 'success')
      setReceiveTarget(null)
      await load()
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      toast.push(msg, 'error')
    } finally {
      setBusy(false)
    }
  }

  if (!isAdmin) {
    return (
      <div className="stack">
        <h1 className="page-title">Purchase orders</h1>
        <div className="alert alert-info">
          Purchase orders are restricted to <strong>ADMIN</strong> in the API.
          Sign in as an admin user to use this page.
        </div>
      </div>
    )
  }

  if (loading) return <LoadingState />

  return (
    <div className="stack">
      <h1 className="page-title">Purchase orders</h1>
      {error ? <div className="alert alert-error">{error}</div> : null}

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Create purchase order</h2>
        <form onSubmit={onCreate} className="stack">
          <div className="field" style={{ maxWidth: 320 }}>
            <label>Supplier</label>
            <select
              value={supplierId || ''}
              onChange={(e) => setSupplierId(Number(e.target.value))}
              required
            >
              {suppliers.map((s) => (
                <option key={s.id} value={s.id}>
                  {s.name}
                </option>
              ))}
            </select>
          </div>
          <div>
            <div className="muted" style={{ marginBottom: '0.35rem' }}>
              Line items
            </div>
            {lines.map((line, idx) => (
              <div
                key={line.key}
                className="row-actions"
                style={{ marginBottom: '0.5rem', alignItems: 'flex-end' }}
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
                        {p.sku} — {p.name}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="field" style={{ flex: 1, marginBottom: 0 }}>
                  <label>Qty</label>
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
                <div className="field" style={{ flex: 1, marginBottom: 0 }}>
                  <label>Unit price (optional)</label>
                  <input
                    type="number"
                    step="0.01"
                    min={0}
                    value={line.unitPriceStr}
                    onChange={(e) => {
                      const raw = e.target.value
                      setLines((ls) =>
                        ls.map((l, i) =>
                          i === idx ? { ...l, unitPriceStr: raw } : l
                        )
                      )
                    }}
                  />
                </div>
                <button
                  type="button"
                  className="btn btn-ghost"
                  onClick={() =>
                    setLines((ls) => ls.filter((_, i) => i !== idx))
                  }
                  disabled={lines.length <= 1}
                >
                  Remove
                </button>
              </div>
            ))}
            <button
              type="button"
              className="btn btn-ghost"
              onClick={() => setLines((ls) => [...ls, newLine()])}
            >
              Add line
            </button>
          </div>
          <button type="submit" className="btn btn-primary" disabled={busy}>
            Create PO
          </button>
        </form>
      </div>

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>All orders</h2>
        {orders.length === 0 ? (
          <EmptyState
            title="No purchase orders yet"
            hint="Create a PO above to restock products."
          />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Status</th>
                  <th>Supplier</th>
                  <th>Created</th>
                  <th>Items</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {orders.map((o) => (
                  <tr key={o.id}>
                    <td>{o.id}</td>
                    <td>
                      <StatusBadge status={o.status} />
                    </td>
                    <td>{o.supplierName}</td>
                    <td>{new Date(o.createdAt).toLocaleString()}</td>
                    <td>
                      {o.items.map((i) => (
                        <div key={`${o.id}-${i.productId}-${i.sku}`} className="muted">
                          {i.sku} × {i.quantity}
                        </div>
                      ))}
                    </td>
                    <td>
                      {o.status === 'CREATED' ? (
                        <button
                          type="button"
                          className="btn btn-primary"
                          disabled={busy}
                          onClick={() => setReceiveTarget(o)}
                        >
                          Mark received
                        </button>
                      ) : (
                        <span className="muted">—</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <ConfirmDialog
        open={receiveTarget != null}
        title="Mark purchase order received?"
        description={
          receiveTarget
            ? `PO #${receiveTarget.id} (${receiveTarget.supplierName}) will update inventory.`
            : ''
        }
        confirmLabel="Mark received"
        loading={busy}
        onCancel={() => setReceiveTarget(null)}
        onConfirm={() => void confirmReceive()}
      />
    </div>
  )
}
