import { useCallback, useEffect, useState } from 'react'
import { getErrorMessage } from '../api/errors'
import { productsApi } from '../api/products'
import { suppliersApi } from '../api/suppliers'
import type { ProductCreateRequest, ProductResponse } from '../types/product'
import type { SupplierResponse } from '../types/supplier'
import type { PageDto } from '../types/common'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { LoadingState } from '../components/LoadingState'
import { EmptyState } from '../components/EmptyState'
import { ConfirmDialog } from '../components/ConfirmDialog'

const emptyCreate: ProductCreateRequest = {
  sku: '',
  name: '',
  description: '',
  category: '',
  price: 0,
  quantityInStock: 0,
  reorderLevel: 0,
  supplierId: 0,
}

const PAGE_SIZE = 12

export function ProductsPage() {
  const { isAdmin } = useAuth()
  const toast = useToast()
  const [suppliers, setSuppliers] = useState<SupplierResponse[]>([])
  const [page, setPage] = useState(0)
  const [sort, setSort] = useState('sku,asc')
  const [searchInput, setSearchInput] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [pageData, setPageData] = useState<PageDto<ProductResponse> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [createForm, setCreateForm] = useState<ProductCreateRequest>(emptyCreate)
  const [editId, setEditId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState<ProductCreateRequest>(emptyCreate)
  const [busy, setBusy] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<ProductResponse | null>(null)

  useEffect(() => {
    const t = window.setTimeout(() => setDebouncedSearch(searchInput.trim()), 350)
    return () => window.clearTimeout(t)
  }, [searchInput])

  useEffect(() => {
    setPage(0)
  }, [debouncedSearch, sort])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      try {
        const res = await suppliersApi.list({ page: 0, size: 200, sort: 'name,asc' })
        if (cancelled) return
        setSuppliers(res.content)
        setCreateForm((f) => ({
          ...f,
          supplierId: f.supplierId || res.content[0]?.id || 0,
        }))
      } catch {
        /* non-fatal for catalog */
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const loadProducts = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await productsApi.list({
        page,
        size: PAGE_SIZE,
        search: debouncedSearch || undefined,
        sort,
      })
      setPageData(data)
    } catch (e) {
      setError(getErrorMessage(e))
    } finally {
      setLoading(false)
    }
  }, [page, debouncedSearch, sort])

  useEffect(() => {
    void loadProducts()
  }, [loadProducts])

  function startEdit(p: ProductResponse) {
    setEditId(p.id)
    setEditForm({
      sku: p.sku,
      name: p.name,
      description: p.description ?? '',
      category: p.category,
      price: Number(p.price),
      quantityInStock: p.quantityInStock,
      reorderLevel: p.reorderLevel,
      supplierId: p.supplierId,
    })
  }

  function cancelEdit() {
    setEditId(null)
  }

  async function onCreate(e: React.FormEvent) {
    e.preventDefault()
    if (createForm.sku.trim().length < 1) {
      toast.push('SKU is required', 'error')
      return
    }
    setBusy(true)
    setError(null)
    try {
      await productsApi.create({
        ...createForm,
        price: Number(createForm.price),
        supplierId: Number(createForm.supplierId),
      })
      setCreateForm({ ...emptyCreate, supplierId: suppliers[0]?.id ?? 0 })
      toast.push('Product created', 'success')
      setPage(0)
      await loadProducts()
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      toast.push(msg, 'error')
    } finally {
      setBusy(false)
    }
  }

  async function onUpdate(e: React.FormEvent) {
    e.preventDefault()
    if (editId == null) return
    setBusy(true)
    setError(null)
    try {
      await productsApi.update(editId, {
        ...editForm,
        price: Number(editForm.price),
        supplierId: Number(editForm.supplierId),
      })
      setEditId(null)
      toast.push('Product updated', 'success')
      await loadProducts()
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      toast.push(msg, 'error')
    } finally {
      setBusy(false)
    }
  }

  async function confirmDelete() {
    if (!deleteTarget) return
    setBusy(true)
    setError(null)
    try {
      await productsApi.delete(deleteTarget.id)
      toast.push('Product deleted', 'success')
      setDeleteTarget(null)
      await loadProducts()
    } catch (err) {
      const msg = getErrorMessage(err)
      setError(msg)
      toast.push(msg, 'error')
    } finally {
      setBusy(false)
    }
  }

  const rows = pageData?.content ?? []
  const totalPages = pageData?.totalPages ?? 0

  if (loading && !pageData) return <LoadingState />

  return (
    <div className="stack">
      <h1 className="page-title">Products</h1>
      {error ? <div className="alert alert-error">{error}</div> : null}

      <div className="toolbar card" style={{ padding: '0.85rem 1rem' }}>
        <div className="field">
          <label htmlFor="product-search">Search</label>
          <input
            id="product-search"
            placeholder="SKU, name, or category"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="product-sort">Sort</label>
          <select
            id="product-sort"
            value={sort}
            onChange={(e) => setSort(e.target.value)}
          >
            <option value="sku,asc">SKU (A–Z)</option>
            <option value="sku,desc">SKU (Z–A)</option>
            <option value="name,asc">Name (A–Z)</option>
            <option value="price,asc">Price (low–high)</option>
            <option value="price,desc">Price (high–low)</option>
            <option value="quantityInStock,asc">Stock (low–high)</option>
            <option value="quantityInStock,desc">Stock (high–low)</option>
          </select>
        </div>
      </div>

      {isAdmin ? (
        <div className="card">
          <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Create product</h2>
          <form onSubmit={onCreate} className="grid-2">
            <div className="field">
              <label>SKU</label>
              <input
                value={createForm.sku}
                onChange={(e) =>
                  setCreateForm((f) => ({ ...f, sku: e.target.value }))
                }
                required
                minLength={1}
              />
            </div>
            <div className="field">
              <label>Name</label>
              <input
                value={createForm.name}
                onChange={(e) =>
                  setCreateForm((f) => ({ ...f, name: e.target.value }))
                }
                required
              />
            </div>
            <div className="field" style={{ gridColumn: '1 / -1' }}>
              <label>Description</label>
              <textarea
                rows={2}
                value={createForm.description}
                onChange={(e) =>
                  setCreateForm((f) => ({ ...f, description: e.target.value }))
                }
              />
            </div>
            <div className="field">
              <label>Category</label>
              <input
                value={createForm.category}
                onChange={(e) =>
                  setCreateForm((f) => ({ ...f, category: e.target.value }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Price</label>
              <input
                type="number"
                step="0.01"
                min={0}
                value={createForm.price || ''}
                onChange={(e) =>
                  setCreateForm((f) => ({
                    ...f,
                    price: e.target.value === '' ? 0 : Number(e.target.value),
                  }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Quantity in stock</label>
              <input
                type="number"
                min={0}
                value={createForm.quantityInStock}
                onChange={(e) =>
                  setCreateForm((f) => ({
                    ...f,
                    quantityInStock: Number(e.target.value),
                  }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Reorder level</label>
              <input
                type="number"
                min={0}
                value={createForm.reorderLevel}
                onChange={(e) =>
                  setCreateForm((f) => ({
                    ...f,
                    reorderLevel: Number(e.target.value),
                  }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Supplier</label>
              <select
                value={createForm.supplierId || ''}
                onChange={(e) =>
                  setCreateForm((f) => ({
                    ...f,
                    supplierId: Number(e.target.value),
                  }))
                }
                required
              >
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <div style={{ alignSelf: 'end' }}>
              <button type="submit" className="btn btn-primary" disabled={busy}>
                {busy ? 'Saving…' : 'Create'}
              </button>
            </div>
          </form>
        </div>
      ) : (
        <div className="alert alert-info">
          View-only for <strong>STAFF</strong>. Create, edit, and delete require{' '}
          <strong>ADMIN</strong>.
        </div>
      )}

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Catalog</h2>
        {loading ? <LoadingState label="Refreshing…" /> : null}
        {!loading && rows.length === 0 ? (
          <EmptyState
            title="No products match"
            hint="Try clearing search or create a new product."
          />
        ) : (
          <>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>SKU</th>
                    <th>Name</th>
                    <th>Category</th>
                    <th className="hide-sm">Price</th>
                    <th>Stock</th>
                    <th className="hide-sm">Reorder</th>
                    <th>Supplier</th>
                    {isAdmin ? <th style={{ width: '9rem' }} /> : null}
                  </tr>
                </thead>
                <tbody>
                  {rows.map((p) => (
                    <tr key={p.id}>
                      <td>
                        <strong>{p.sku}</strong>
                      </td>
                      <td>{p.name}</td>
                      <td>{p.category}</td>
                      <td className="hide-sm">{p.price}</td>
                      <td>{p.quantityInStock}</td>
                      <td className="hide-sm">{p.reorderLevel}</td>
                      <td>{p.supplierName}</td>
                      {isAdmin ? (
                        <td>
                          <div className="row-actions">
                            <button
                              type="button"
                              className="btn btn-ghost"
                              onClick={() => startEdit(p)}
                              disabled={busy}
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              className="btn btn-danger"
                              onClick={() => setDeleteTarget(p)}
                              disabled={busy}
                            >
                              Delete
                            </button>
                          </div>
                        </td>
                      ) : null}
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            {totalPages > 1 ? (
              <div className="pagination">
                <button
                  type="button"
                  className="btn btn-ghost"
                  disabled={page <= 0 || busy}
                  onClick={() => setPage((p) => Math.max(0, p - 1))}
                >
                  Previous
                </button>
                <span className="muted">
                  Page {page + 1} of {totalPages} ({pageData?.totalElements ?? 0} total)
                </span>
                <button
                  type="button"
                  className="btn btn-ghost"
                  disabled={page >= totalPages - 1 || busy}
                  onClick={() => setPage((p) => p + 1)}
                >
                  Next
                </button>
              </div>
            ) : null}
          </>
        )}
      </div>

      {isAdmin && editId != null ? (
        <div className="card">
          <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>
            Edit product #{editId}
          </h2>
          <form onSubmit={onUpdate} className="grid-2">
            <div className="field">
              <label>SKU</label>
              <input
                value={editForm.sku}
                onChange={(e) =>
                  setEditForm((f) => ({ ...f, sku: e.target.value }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Name</label>
              <input
                value={editForm.name}
                onChange={(e) =>
                  setEditForm((f) => ({ ...f, name: e.target.value }))
                }
                required
              />
            </div>
            <div className="field" style={{ gridColumn: '1 / -1' }}>
              <label>Description</label>
              <textarea
                rows={2}
                value={editForm.description}
                onChange={(e) =>
                  setEditForm((f) => ({ ...f, description: e.target.value }))
                }
              />
            </div>
            <div className="field">
              <label>Category</label>
              <input
                value={editForm.category}
                onChange={(e) =>
                  setEditForm((f) => ({ ...f, category: e.target.value }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Price</label>
              <input
                type="number"
                step="0.01"
                min={0}
                value={editForm.price || ''}
                onChange={(e) =>
                  setEditForm((f) => ({
                    ...f,
                    price: e.target.value === '' ? 0 : Number(e.target.value),
                  }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Quantity in stock</label>
              <input
                type="number"
                min={0}
                value={editForm.quantityInStock}
                onChange={(e) =>
                  setEditForm((f) => ({
                    ...f,
                    quantityInStock: Number(e.target.value),
                  }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Reorder level</label>
              <input
                type="number"
                min={0}
                value={editForm.reorderLevel}
                onChange={(e) =>
                  setEditForm((f) => ({
                    ...f,
                    reorderLevel: Number(e.target.value),
                  }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Supplier</label>
              <select
                value={editForm.supplierId}
                onChange={(e) =>
                  setEditForm((f) => ({
                    ...f,
                    supplierId: Number(e.target.value),
                  }))
                }
                required
              >
                {suppliers.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="row-actions" style={{ alignSelf: 'end' }}>
              <button type="submit" className="btn btn-primary" disabled={busy}>
                {busy ? 'Saving…' : 'Save changes'}
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={cancelEdit}
                disabled={busy}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      ) : null}

      <ConfirmDialog
        open={deleteTarget != null}
        title="Delete product?"
        description={
          deleteTarget
            ? `Remove ${deleteTarget.sku} — ${deleteTarget.name}? This cannot be undone.`
            : ''
        }
        confirmLabel="Delete"
        danger
        loading={busy}
        onCancel={() => setDeleteTarget(null)}
        onConfirm={() => void confirmDelete()}
      />
    </div>
  )
}
