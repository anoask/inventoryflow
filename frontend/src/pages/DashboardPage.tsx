import { useEffect, useState } from 'react'
import { dashboardApi } from '../api/dashboard'
import { productsApi } from '../api/products'
import { suppliersApi } from '../api/suppliers'
import { getErrorMessage } from '../api/errors'
import type { LowStockProductResponse } from '../types/dashboard'
import type { RecentPurchaseOrderDto, RecentSalesOrderDto } from '../types/dashboard'
import { LoadingState } from '../components/LoadingState'
import { EmptyState } from '../components/EmptyState'
import { StatusBadge } from '../components/StatusBadge'
import { useAuth } from '../hooks/useAuth'

export function DashboardPage() {
  const { isAdmin } = useAuth()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [productCount, setProductCount] = useState(0)
  const [supplierCount, setSupplierCount] = useState(0)
  const [lowStock, setLowStock] = useState<LowStockProductResponse[]>([])
  const [recentPo, setRecentPo] = useState<RecentPurchaseOrderDto[]>([])
  const [recentSo, setRecentSo] = useState<RecentSalesOrderDto[]>([])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const [pMeta, sMeta, low] = await Promise.all([
          productsApi.list({ page: 0, size: 1 }),
          suppliersApi.list({ page: 0, size: 1 }),
          dashboardApi.lowStock(),
        ])
        if (cancelled) return

        setProductCount(pMeta.totalElements)
        setSupplierCount(sMeta.totalElements)
        setLowStock(low)

        const so = await dashboardApi.recentSalesOrders()
        if (cancelled) return
        setRecentSo(so)

        if (isAdmin) {
          const po = await dashboardApi.recentPurchaseOrders()
          if (!cancelled) setRecentPo(po)
        } else {
          setRecentPo([])
        }
      } catch (e) {
        if (!cancelled) setError(getErrorMessage(e))
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [isAdmin])

  if (loading) return <LoadingState />

  return (
    <div className="stack">
      <h1 className="page-title">Dashboard</h1>
      {error ? <div className="alert alert-error">{error}</div> : null}

      <div className="grid-2">
        <div className="card stat-card">
          <span className="stat-label">Products</span>
          <span className="stat-value">{productCount}</span>
        </div>
        <div className="card stat-card">
          <span className="stat-label">Suppliers</span>
          <span className="stat-value">{supplierCount}</span>
        </div>
        <div className="card stat-card">
          <span className="stat-label">Low-stock SKUs</span>
          <span className="stat-value">{lowStock.length}</span>
        </div>
      </div>

      <div className="grid-2">
        {isAdmin ? (
          <div className="card">
            <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Recent purchase orders</h2>
            {recentPo.length === 0 ? (
              <EmptyState title="No purchase orders yet" hint="Create one from Purchase orders." />
            ) : (
              <div className="table-wrap">
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Status</th>
                      <th>Supplier</th>
                      <th>Lines</th>
                      <th>When</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentPo.map((o) => (
                      <tr key={o.id}>
                        <td>{o.id}</td>
                        <td>
                          <StatusBadge status={o.status} />
                        </td>
                        <td>{o.supplierName}</td>
                        <td>{o.lineItemCount}</td>
                        <td className="muted">
                          {new Date(o.createdAt).toLocaleString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        ) : null}

        <div className="card">
          <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Recent sales orders</h2>
          {recentSo.length === 0 ? (
            <EmptyState title="No sales orders yet" />
          ) : (
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>ID</th>
                    <th>Status</th>
                    <th>Lines</th>
                    <th>When</th>
                  </tr>
                </thead>
                <tbody>
                  {recentSo.map((o) => (
                    <tr key={o.id}>
                      <td>{o.id}</td>
                      <td>
                        <StatusBadge status={o.status} />
                      </td>
                      <td>{o.lineItemCount}</td>
                      <td className="muted">
                        {new Date(o.createdAt).toLocaleString()}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Low-stock products</h2>
        {lowStock.length === 0 ? (
          <EmptyState
            title="All clear"
            hint="No products are currently below their reorder level."
          />
        ) : (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>SKU</th>
                  <th>Name</th>
                  <th>Category</th>
                  <th>Stock</th>
                  <th>Reorder</th>
                  <th>Supplier</th>
                </tr>
              </thead>
              <tbody>
                {lowStock.map((p) => (
                  <tr key={p.id}>
                    <td>
                      <strong>{p.sku}</strong>
                    </td>
                    <td>{p.name}</td>
                    <td>{p.category}</td>
                    <td>
                      <span style={{ color: '#b45309', fontWeight: 600 }}>
                        {p.quantityInStock}
                      </span>
                    </td>
                    <td>{p.reorderLevel}</td>
                    <td>{p.supplierName}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}
