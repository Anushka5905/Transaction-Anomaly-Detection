import React, { useEffect, useRef, useState } from "react";
import api from "./services/api";

function NotificationBell() {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [open, setOpen] = useState(false);
  const [loading, setLoading] = useState(false);

  const dropdownRef = useRef(null);

  // ============================================================
  // LOAD NOTIFICATIONS
  // ============================================================

  const loadNotifications = async () => {
    try {
      const response = await api.get("/notifications");
      setNotifications(response.data || []);

      const unreadResponse = await api.get("/notifications/count");
      setUnreadCount(unreadResponse.data?.count || 0);
    } catch (error) {
      console.error("Failed to load notifications:", error);
    }
  };

  // ============================================================
  // LOAD WHEN COMPONENT STARTS
  // ============================================================

  useEffect(() => {
    loadNotifications();

    // Refresh notification count every 30 seconds
    const interval = setInterval(() => {
      loadNotifications();
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  // ============================================================
  // CLOSE DROPDOWN WHEN CLICKING OUTSIDE
  // ============================================================

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target)
      ) {
        setOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, []);

  // ============================================================
  // MARK ONE NOTIFICATION AS READ
  // ============================================================

  const markAsRead = async (notificationId) => {
    try {
      await api.put(`/notifications/${notificationId}/read`);

      setNotifications((previous) =>
        previous.map((notification) =>
          notification.id === notificationId
            ? { ...notification, readStatus: true }
            : notification
        )
      );

      setUnreadCount((previous) => Math.max(0, previous - 1));
    } catch (error) {
      console.error("Failed to mark notification as read:", error);
    }
  };

  // ============================================================
  // MARK ALL AS READ
  // ============================================================

  const markAllAsRead = async () => {
    try {
      setLoading(true);

      await api.put("/notifications/read-all");

      setNotifications((previous) =>
        previous.map((notification) => ({
          ...notification,
          readStatus: true,
        }))
      );

      setUnreadCount(0);
    } catch (error) {
      console.error("Failed to mark all notifications as read:", error);
    } finally {
      setLoading(false);
    }
  };

  // ============================================================
  // FORMAT DATE
  // ============================================================

  const formatDate = (date) => {
    if (!date) return "";

    try {
      return new Date(date).toLocaleString();
    } catch {
      return "";
    }
  };

  // ============================================================
  // RENDER
  // ============================================================

  return (
    <div className="notification-container" ref={dropdownRef}>
      {/* Notification Bell */}
      <button
        className="notification-bell"
        onClick={() => setOpen((previous) => !previous)}
        title="Notifications"
      >
        <span className="notification-icon">🔔</span>

        {unreadCount > 0 && (
          <span className="notification-badge">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {/* Notification Dropdown */}
      {open && (
        <div className="notification-dropdown">
          <div className="notification-header">
            <div>
              <h3>Notifications</h3>
              <span>
                {unreadCount} unread
              </span>
            </div>

            {unreadCount > 0 && (
              <button
                className="mark-all-button"
                onClick={markAllAsRead}
                disabled={loading}
              >
                Mark all as read
              </button>
            )}
          </div>

          <div className="notification-list">
            {notifications.length === 0 ? (
              <div className="notification-empty">
                <div className="notification-empty-icon">🔔</div>
                <p>No notifications</p>
              </div>
            ) : (
              notifications.map((notification) => (
                <div
                  key={notification.id}
                  className={`notification-item ${
                    notification.readStatus
                      ? "notification-read"
                      : "notification-unread"
                  }`}
                  onClick={() => {
                    if (!notification.readStatus) {
                      markAsRead(notification.id);
                    }
                  }}
                >
                  <div className="notification-item-icon">
                    {notification.type === "HIGH_RISK"
                      ? "🚨"
                      : "⚠️"}
                  </div>

                  <div className="notification-item-content">
                    <div className="notification-item-message">
                      {notification.message}
                    </div>

                    <div className="notification-item-time">
                      {formatDate(notification.createdAt)}
                    </div>

                    {notification.type && (
                      <span
                        className={`notification-type ${
                          notification.type === "HIGH_RISK"
                            ? "notification-type-high"
                            : "notification-type-suspicious"
                        }`}
                      >
                        {notification.type === "HIGH_RISK"
                          ? "HIGH RISK"
                          : "SUSPICIOUS"}
                      </span>
                    )}
                  </div>

                  {!notification.readStatus && (
                    <span className="notification-unread-dot"></span>
                  )}
                </div>
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
}

export default NotificationBell;