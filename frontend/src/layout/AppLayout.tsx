import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'

const navCls = ({ isActive }: { isActive: boolean }) =>
  isActive ? 'active' : undefined

export function AppLayout() {
  const { user, logout, isAdmin } = useAuth()

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="sidebar-brand">InventoryFlow</div>
        <NavLink to="/dashboard" className={navCls} end>
          Dashboard
        </NavLink>
        <NavLink to="/products" className={navCls}>
          Products
        </NavLink>
        <NavLink to="/suppliers" className={navCls}>
          Suppliers
        </NavLink>
        {isAdmin ? (
          <NavLink to="/purchase-orders" className={navCls}>
            Purchase orders
          </NavLink>
        ) : null}
        <NavLink to="/sales-orders" className={navCls}>
          Sales orders
        </NavLink>
      </aside>
      <div className="main">
        <header className="topbar">
          <h1>InventoryFlow</h1>
          <div className="topbar-meta">
            <span className="muted hide-sm">{user?.email}</span>
            {user?.roles?.map((r) => (
              <span
                key={r}
                className={r === 'ADMIN' ? 'badge badge-admin' : 'badge'}
              >
                {r}
              </span>
            ))}
            <button type="button" className="btn btn-ghost" onClick={logout}>
              Log out
            </button>
          </div>
        </header>
        <main className="content">
          <Outlet />
        </main>
      </div>
    </div>
  )
}
