import React from "react";

function Sidebar({
  activePage,
  setActivePage,
  onLogout,
  userRole,
}) {
  const menuItems = [
    {
      id: "dashboard",
      label: "Dashboard",
      icon: "▦",
    },
    {
      id: "analytics",
      label: "Analytics",
      icon: "📊",
    },
    {
      id: "transactions",
      label: "Transactions",
      icon: "↔",
    },
    {
      id: "anomalies",
      label: "Anomalies",
      icon: "⚠",
    },
  ];

  // Only ADMIN can see these pages
  if (userRole === "ADMIN") {
    menuItems.push(
      {
        id: "users",
        label: "User Management",
        icon: "👥",
      },
      {
        id: "audit",
        label: "Audit Logs",
        icon: "📋",
      }
    );
  }

  return (
    <aside className="sidebar">

      <div className="sidebar-logo">
        <div className="logo-icon">
          AI
        </div>

        <div>
          <h2>Transaction AI</h2>
          <span>Risk Monitoring</span>
        </div>
      </div>

      <nav className="sidebar-nav">

        {menuItems.map((item) => (

          <button
            key={item.id}
            className={`sidebar-item ${
              activePage === item.id
                ? "active"
                : ""
            }`}
            onClick={() =>
              setActivePage(item.id)
            }
          >

            <span className="sidebar-icon">
              {item.icon}
            </span>

            <span>
              {item.label}
            </span>

          </button>

        ))}

      </nav>

      <div className="sidebar-bottom">

        <button
          className="sidebar-logout"
          onClick={onLogout}
        >
          <span className="sidebar-icon">
            ⇥
          </span>

          Logout
        </button>

      </div>

    </aside>
  );
}

export default Sidebar;