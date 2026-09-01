# AI Transaction Anomaly Visualization System

A small Java desktop app that stores financial transactions in MySQL, flags
unusual ones using a Weka machine-learning filter, and shows everything on an
interactive JavaFX dashboard.

## Tech Stack
Java 17 + JavaFX + Weka + MySQL + JDBC (Maven project)

## Project Structure
```
transaction-anomaly-system/
├── pom.xml                          # Maven dependencies (JavaFX, Weka, MySQL)
├── sql/schema.sql                   # Creates the database + table + sample data
└── src/main/
    ├── java/com/transactionanomaly/
    │   ├── Main.java                # App entry point (opens the window)
    │   ├── model/Transaction.java   # Plain data class for one transaction
    │   ├── db/DatabaseConnection.java  # Opens the JDBC connection
    │   ├── db/TransactionDAO.java   # Insert / read / update SQL queries
    │   ├── ml/AnomalyDetector.java  # Weka-based anomaly detection logic
    │   └── ui/DashboardView.java    # The whole interactive screen (table + form + chart)
    └── resources/style.css          # Dashboard styling
```

Each file does one job, so you can read them in this order to understand the
whole app: `Transaction` → `DatabaseConnection` → `TransactionDAO` →
`AnomalyDetector` → `DashboardView` → `Main`.

## Setup

### 1. Install prerequisites
- Java 17 or newer (JDK)
- Maven
- MySQL Server, running locally

### 2. Create the database
Open a MySQL client and run:
```bash
mysql -u root -p < sql/schema.sql
```
This creates `transaction_anomaly_db`, the `transactions` table, and 10
sample rows (two of which are intentionally unusual, so you'll see results
right away).

### 3. Set your MySQL credentials
Open `src/main/java/com/transactionanomaly/db/DatabaseConnection.java` and
update `USERNAME` / `PASSWORD` to match your MySQL setup.

### 4. Run the app
```bash
mvn clean javafx:run
```
(Or import the project into IntelliJ IDEA / Eclipse as a Maven project and
run `Main.java` directly — most IDEs will ask you to add the JavaFX VM
options automatically.)

## How to use the dashboard
- **Add Transaction** (left panel): fill in the form and click the button —
  it's saved straight to MySQL and the table refreshes.
- **Run Anomaly Detection** (top bar): analyzes every transaction currently
  loaded, scores each one, and saves the result back to the database.
  Suspicious rows turn red in the table and appear as orange dots on the
  chart.
- **Refresh**: reloads the table from the database.

## How the anomaly detection works
`AnomalyDetector.java` builds a small Weka dataset from each transaction's
**amount** and **hour of day**, then runs Weka's `InterquartileRange`
filter, which is a standard statistics technique: it flags any value that
sits far outside the normal spread of the data as an "Outlier", and values
extremely far outside as an "Extreme Value". That's converted into a
0.0–1.0 anomaly score:
- `1.0` → extreme value (very likely suspicious)
- `0.6` → outlier (worth a second look)
- `0.1` → normal

This keeps the ML part easy to follow while still being a real,
explainable anomaly-detection method — no training data or saved model
required, so it works the moment you add new transactions.

## Extending it
- Add more features to the detector (e.g. transaction frequency per user)
  by adding more attributes in `AnomalyDetector.buildDataset`-style code.
- Swap `InterquartileRange` for a Weka clustering algorithm (e.g.
  `SimpleKMeans`) if you want distance-from-cluster-center anomaly scoring.
- Add authentication / multiple users to the JavaFX login screen.
