import React, { useEffect, useState } from "react";
import api from "./services/api";

function AuditLogs() {
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadAuditLogs = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/audit-logs");

      setLogs(response.data || []);
    } catch (err) {
      console.error("Failed to load audit logs:", err);

      if (err.response?.status === 403) {
        setError(
          "Access denied. Only administrators can view audit logs."
        );
      } else if (err.response?.status === 401) {
        setError("Your session has expired. Please log in again.");
      } else {
        setError("Failed to load audit logs.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAuditLogs();
  }, []);

  const formatDate = (date) => {
    if (!date) return "-";

    try {
      return new Date(date).toLocaleString();
    } catch {
      return "-";
    }
  };

  const getActionClass = (action) => {
    if (!action) return "audit-action";

    const value = action.toUpperCase();

    if (value.includes("FAILED")) {
      return "audit-action audit-danger";
    }

    if (
      value.includes("DELETE") ||
      value.includes("PASSWORD")
    ) {
      return "audit-action audit-warning";
    }

    if (
      value.includes("LOGIN") ||
      value.includes("CREATE") ||
      value.includes("UPDATE")
    ) {
      return "audit-action audit-success";
    }

    return "audit-action";
  };

  return (
    <div className="page-content">

      <div className="page-header">
        <div>
          <h1>Audit Logs</h1>
          <p>
            Security and activity history of the transaction
            monitoring system.
          </p>
        </div>

        <button
          className="refresh-button"
          onClick={loadAuditLogs}
        >
          ↻ Refresh
        </button>
      </div>

      <div className="stat-card">
        <div className="stat-title">
          Total Audit Events
        </div>

        <div className="stat-value">
          {logs.length}
        </div>

        <div className="stat-subtitle">
          Recorded system activities
        </div>
      </div>

      <div className="table-card">

        <div className="table-header">
          <div>
            <h2>System Activity</h2>
            <p>
              Login, transaction and account activity
              recorded by the backend.
            </p>
          </div>
        </div>

        {loading ? (
          <div className="empty-state">
            Loading audit logs...
          </div>
        ) : error ? (
          <div className="empty-state">
            {error}
          </div>
        ) : logs.length === 0 ? (
          <div className="empty-state">
            No audit logs available.
          </div>
        ) : (
          <div className="table-wrapper">

            <table>

              <thead>
                <tr>
                  <th>ID</th>
                  <th>User</th>
                  <th>Action</th>
                  <th>Description</th>
                  <th>IP Address</th>
                  <th>Date & Time</th>
                </tr>
              </thead>

              <tbody>

                {logs.map((log) => (
                  <tr key={log.id}>

                    <td>
                      {log.id}
                    </td>

                    <td>
                      {log.userEmail || "-"}
                    </td>

                    <td>
                      <span
                        className={getActionClass(
                          log.action
                        )}
                      >
                        {log.action || "-"}
                      </span>
                    </td>

                    <td>
                      {log.description || "-"}
                    </td>

                    <td>
                      {log.ipAddress || "-"}
                    </td>

                    <td>
                      {formatDate(log.timestamp)}
                    </td>

                  </tr>
                ))}

              </tbody>

            </table>

          </div>
        )}

      </div>

    </div>
  );
}

export default AuditLogs;