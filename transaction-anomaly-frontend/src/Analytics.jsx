import React, { useEffect, useMemo, useState } from "react";

import {
  BarChart,
  Bar,
  LineChart,
  Line,
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

function Analytics() {
  const [analytics, setAnalytics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadAnalytics = async () => {
    try {
      setLoading(true);
      setError("");

      const response = await api.get("/analytics");

      setAnalytics(response.data);
    } catch (err) {
      console.error("Failed to load analytics:", err);

      if (err.response?.status === 403) {
        setError("You do not have permission to access analytics.");
      } else {
        setError("Failed to load analytics data.");
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnalytics();
  }, []);

  const riskDistribution = useMemo(() => {
    if (!analytics) return [];

    return [
      {
        name: "High Risk",
        value: analytics.highRiskTransactions || 0,
      },
      {
        name: "Suspicious",
        value: analytics.suspiciousTransactions || 0,
      },
      {
        name: "Normal",
        value: analytics.normalTransactions || 0,
      },
    ];
  }, [analytics]);

  const reviewDistribution = useMemo(() => {
    if (!analytics) return [];

    return [
      {
        name: "Open",
        value: analytics.openReviews || 0,
      },
      {
        name: "Investigating",
        value: analytics.investigatingReviews || 0,
      },
      {
        name: "Confirmed",
        value: analytics.confirmedReviews || 0,
      },
      {
        name: "False Positive",
        value: analytics.falsePositiveReviews || 0,
      },
    ];
  }, [analytics]);

  const dailyTransactions = useMemo(() => {
    if (!analytics?.dailyTransactions) return [];

    return analytics.dailyTransactions.map((item) => ({
      date: item.date,
      transactions: Number(item.count || 0),
    }));
  }, [analytics]);

  const dailyRisk = useMemo(() => {
    if (!analytics?.dailyRisk) return [];

    return analytics.dailyRisk.map((item) => ({
      date: item.date,
      risk: Number(item.averageRisk || 0),
    }));
  }, [analytics]);

  const reviewedCount = useMemo(() => {
    if (!analytics) return 0;

    return (
      (analytics.investigatingReviews || 0) +
      (analytics.confirmedReviews || 0) +
      (analytics.falsePositiveReviews || 0)
    );
  }, [analytics]);

  const totalAnomalies = useMemo(() => {
    if (!analytics) return 0;

    return (
      (analytics.openReviews || 0) +
      (analytics.investigatingReviews || 0) +
      (analytics.confirmedReviews || 0) +
      (analytics.falsePositiveReviews || 0)
    );
  }, [analytics]);

  const reviewRate = useMemo(() => {
    if (totalAnomalies === 0) return 0;

    return ((reviewedCount / totalAnomalies) * 100).toFixed(1);
  }, [reviewedCount, totalAnomalies]);

  if (loading) {
    return (
      <div className="page-content">
        <div className="page-header">
          <div>
            <h1>Advanced Analytics</h1>
            <p>
              Detailed transaction risk and anomaly analysis.
            </p>
          </div>
        </div>

        <div className="loading-message">
          Loading analytics...
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="page-content">
        <div className="page-header">
          <div>
            <h1>Advanced Analytics</h1>
            <p>
              Detailed transaction risk and anomaly analysis.
            </p>
          </div>

          <button
            className="refresh-button"
            onClick={loadAnalytics}
          >
            ↻ Refresh
          </button>
        </div>

        <div className="empty-state">
          <h3>Analytics Unavailable</h3>
          <p>{error}</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-content">

      {/* HEADER */}

      <div className="page-header">

        <div>
          <h1>Advanced Analytics</h1>

          <p>
            Detailed transaction risk, anomaly and
            investigation analytics.
          </p>
        </div>

        <button
          className="refresh-button"
          onClick={loadAnalytics}
        >
          ↻ Refresh
        </button>

      </div>


      {/* KPI CARDS */}

      <div className="stats-grid">

        <div className="stat-card">

          <div className="stat-title">
            Anomaly Rate
          </div>

          <div className="stat-value">
            {Number(
              analytics?.anomalyRate || 0
            ).toFixed(2)}
            %
          </div>

          <div className="stat-subtitle">
            Percentage of transactions flagged
          </div>

        </div>


        <div className="stat-card danger-card">

          <div className="stat-title">
            High Risk
          </div>

          <div className="stat-value">
            {analytics?.highRiskTransactions || 0}
          </div>

          <div className="stat-subtitle">
            Requires immediate attention
          </div>

        </div>


        <div className="stat-card warning-card">

          <div className="stat-title">
            Suspicious
          </div>

          <div className="stat-value">
            {analytics?.suspiciousTransactions || 0}
          </div>

          <div className="stat-subtitle">
            Requires investigation
          </div>

        </div>


        <div className="stat-card">

          <div className="stat-title">
            Normal
          </div>

          <div className="stat-value">
            {analytics?.normalTransactions || 0}
          </div>

          <div className="stat-subtitle">
            Transactions without detected anomaly
          </div>

        </div>


        <div className="stat-card">

          <div className="stat-title">
            Review Progress
          </div>

          <div className="stat-value">
            {reviewRate}%
          </div>

          <div className="stat-subtitle">
            Detected anomalies reviewed
          </div>

        </div>

      </div>


      {/* FIRST ROW */}

      <div className="charts-grid">


        {/* RISK DISTRIBUTION */}

        <div className="chart-card">

          <div className="chart-header">

            <h2>Risk Distribution</h2>

            <span>
              Transaction risk classification
            </span>

          </div>

          <div className="chart-container">

            {riskDistribution.some(
              (item) => item.value > 0
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
                          key={`risk-${index}`}
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
                No risk data available.
              </div>

            )}

          </div>

        </div>


        {/* REVIEW DISTRIBUTION */}

        <div className="chart-card">

          <div className="chart-header">

            <h2>Investigation Status</h2>

            <span>
              Anomaly review progress
            </span>

          </div>

          <div className="chart-container">

            {reviewDistribution.some(
              (item) => item.value > 0
            ) ? (

              <ResponsiveContainer
                width="100%"
                height="100%"
              >

                <PieChart>

                  <Pie
                    data={reviewDistribution}
                    dataKey="value"
                    nameKey="name"
                    cx="50%"
                    cy="50%"
                    outerRadius={105}
                    label
                  >

                    {reviewDistribution.map(
                      (entry, index) => (

                        <Cell
                          key={`review-${index}`}
                          fill={
                            index === 0
                              ? "#64748b"
                              : index === 1
                              ? "#2563eb"
                              : index === 2
                              ? "#dc2626"
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
                No review data available.
              </div>

            )}

          </div>

        </div>

      </div>


      {/* TRANSACTION ACTIVITY */}

      <div className="chart-card">

        <div className="chart-header">

          <div>

            <h2>Transaction Activity</h2>

            <span>
              Number of transactions recorded by date
            </span>

          </div>

        </div>

        <div className="chart-container">

          {dailyTransactions.length > 0 ? (

            <ResponsiveContainer
              width="100%"
              height="100%"
            >

              <BarChart
                data={dailyTransactions}
              >

                <CartesianGrid
                  strokeDasharray="3 3"
                />

                <XAxis
                  dataKey="date"
                />

                <YAxis allowDecimals={false} />

                <Tooltip />

                <Bar
                  dataKey="transactions"
                  name="Transactions"
                  fill="#2563eb"
                />

              </BarChart>

            </ResponsiveContainer>

          ) : (

            <div className="empty-chart">
              No transaction activity data available.
            </div>

          )}

        </div>

      </div>


      {/* RISK TREND */}

      <div className="chart-card">

        <div className="chart-header">

          <div>

            <h2>Average Risk Trend</h2>

            <span>
              Average anomaly risk score over time
            </span>

          </div>

        </div>

        <div className="chart-container">

          {dailyRisk.length > 0 ? (

            <ResponsiveContainer
              width="100%"
              height="100%"
            >

              <LineChart
                data={dailyRisk}
              >

                <CartesianGrid
                  strokeDasharray="3 3"
                />

                <XAxis
                  dataKey="date"
                />

                <YAxis
                  domain={[0, 100]}
                />

                <Tooltip />

                <Line
                  type="monotone"
                  dataKey="risk"
                  name="Average Risk"
                  stroke="#dc2626"
                  strokeWidth={3}
                  dot
                />

              </LineChart>

            </ResponsiveContainer>

          ) : (

            <div className="empty-chart">
              No risk trend data available.
            </div>

          )}

        </div>

      </div>


      {/* REVIEW SUMMARY */}

      <div className="table-card">

        <div className="table-header">

          <div>

            <h2>Investigation Summary</h2>

            <p>
              Current status of detected anomalies.
            </p>

          </div>

        </div>


        <div className="stats-grid">

          <div className="stat-card">

            <div className="stat-title">
              Open
            </div>

            <div className="stat-value">
              {analytics?.openReviews || 0}
            </div>

            <div className="stat-subtitle">
              Waiting for review
            </div>

          </div>


          <div className="stat-card">

            <div className="stat-title">
              Investigating
            </div>

            <div className="stat-value">
              {analytics?.investigatingReviews || 0}
            </div>

            <div className="stat-subtitle">
              Currently under investigation
            </div>

          </div>


          <div className="stat-card danger-card">

            <div className="stat-title">
              Confirmed
            </div>

            <div className="stat-value">
              {analytics?.confirmedReviews || 0}
            </div>

            <div className="stat-subtitle">
              Confirmed suspicious activity
            </div>

          </div>


          <div className="stat-card">

            <div className="stat-title">
              False Positive
            </div>

            <div className="stat-value">
              {analytics?.falsePositiveReviews || 0}
            </div>

            <div className="stat-subtitle">
              Marked as legitimate
            </div>

          </div>

        </div>

      </div>

    </div>
  );
}

export default Analytics;