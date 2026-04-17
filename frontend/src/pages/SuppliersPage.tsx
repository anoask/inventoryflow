import { useCallback, useEffect, useState } from 'react'
import { getErrorMessage } from '../api/errors'
import { suppliersApi } from '../api/suppliers'
import type { SupplierCreateRequest, SupplierResponse } from '../types/supplier'
import type { PageDto } from '../types/common'
import { useAuth } from '../hooks/useAuth'
import { useToast } from '../hooks/useToast'
import { LoadingState } from '../components/LoadingState'
import { EmptyState } from '../components/EmptyState'
import { ConfirmDialog } from '../components/ConfirmDialog'

const empty: SupplierCreateRequest = {
  name: '',
  email: '',
  phone: '',
  address: '',
}

const PAGE_SIZE = 15

export function SuppliersPage() {
  const { isAdmin } = useAuth()
  const toast = useToast()
  const [page, setPage] = useState(0)
  const [sort, setSort] = useState('name,asc')
  const [searchInput, setSearchInput] = useState('')
  const [debouncedSearch, setDebouncedSearch] = useState('')
  const [pageData, setPageData] = useState<PageDto<SupplierResponse> | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [createForm, setCreateForm] = useState(empty)
  const [editId, setEditId] = useState<number | null>(null)
  const [editForm, setEditForm] = useState(empty)
  const [busy, setBusy] = useState(false)
  const [deleteTarget, setDeleteTarget] = useState<SupplierResponse | null>(null)

  useEffect(() => {
    const t = window.setTimeout(() => setDebouncedSearch(searchInput.trim()), 350)
    return () => window.clearTimeout(t)
  }, [searchInput])

  useEffect(() => {
    setPage(0)
  }, [debouncedSearch, sort])

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await suppliersApi.list({
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
    void load()
  }, [load])

  function startEdit(s: SupplierResponse) {
    setEditId(s.id)
    setEditForm({
      name: s.name,
      email: s.email,
      phone: s.phone ?? '',
      address: s.address ?? '',
    })
  }

  async function onCreate(e: React.FormEvent) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    try {
      await suppliersApi.create({
        ...createForm,
        phone: createForm.phone || undefined,
        address: createForm.address || undefined,
      })
      setCreateForm(empty)
      toast.push('Supplier created', 'success')
      setPage(0)
      await load()
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
      await suppliersApi.update(editId, {
        ...editForm,
        phone: editForm.phone || undefined,
        address: editForm.address || undefined,
      })
      setEditId(null)
      toast.push('Supplier updated', 'success')
      await load()
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
      await suppliersApi.delete(deleteTarget.id)
      toast.push('Supplier deleted', 'success')
      setDeleteTarget(null)
      await load()
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
      <h1 className="page-title">Suppliers</h1>
      {error ? <div className="alert alert-error">{error}</div> : null}

      <div className="toolbar card" style={{ padding: '0.85rem 1rem' }}>
        <div className="field">
          <label htmlFor="supplier-search">Search by name</label>
          <input
            id="supplier-search"
            placeholder="Type to filter…"
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
          />
        </div>
        <div className="field">
          <label htmlFor="supplier-sort">Sort</label>
          <select
            id="supplier-sort"
            value={sort}
            onChange={(e) => setSort(e.target.value)}
          >
            <option value="name,asc">Name (A–Z)</option>
            <option value="name,desc">Name (Z–A)</option>
            <option value="email,asc">Email (A–Z)</option>
          </select>
        </div>
      </div>

      {isAdmin ? (
        <div className="card">
          <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Create supplier</h2>
          <form onSubmit={onCreate} className="grid-2">
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
            <div className="field">
              <label>Email</label>
              <input
                type="email"
                value={createForm.email}
                onChange={(e) =>
                  setCreateForm((f) => ({ ...f, email: e.target.value }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Phone</label>
              <input
                value={createForm.phone}
                onChange={(e) =>
                  setCreateForm((f) => ({ ...f, phone: e.target.value }))
                }
              />
            </div>
            <div className="field">
              <label>Address</label>
              <input
                value={createForm.address}
                onChange={(e) =>
                  setCreateForm((f) => ({ ...f, address: e.target.value }))
                }
              />
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
          View-only for <strong>STAFF</strong>. Supplier changes require{' '}
          <strong>ADMIN</strong>.
        </div>
      )}

      <div className="card">
        <h2 style={{ marginTop: 0, fontSize: '1.05rem' }}>Directory</h2>
        {loading ? <LoadingState label="Refreshing…" /> : null}
        {!loading && rows.length === 0 ? (
          <EmptyState title="No suppliers found" hint="Adjust search or add a supplier." />
        ) : (
          <>
            <div className="table-wrap">
              <table>
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Email</th>
                    <th className="hide-sm">Phone</th>
                    <th className="hide-sm">Address</th>
                    {isAdmin ? <th style={{ width: '9rem' }} /> : null}
                  </tr>
                </thead>
                <tbody>
                  {rows.map((s) => (
                    <tr key={s.id}>
                      <td>
                        <strong>{s.name}</strong>
                      </td>
                      <td>{s.email}</td>
                      <td className="hide-sm">{s.phone ?? '—'}</td>
                      <td className="hide-sm">{s.address ?? '—'}</td>
                      {isAdmin ? (
                        <td>
                          <div className="row-actions">
                            <button
                              type="button"
                              className="btn btn-ghost"
                              onClick={() => startEdit(s)}
                              disabled={busy}
                            >
                              Edit
                            </button>
                            <button
                              type="button"
                              className="btn btn-danger"
                              onClick={() => setDeleteTarget(s)}
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
            Edit supplier #{editId}
          </h2>
          <form onSubmit={onUpdate} className="grid-2">
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
            <div className="field">
              <label>Email</label>
              <input
                type="email"
                value={editForm.email}
                onChange={(e) =>
                  setEditForm((f) => ({ ...f, email: e.target.value }))
                }
                required
              />
            </div>
            <div className="field">
              <label>Phone</label>
              <input
                value={editForm.phone}
                onChange={(e) =>
                  setEditForm((f) => ({ ...f, phone: e.target.value }))
                }
              />
            </div>
            <div className="field">
              <label>Address</label>
              <input
                value={editForm.address}
                onChange={(e) =>
                  setEditForm((f) => ({ ...f, address: e.target.value }))
                }
              />
            </div>
            <div className="row-actions" style={{ alignSelf: 'end' }}>
              <button type="submit" className="btn btn-primary" disabled={busy}>
                {busy ? 'Saving…' : 'Save changes'}
              </button>
              <button
                type="button"
                className="btn btn-ghost"
                onClick={() => setEditId(null)}
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
        title="Delete supplier?"
        description={
          deleteTarget
            ? `Remove ${deleteTarget.name}? Linked products may be affected.`
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
