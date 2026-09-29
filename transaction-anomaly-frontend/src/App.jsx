import React, { useEffect, useMemo, useState } from "react";
import NotificationBell from "./NotificationBell";
import Analytics from "./Analytics";
import AuditLogs from "./AuditLogs";


import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
  PieChart,
  Pie,
  Cell,
  Legend,
} from "recharts";

import api from "./services/api";
import Login from "./pages/Login";
import Sidebar from "./components/Sidebar";

import "./App.css";


function App() {

  const storedUser = JSON.parse(localStorage.getItem("user") || "null");
const userRole = storedUser?.role || "";

  const [isLoggedIn, setIsLoggedIn] = useState(
    !!localStorage.getItem("token")
  );

  const [activePage, setActivePage] = useState("dashboard");

  useEffect(() => {
    if (
      userRole !== "ADMIN" &&
      (activePage === "users" || activePage === "audit")
    ) {
      setActivePage("dashboard");
    }
  }, [userRole, activePage]);


  /* =========================================================
     DASHBOARD DATA
  ========================================================= */

  const [stats, setStats] = useState({
    totalTransactions: 0,
    totalAnomalies: 0,
    highRiskCount: 0,
    suspiciousCount: 0,
    averageRiskScore: 0,
  });


  const [anomalies, setAnomalies] = useState([]);

  const [transactions, setTransactions] = useState([]);

  const [loading, setLoading] = useState(false);


  /* =========================================================
     ANOMALY FILTERS
  ========================================================= */

  const [searchTerm, setSearchTerm] = useState("");

  const [statusFilter, setStatusFilter] = useState("ALL");

  const [riskSort, setRiskSort] = useState("NONE");

  const [selectedAnomaly, setSelectedAnomaly] = useState(null);
  const [reviewStatus, setReviewStatus] = useState("OPEN");
  const [reviewNotes, setReviewNotes] = useState("");
  const [reviewSaving, setReviewSaving] = useState(false);
  const [users, setUsers] = useState([]);
  const [usersLoading, setUsersLoading] = useState(false);

  /* =========================================================
     TRANSACTION FORM
  ========================================================= */

  const [transaction, setTransaction] = useState({

    userId: "",

    amount: "",

    transactionType: "PAYMENT",

    location: "",

    merchant: "",

    paymentMethod: "CARD",

    transactionTime: "",

    accountBalance: "",

  });


  /* =========================================================
     LOAD DATA
  ========================================================= */

  useEffect(() => {
    if (isLoggedIn) {
      loadDashboardData();
  
      if (activePage === "users") {
        loadUsers();
      }
    }
  }, [isLoggedIn, activePage]);


  const loadDashboardData = async () => {

    try {

      setLoading(true);


      const [
        statsResponse,
        anomalyResponse,
        transactionResponse,
      ] = await Promise.all([

        api.get("/dashboard/stats"),

        api.get("/anomalies"),

        api.get("/transactions"),

      ]);


      setStats(statsResponse.data);

      setAnomalies(anomalyResponse.data);

      setTransactions(transactionResponse.data);


    } catch (error) {

      console.error(
        "Failed to load dashboard data:",
        error
      );


    } finally {

      setLoading(false);

    }

  };

  useEffect(() => {
    if (selectedAnomaly?.anomaly) {
      setReviewStatus(selectedAnomaly.anomaly.reviewStatus || "OPEN");
      setReviewNotes(selectedAnomaly.anomaly.reviewNotes || "");
    }
  }, [selectedAnomaly]);


  const handleReviewAnomaly = async () => {
    const anomalyId = selectedAnomaly?.anomaly?.id;

    if (!anomalyId) {
      alert("Anomaly ID is not available.");
      return;
    }

    if (reviewNotes.length > 2000) {
      alert("Review notes cannot exceed 2000 characters.");
      return;
    }

    try {
      setReviewSaving(true);

      const response = await api.put(`/anomalies/${anomalyId}/review`, {
        reviewStatus,
        reviewNotes: reviewNotes.trim()
      });

      const updatedAnomaly = response.data;

      setAnomalies((current) =>
        current.map((item) =>
          item.anomaly?.id === anomalyId
            ? { ...item, anomaly: updatedAnomaly }
            : item
        )
      );

      setSelectedAnomaly((current) =>
        current
          ? { ...current, anomaly: updatedAnomaly }
          : current
      );

      alert("Anomaly review updated successfully.");
    } catch (error) {
      console.error("Failed to update anomaly review:", error);
      alert(
        error.response?.data?.message ||
        "Failed to update anomaly review."
      );
    } finally {
      setReviewSaving(false);
    }
  };


  const loadUsers = async () => {
    try {
      setUsersLoading(true);
  
      const response = await api.get("/users");
  
      setUsers(response.data);
    } catch (error) {
      console.error("Failed to load users:", error);
  
      if (error.response?.status === 403) {
        alert("You do not have permission to access User Management.");
      }
    } finally {
      setUsersLoading(false);
    }
  };


  const handleDeleteUser = async (user) => {
    const currentUser = JSON.parse(localStorage.getItem("user") || "null");

    if (currentUser?.email && currentUser.email === user.email) {
      alert("You cannot delete your own account.");
      return;
    }

    const confirmed = window.confirm(
      `Are you sure you want to delete ${user.name || user.email}?`
    );

    if (!confirmed) {
      return;
    }

    try {
      await api.delete(`/users/${user.id}`);
      alert("User deleted successfully.");
      await loadUsers();
    } catch (error) {
      console.error("Failed to delete user:", error);
      alert(
        error.response?.data?.message ||
        "Failed to delete user."
      );
    }
  };


  const handleEditUser = async (user) => {
    const name = window.prompt("Enter new name:", user.name || "");

    if (name === null || name.trim() === "") {
      return;
    }

    const email = window.prompt("Enter new email:", user.email || "");

    if (email === null || email.trim() === "") {
      return;
    }

    const roleInput = window.prompt(
      "Enter role (ADMIN or ANALYST):",
      user.role || "ANALYST"
    );

    if (roleInput === null) {
      return;
    }

    const role = roleInput.trim().toUpperCase();

    if (role !== "ADMIN" && role !== "ANALYST") {
      alert("Role must be ADMIN or ANALYST.");
      return;
    }

    try {
      await api.put(`/users/${user.id}`, {
        name: name.trim(),
        email: email.trim(),
        role: role
      });

      alert("User updated successfully.");
      await loadUsers();
    } catch (error) {
      console.error("Failed to update user:", error);
      alert(
        error.response?.data?.message ||
        "Failed to update user."
      );
    }
  };


  /* =========================================================
     LOGIN / LOGOUT
  ========================================================= */

  const handleLogin = () => {

    setIsLoggedIn(true);

  };


  const handleLogout = () => {

    localStorage.removeItem("token");

    localStorage.removeItem("user");

    setIsLoggedIn(false);

  };


  /* =========================================================
     TRANSACTION FORM
  ========================================================= */

  const handleChange = (event) => {

    const { name, value } = event.target;


    setTransaction((previous) => ({

      ...previous,

      [name]: value,

    }));

  };


  const handleAddTransaction = async (event) => {

    event.preventDefault();


    try {

      const payload = {

        ...transaction,

        userId: Number(transaction.userId),

        amount: Number(transaction.amount),

        accountBalance: Number(
          transaction.accountBalance
        ),

      };


      await api.post(
        "/transactions",
        payload
      );


      alert(
        "Transaction added successfully."
      );


      setTransaction({

        userId: "",

        amount: "",

        transactionType: "PAYMENT",

        location: "",

        merchant: "",

        paymentMethod: "CARD",

        transactionTime: "",

        accountBalance: "",

      });


      await loadDashboardData();


    } catch (error) {

      console.error(
        "Transaction creation failed:",
        error
      );


      alert(

        error.response?.data?.message ||

        "Failed to create transaction."

      );

    }

  };


  /* =========================================================
     RISK DISTRIBUTION
  ========================================================= */

  const riskDistribution = useMemo(() => {

    return [

      {
        name: "High Risk",
        value: stats.highRiskCount || 0,
      },

      {
        name: "Suspicious",
        value: stats.suspiciousCount || 0,
      },

      {
        name: "Normal",

        value: Math.max(

          (stats.totalTransactions || 0) -

          (stats.totalAnomalies || 0),

          0

        ),

      },

    ];

  }, [stats]);


  /* =========================================================
     ML VS RULE CHART
  ========================================================= */

  const mlVsRuleData = useMemo(() => {

    return anomalies
      .slice(0, 10)
      .map((item, index) => ({

        name:
          `TX-${
            item.transaction?.id ??
            index + 1
          }`,

        ML: Number(
          item.anomaly?.mlScore || 0
        ),

        Rules: Number(
          item.anomaly?.ruleScore || 0
        ),

      }));

  }, [anomalies]);


  /* =========================================================
     FILTERED ANOMALIES
  ========================================================= */

  const filteredAnomalies = useMemo(() => {

    let result = [...anomalies];


    /* -------------------------
       SEARCH
    ------------------------- */

    if (searchTerm.trim() !== "") {

      const search =
        searchTerm
          .toLowerCase()
          .trim();


      result = result.filter((item) => {

        const transactionId =
          item.transaction?.id
            ?.toString() || "";


        const userId =
          item.transaction?.userId
            ?.toString() || "";


        const location =
          item.transaction?.location ||
          "";


        const merchant =
          item.transaction?.merchant ||
          "";


        return (

          transactionId
            .toLowerCase()
            .includes(search)

          ||

          userId
            .toLowerCase()
            .includes(search)

          ||

          location
            .toLowerCase()
            .includes(search)

          ||

          merchant
            .toLowerCase()
            .includes(search)

        );

      });

    }


    /* -------------------------
       STATUS FILTER
    ------------------------- */

    if (statusFilter !== "ALL") {

      result = result.filter(

        (item) =>

          item.anomaly?.status ===
          statusFilter

      );

    }


    /* -------------------------
       RISK SORT
    ------------------------- */

    if (riskSort === "HIGH_TO_LOW") {

      result.sort(

        (a, b) =>

          Number(
            b.anomaly?.riskScore || 0
          )

          -

          Number(
            a.anomaly?.riskScore || 0
          )

      );

    }


    if (riskSort === "LOW_TO_HIGH") {

      result.sort(

        (a, b) =>

          Number(
            a.anomaly?.riskScore || 0
          )

          -

          Number(
            b.anomaly?.riskScore || 0
          )

      );

    }


    return result;

  }, [

    anomalies,

    searchTerm,

    statusFilter,

    riskSort,

  ]);


  /* =========================================================
     LOGIN PAGE
  ========================================================= */

  if (!isLoggedIn) {

    return (
      <Login
        onLogin={handleLogin}
      />
    );

  }


  /* =========================================================
     MAIN APPLICATION
  ========================================================= */

  return (

    <div className="app-layout">


<Sidebar
  activePage={activePage}
  setActivePage={setActivePage}
  onLogout={handleLogout}
  userRole={userRole}
/>

      <NotificationBell />
      <main className="main-content">


        {/* =====================================================
           DASHBOARD
        ===================================================== */}

        {activePage === "dashboard" && (

          <>


            <div className="page-header">

              <div>

                <h1>
                  Transaction Monitoring Dashboard
                </h1>

                <p>
                  AI-powered transaction anomaly
                  detection and risk monitoring.
                </p>

              </div>


              <button

                className="refresh-button"

                onClick={loadDashboardData}

              >

                ↻ Refresh

              </button>

            </div>


            {/* KPI CARDS */}

            <div className="stats-grid">


              <div className="stat-card">

                <div className="stat-title">
                  Total Transactions
                </div>

                <div className="stat-value">
                  {stats.totalTransactions}
                </div>

                <div className="stat-subtitle">
                  All recorded transactions
                </div>

              </div>


              <div className="stat-card">

                <div className="stat-title">
                  Total Anomalies
                </div>

                <div className="stat-value">
                  {stats.totalAnomalies}
                </div>

                <div className="stat-subtitle">
                  Detected suspicious activity
                </div>

              </div>


              <div className="stat-card danger-card">

                <div className="stat-title">
                  High Risk
                </div>

                <div className="stat-value">
                  {stats.highRiskCount}
                </div>

                <div className="stat-subtitle">
                  Requires immediate review
                </div>

              </div>


              <div className="stat-card warning-card">

                <div className="stat-title">
                  Suspicious
                </div>

                <div className="stat-value">
                  {stats.suspiciousCount}
                </div>

                <div className="stat-subtitle">
                  Requires investigation
                </div>

              </div>


              <div className="stat-card">

                <div className="stat-title">
                  Average Risk Score
                </div>

                <div className="stat-value">

                  {Number(
                    stats.averageRiskScore || 0
                  ).toFixed(2)}

                </div>

                <div className="stat-subtitle">
                  Overall anomaly risk
                </div>

              </div>


            </div>


            {/* CHARTS */}

            <div className="charts-grid">


              {/* RISK DISTRIBUTION */}

              <div className="chart-card">

                <div className="chart-header">

                  <h2>
                    Risk Distribution
                  </h2>

                  <span>
                    Transaction risk levels
                  </span>

                </div>


                <div className="chart-container">

                  {riskDistribution.some(
                    (item) =>
                      item.value > 0
                  ) ? (

                    <ResponsiveContainer
                      width="100%"
                      height="100%"
                    >

                      <PieChart>

                        <Pie

                          data={riskDistribution}

                          dataKey="value"

                          nameKey="name"

                          cx="50%"

                          cy="50%"

                          outerRadius={105}

                          label

                        >

                          {riskDistribution.map(
                            (entry, index) => (

                              <Cell

                                key={
                                  `cell-${index}`
                                }

                                fill={

                                  index === 0

                                    ? "#dc2626"

                                    : index === 1

                                    ? "#f59e0b"

                                    : "#16a34a"

                                }

                              />

                            )

                          )}

                        </Pie>


                        <Tooltip />

                        <Legend />

                      </PieChart>

                    </ResponsiveContainer>

                  ) : (

                    <div className="empty-chart">

                      No transaction data
                      available.

                    </div>

                  )}

                </div>

              </div>


              {/* ML VS RULE */}

              <div className="chart-card">

                <div className="chart-header">

                  <h2>
                    ML vs Rule Analysis
                  </h2>

                  <span>
                    Latest detected anomalies
                  </span>

                </div>


                <div className="chart-container">

                  {mlVsRuleData.length > 0 ? (

                    <ResponsiveContainer
                      width="100%"
                      height="100%"
                    >

                      <BarChart
                        data={mlVsRuleData}
                      >

                        <CartesianGrid
                          strokeDasharray="3 3"
                        />

                        <XAxis
                          dataKey="name"
                        />

                        <YAxis
                          domain={[0, 100]}
                        />

                        <Tooltip />

                        <Legend />


                        <Bar

                          dataKey="ML"

                          name="ML Score"

                          fill="#2563eb"

                        />


                        <Bar

                          dataKey="Rules"

                          name="Rule Score"

                          fill="#f59e0b"

                        />

                      </BarChart>

                    </ResponsiveContainer>

                  ) : (

                    <div className="empty-chart">

                      No anomaly data
                      available.

                    </div>

                  )}

                </div>

              </div>


            </div>


            {/* RECENT ANOMALIES */}

            <div className="table-card">


              <div className="table-header">

                <div>

                  <h2>
                    Recent Anomalies
                  </h2>

                  <p>
                    Transactions flagged by
                    the anomaly detection engine.
                  </p>

                </div>


                <button

                  className="secondary-button"

                  onClick={() =>
                    setActivePage("anomalies")
                  }

                >

                  View All

                </button>

              </div>


              {anomalies.length === 0 ? (

                <div className="empty-state">

                  No anomalies detected.

                </div>

              ) : (

                <div className="table-wrapper">

                  <table>

                    <thead>

                      <tr>

                        <th>
                          Transaction
                        </th>

                        <th>
                          User
                        </th>

                        <th>
                          Amount
                        </th>

                        <th>
                          ML Score
                        </th>

                        <th>
                          Rule Score
                        </th>

                        <th>
                          Final Risk
                        </th>

                        <th>
                          Status
                        </th>

                      </tr>

                    </thead>


                    <tbody>

                      {anomalies
                        .slice(0, 8)
                        .map((item) => (

                          <tr
                            key={
                              item.anomaly?.id
                            }
                             onClick={() => setSelectedAnomaly(item)}
                            className="clickable-anomaly-row"
                          >

                            <td>
                              TX-
                              {item.transaction?.id}
                            </td>


                            <td>
                              User{" "}
                              {item.transaction?.userId}
                            </td>


                            <td>

                              ₹
                              {Number(
                                item.transaction
                                  ?.amount || 0
                              ).toLocaleString(
                                "en-IN"
                              )}

                            </td>


                            <td>

                              {item.anomaly
                                ?.mlScore != null

                                ? Number(
                                    item.anomaly
                                      .mlScore
                                  ).toFixed(2)

                                : "N/A"}

                            </td>


                            <td>

                              {item.anomaly
                                ?.ruleScore != null

                                ? Number(
                                    item.anomaly
                                      .ruleScore
                                  ).toFixed(2)

                                : "N/A"}

                            </td>


                            <td>

                              {item.anomaly
                                ?.riskScore != null

                                ? Number(
                                    item.anomaly
                                      .riskScore
                                  ).toFixed(2)

                                : "N/A"}

                            </td>


                            <td>

                              <span

                                className={`status-badge ${
                                  item.anomaly
                                    ?.status ===
                                  "HIGH_RISK"
                                    ? "high-risk"
                                    : "suspicious"
                                }`}

                              >

                                {item.anomaly
                                  ?.status}

                              </span>

                            </td>


                          </tr>

                        ))}

                    </tbody>

                  </table>

                </div>

              )}

            </div>


          </>

        )}

        {activePage === "analytics" && (
          <Analytics />
        )}
        {/* =====================================================
           TRANSACTIONS
        ===================================================== */}

        {activePage === "transactions" && (

          <>


            <div className="page-header">

              <div>

                <h1>
                  Transactions
                </h1>

                <p>
                  Create and monitor financial
                  transactions.
                </p>

              </div>

            </div>


            {/* ADD TRANSACTION */}

            <div className="form-card">

              <h2>
                Add Transaction
              </h2>


              <form

                onSubmit={
                  handleAddTransaction
                }

                className="transaction-form"

              >


                <div className="form-grid">


                  <div className="form-group">

                    <label>
                      User ID
                    </label>

                    <input

                      type="number"

                      name="userId"

                      value={
                        transaction.userId
                      }

                      onChange={handleChange}

                      required

                    />

                  </div>


                  <div className="form-group">

                    <label>
                      Amount
                    </label>

                    <input

                      type="number"

                      name="amount"

                      value={
                        transaction.amount
                      }

                      onChange={handleChange}

                      min="1"

                      required

                    />

                  </div>


                  <div className="form-group">

                    <label>
                      Transaction Type
                    </label>

                    <select

                      name="transactionType"

                      value={
                        transaction.transactionType
                      }

                      onChange={handleChange}

                    >

                      <option value="PAYMENT">
                        PAYMENT
                      </option>

                      <option value="TRANSFER">
                        TRANSFER
                      </option>

                      <option value="WITHDRAWAL">
                        WITHDRAWAL
                      </option>

                      <option value="DEPOSIT">
                        DEPOSIT
                      </option>

                    </select>

                  </div>


                  <div className="form-group">

                    <label>
                      Location
                    </label>

                    <input

                      type="text"

                      name="location"

                      value={
                        transaction.location
                      }

                      onChange={handleChange}

                      placeholder="Mumbai"

                    />

                  </div>


                  <div className="form-group">

                    <label>
                      Merchant
                    </label>

                    <input

                      type="text"

                      name="merchant"

                      value={
                        transaction.merchant
                      }

                      onChange={handleChange}

                      placeholder="Amazon"

                    />

                  </div>


                  <div className="form-group">

                    <label>
                      Payment Method
                    </label>

                    <select

                      name="paymentMethod"

                      value={
                        transaction.paymentMethod
                      }

                      onChange={handleChange}

                    >

                      <option value="CARD">
                        CARD
                      </option>

                      <option value="UPI">
                        UPI
                      </option>

                      <option value="NET_BANKING">
                        NET BANKING
                      </option>

                      <option value="CASH">
                        CASH
                      </option>

                    </select>

                  </div>


                  <div className="form-group">

                    <label>
                      Transaction Time
                    </label>

                    <input

                      type="datetime-local"

                      name="transactionTime"

                      value={
                        transaction.transactionTime
                      }

                      onChange={handleChange}

                      required

                    />

                  </div>


                  <div className="form-group">

                    <label>
                      Account Balance
                    </label>

                    <input

                      type="number"

                      name="accountBalance"

                      value={
                        transaction.accountBalance
                      }

                      onChange={handleChange}

                      min="0"

                      required

                    />

                  </div>


                </div>


                <button

                  type="submit"

                  className="primary-button"

                >

                  Add Transaction

                </button>


              </form>

            </div>


            {/* TRANSACTION TABLE */}

            <div className="table-card">


              <div className="table-header">

                <div>

                  <h2>
                    All Transactions
                  </h2>

                  <p>
                    Complete transaction history.
                  </p>

                </div>

              </div>


              {transactions.length === 0 ? (

                <div className="empty-state">

                  No transactions found.

                </div>

              ) : (

                <div className="table-wrapper">

                  <table>

                    <thead>

                      <tr>

                        <th>ID</th>

                        <th>User</th>

                        <th>Amount</th>

                        <th>Type</th>

                        <th>Location</th>

                        <th>Merchant</th>

                        <th>Payment</th>

                        <th>Time</th>

                      </tr>

                    </thead>


                    <tbody>

                      {transactions.map(
                        (item) => (

                          <tr
                            key={item.id}
                          >

                            <td>
                              TX-{item.id}
                            </td>

                            <td>
                              User{" "}
                              {item.userId}
                            </td>

                            <td>

                              ₹
                              {Number(
                                item.amount || 0
                              ).toLocaleString(
                                "en-IN"
                              )}

                            </td>

                            <td>
                              {
                                item.transactionType
                              }
                            </td>

                            <td>
                              {
                                item.location ||
                                "-"
                              }
                            </td>

                            <td>
                              {
                                item.merchant ||
                                "-"
                              }
                            </td>

                            <td>
                              {
                                item.paymentMethod ||
                                "-"
                              }
                            </td>

                            <td>

                              {item.transactionTime

                                ? new Date(
                                    item.transactionTime
                                  ).toLocaleString()

                                : "-"}

                            </td>

                          </tr>

                        )
                      )}

                    </tbody>

                  </table>

                </div>

              )}

            </div>


          </>

        )}



        {/* =====================================================
           ANOMALIES
        ===================================================== */}

        {activePage === "anomalies" && (

          <>


            <div className="page-header">

              <div>

                <h1>
                  Anomaly Detection
                </h1>

                <p>
                  AI and rule-based transaction
                  risk analysis.
                </p>

              </div>

            </div>


            <div className="table-card">


              {/* HEADER */}

              <div className="table-header">

                <div>

                  <h2>
                    Detected Anomalies
                  </h2>

                  <p>
                    Combined machine-learning
                    and rule-based risk analysis.
                  </p>

                </div>

              </div>

              


              {/* =================================================
                 FILTER CONTROLS
              ================================================= */}

              <div className="anomaly-filters">


                {/* SEARCH */}

                <div className="filter-group search-group">

                  <label>
                    Search
                  </label>

                  <input

                    type="text"

                    placeholder="Transaction, user, location, merchant..."

                    value={searchTerm}

                    onChange={(event) =>
                      setSearchTerm(
                        event.target.value
                      )
                    }

                  />

                </div>


                {/* STATUS */}

                <div className="filter-group">

                  <label>
                    Status
                  </label>

                  <select

                    value={statusFilter}

                    onChange={(event) =>
                      setStatusFilter(
                        event.target.value
                      )
                    }

                  >

                    <option value="ALL">
                      All
                    </option>

                    <option value="HIGH_RISK">
                      High Risk
                    </option>

                    <option value="SUSPICIOUS">
                      Suspicious
                    </option>

                  </select>

                </div>


                {/* SORT */}

                <div className="filter-group">

                  <label>
                    Sort by Risk
                  </label>

                  <select

                    value={riskSort}

                    onChange={(event) =>
                      setRiskSort(
                        event.target.value
                      )
                    }

                  >

                    <option value="NONE">
                      Default
                    </option>

                    <option value="HIGH_TO_LOW">
                      Highest → Lowest
                    </option>

                    <option value="LOW_TO_HIGH">
                      Lowest → Highest
                    </option>

                  </select>

                </div>


                {/* CLEAR */}

                <button

                  className="clear-filter-button"

                  onClick={() => {

                    setSearchTerm("");

                    setStatusFilter(
                      "ALL"
                    );

                    setRiskSort(
                      "NONE"
                    );

                  }}

                >

                  Clear Filters

                </button>


              </div>


              {/* =================================================
                 RESULT COUNT
              ================================================= */}

              <div className="filter-result-count">

                Showing{" "}

                {filteredAnomalies.length}

                {" "}of{" "}

                {anomalies.length}

                {" "}anomalies

              </div>


              {/* =================================================
                 ANOMALY TABLE
              ================================================= */}

              {anomalies.length === 0 ? (

                <div className="empty-state">

                  No anomalies detected.

                </div>

              ) : filteredAnomalies.length === 0 ? (

                <div className="empty-state">

                  No anomalies match
                  your filters.

                </div>

              ) : (

                <div className="table-wrapper">

                  <table>

                    <thead>

                      <tr>

                        <th>
                          Transaction
                        </th>

                        <th>
                          User
                        </th>

                        <th>
                          Amount
                        </th>

                        <th>
                          ML Score
                        </th>

                        <th>
                          Rule Score
                        </th>

                        <th>
                          Final Risk
                        </th>

                        <th>
                          Status
                        </th>

                        <th>
                          Review
                        </th>

                        <th>
                          Reasons
                        </th>

                      </tr>

                    </thead>


                    <tbody>

                      {filteredAnomalies.map(
                        (item) => (

                          <tr
                            key={item.anomaly?.id}
                            onClick={() => setSelectedAnomaly(item)}
                            className="clickable-anomaly-row"
                          >


                            <td>

                              TX-
                              {item.transaction?.id}

                            </td>


                            <td>

                              User{" "}
                              {
                                item.transaction
                                  ?.userId
                              }

                            </td>


                            <td>

                              ₹
                              {Number(
                                item.transaction
                                  ?.amount || 0
                              ).toLocaleString(
                                "en-IN"
                              )}

                            </td>


                            <td>

                              {item.anomaly
                                ?.mlScore != null

                                ? Number(
                                    item.anomaly
                                      .mlScore
                                  ).toFixed(2)

                                : "N/A"}

                            </td>


                            <td>

                              {item.anomaly
                                ?.ruleScore != null

                                ? Number(
                                    item.anomaly
                                      .ruleScore
                                  ).toFixed(2)

                                : "N/A"}

                            </td>


                            <td>

                              <strong>

                                {item.anomaly
                                  ?.riskScore != null

                                  ? Number(
                                      item.anomaly
                                        .riskScore
                                    ).toFixed(2)

                                  : "N/A"}

                              </strong>

                            </td>


                            <td>

                              <span

                                className={`status-badge ${
                                  item.anomaly
                                    ?.status ===
                                  "HIGH_RISK"

                                    ? "high-risk"

                                    : "suspicious"
                                }`}

                              >

                                {
                                  item.anomaly
                                    ?.status
                                }

                              </span>

                            </td>


                            <td>
                              <span
                                className={`review-status-badge ${
                                  item.anomaly?.reviewStatus === "CONFIRMED"
                                    ? "review-confirmed"
                                    : item.anomaly?.reviewStatus === "FALSE_POSITIVE"
                                    ? "review-false-positive"
                                    : item.anomaly?.reviewStatus === "INVESTIGATING"
                                    ? "review-investigating"
                                    : "review-open"
                                }`}
                              >
                                {item.anomaly?.reviewStatus || "OPEN"}
                              </span>
                            </td>


                            <td className="reason-cell">

                              {
                                item.anomaly
                                  ?.reasons ||

                                "No reason available"

                              }

                            </td>


                          </tr>

                        )
                      )}

                    </tbody>

                  </table>

                </div>

              )}

              {/* =================================================
                 ANOMALY DETAILS PANEL
              ================================================= */}

              {selectedAnomaly && (
                <div className="anomaly-details-panel">

                  <div className="anomaly-details-header">
                    <div>
                      <h2>Anomaly Details</h2>
                      <p>
                        Detailed risk analysis for the selected transaction.
                      </p>
                    </div>

                    <button
                      className="close-details-button"
                      onClick={() => setSelectedAnomaly(null)}
                    >
                      Close
                    </button>
                  </div>

                  <div className="anomaly-details-grid">

                    <div className="detail-section">
                      <h3>Transaction Information</h3>

                      <div className="detail-list">
                        <div>
                          <span>Transaction ID</span>
                          <strong>
                            TX-{selectedAnomaly.transaction?.id ?? "-"}
                          </strong>
                        </div>

                        <div>
                          <span>User ID</span>
                          <strong>
                            {selectedAnomaly.transaction?.userId ?? "-"}
                          </strong>
                        </div>

                        <div>
                          <span>Amount</span>
                          <strong>
                            ₹
                            {Number(
                              selectedAnomaly.transaction?.amount || 0
                            ).toLocaleString("en-IN")}
                          </strong>
                        </div>

                        <div>
                          <span>Transaction Type</span>
                          <strong>
                            {selectedAnomaly.transaction?.transactionType || "-"}
                          </strong>
                        </div>

                        <div>
                          <span>Location</span>
                          <strong>
                            {selectedAnomaly.transaction?.location || "-"}
                          </strong>
                        </div>

                        <div>
                          <span>Merchant</span>
                          <strong>
                            {selectedAnomaly.transaction?.merchant || "-"}
                          </strong>
                        </div>

                        <div>
                          <span>Payment Method</span>
                          <strong>
                            {selectedAnomaly.transaction?.paymentMethod || "-"}
                          </strong>
                        </div>

                        <div>
                          <span>Transaction Time</span>
                          <strong>
                            {selectedAnomaly.transaction?.transactionTime
                              ? new Date(
                                  selectedAnomaly.transaction.transactionTime
                                ).toLocaleString()
                              : "-"}
                          </strong>
                        </div>

                        <div>
                          <span>Account Balance</span>
                          <strong>
                            ₹
                            {Number(
                              selectedAnomaly.transaction?.accountBalance || 0
                            ).toLocaleString("en-IN")}
                          </strong>
                        </div>
                      </div>
                    </div>

                    <div className="detail-section">
                      <h3>Risk Analysis</h3>

                      <div className="risk-score-large">
                        <span>Final Risk Score</span>
                        <strong>
                          {selectedAnomaly.anomaly?.riskScore != null
                            ? Number(
                                selectedAnomaly.anomaly.riskScore
                              ).toFixed(2)
                            : "N/A"}
                        </strong>
                      </div>

                      <div className="risk-metrics">
                        <div className="risk-metric">
                          <span>ML Score</span>
                          <strong>
                            {selectedAnomaly.anomaly?.mlScore != null
                              ? Number(
                                  selectedAnomaly.anomaly.mlScore
                                ).toFixed(2)
                              : "N/A"}
                          </strong>
                        </div>

                        <div className="risk-metric">
                          <span>Rule Score</span>
                          <strong>
                            {selectedAnomaly.anomaly?.ruleScore != null
                              ? Number(
                                  selectedAnomaly.anomaly.ruleScore
                                ).toFixed(2)
                              : "N/A"}
                          </strong>
                        </div>

                        <div className="risk-metric">
                          <span>Status</span>
                          <span
                            className={`status-badge ${
                              selectedAnomaly.anomaly?.status === "HIGH_RISK"
                                ? "high-risk"
                                : "suspicious"
                            }`}
                          >
                            {selectedAnomaly.anomaly?.status || "UNKNOWN"}
                          </span>
                        </div>
                      </div>

                      <div className="reason-details">
                        <h3>Detection Reasons</h3>

                        {selectedAnomaly.anomaly?.reasons ? (
                          <ul>
                            {selectedAnomaly.anomaly.reasons
                              .split(",")
                              .map((reason, index) => (
                                <li key={index}>
                                  {reason.trim()}
                                </li>
                              ))}
                          </ul>
                        ) : (
                          <p>No reason available.</p>
                        )}
                      </div>

                      <div
                        className="reason-details"
                        style={{
                          marginTop: "24px",
                          paddingTop: "20px",
                          borderTop: "1px solid #e5e7eb"
                        }}
                      >
                        <h3>Anomaly Review</h3>

                        <div
                          style={{
                            display: "grid",
                            gap: "14px"
                          }}
                        >
                          <div className="form-group">
                            <label>Review Status</label>
                            <select
                              value={reviewStatus}
                              onChange={(event) =>
                                setReviewStatus(event.target.value)
                              }
                              disabled={reviewSaving}
                            >
                              <option value="OPEN">OPEN</option>
                              <option value="INVESTIGATING">INVESTIGATING</option>
                              <option value="CONFIRMED">CONFIRMED</option>
                              <option value="FALSE_POSITIVE">FALSE POSITIVE</option>
                            </select>
                          </div>

                          <div className="form-group">
                            <label>Analyst Notes</label>
                            <textarea
                              value={reviewNotes}
                              onChange={(event) =>
                                setReviewNotes(event.target.value)
                              }
                              placeholder="Enter investigation notes..."
                              maxLength={2000}
                              rows={5}
                              disabled={reviewSaving}
                              style={{
                                width: "100%",
                                resize: "vertical",
                                boxSizing: "border-box"
                              }}
                            />
                            <small>
                              {reviewNotes.length}/2000 characters
                            </small>
                          </div>

                          <button
                            type="button"
                            className="primary-button"
                            onClick={handleReviewAnomaly}
                            disabled={reviewSaving}
                          >
                            {reviewSaving
                              ? "Saving Review..."
                              : "Save Review"}
                          </button>

                          {selectedAnomaly.anomaly?.reviewedBy && (
                            <div
                              style={{
                                marginTop: "4px",
                                padding: "12px",
                                borderRadius: "8px",
                                background: "#f8fafc",
                                fontSize: "14px"
                              }}
                            >
                              <div>
                                <strong>Reviewed By:</strong>{" "}
                                {selectedAnomaly.anomaly.reviewedBy}
                              </div>
                              <div style={{ marginTop: "4px" }}>
                                <strong>Reviewed At:</strong>{" "}
                                {selectedAnomaly.anomaly.reviewedAt
                                  ? new Date(
                                      selectedAnomaly.anomaly.reviewedAt
                                    ).toLocaleString()
                                  : "-"}
                              </div>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>

                  </div>

                </div>
              )}

            </div>


          </>

        )}
{activePage === "users" && userRole === "ADMIN" && (
  <div className="page-content">
    <div className="page-header">
      <div>
        <h1>User Management</h1>
        <p>Manage users registered in the anomaly detection system.</p>
      </div>
    </div>

    {usersLoading ? (
      <div className="loading-message">
        Loading users...
      </div>
    ) : users.length === 0 ? (
      <div className="empty-state">
        <h3>No Users Found</h3>
        <p>There are no users available to display.</p>
      </div>
    ) : (
      <div className="table-card">
        <div className="table-header">
          <div>
            <h2>Registered Users</h2>
            <p>Administrators can update roles or remove users.</p>
          </div>
          <button
            className="secondary-button"
            onClick={loadUsers}
          >
            ↻ Refresh
          </button>
        </div>

        <div className="table-wrapper">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Actions</th>
              </tr>
            </thead>

            <tbody>
              {users.map((user) => (
                <tr key={user.id}>
                  <td>{user.id}</td>
                  <td>{user.name}</td>
                  <td>{user.email}</td>
                  <td>
                    <span
                      className={`role-badge ${
                        user.role === "ADMIN"
                          ? "role-admin"
                          : "role-analyst"
                      }`}
                    >
                      {user.role}
                    </span>
                  </td>
                  <td>
                    <div
                      style={{
                        display: "flex",
                        gap: "8px",
                        flexWrap: "wrap"
                      }}
                    >
                      <button
                        type="button"
                        className="secondary-button"
                        onClick={() => handleEditUser(user)}
                      >
                        Edit
                      </button>

                      <button
                        type="button"
                        onClick={() => handleDeleteUser(user)}
                        style={{
                          border: "none",
                          borderRadius: "8px",
                          padding: "8px 12px",
                          background: "#fee2e2",
                          color: "#b91c1c",
                          cursor: "pointer",
                          fontWeight: 600
                        }}
                      >
                        Delete
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    )}
  </div>
)}

{activePage === "audit" && userRole === "ADMIN" && (
  <AuditLogs />
)}

      </main>

    </div>

  );

}


export default App;