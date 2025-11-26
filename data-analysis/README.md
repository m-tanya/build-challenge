# Sales Data Analysis

A Java application for analyzing sales data from CSV files using Java Streams and lambda expressions. Provides comprehensive sales analytics including revenue metrics, segmentation, trends, and financial views (cash vs accrual, invoice aging, and profitability).


## Key Features

### Core Analytics
- **Revenue Metrics**: Total net revenue, units sold, average order value, average selling price
- **Segmentation**: Revenue, units, and order counts by product, category, region, channel, and customer segment
- **Time-Based Trends**: Monthly revenue, month-over-month changes, growth rates, rolling averages
- **Rankings**: Top N products, customers, and regions by revenue
- **Discounts & Refunds**: Total discounts, discount share of revenue, refund impact

### Financial Features
- **Cash vs Accrual Revenue**: Compare revenue recognition timing (invoice date vs payment date)
- **Invoice Aging**: Analyze outstanding invoices by aging buckets (0-30, 31-60, 61-90, 90+ days)
- **Profitability Analysis**: Gross profit and margin analysis by product, category, and region


**Command Options:**
- `inputPath` (optional): Path to the CSV file. If not provided, defaults to `data/sales.csv`.
- `--asOfDate=YYYY-MM-DD` (optional): Date for aging analysis. If not provided, uses current date.

## Choices & Assumptions
- **Language & Tooling**: Pure Java 17 project with no build system; scripts invoke `javac`/`java` directly so the app runs anywhere a JDK is available. JUnit Console Standalone is downloaded on demand.
- **Dataset**: `data/sales.csv` is a synthetic dataset with discounts, refunds, and late invoices so every analytics section has meaningful signal. Values are in USD and pre-normalized; currency conversion is out of scope.
- **Precision**: Monetary calculations use `BigDecimal` and reporting uses US locale formatting. Metrics are rounded for readability but keep two decimal places.
- **Execution Model**: Entire dataset loads in memory to prioritize simplicity and streaming-style operations. For extremely large files you would swap in chunked loaders, but that was not required here.
- **Reporting**: Output goes to stdout only (no external storage) to make it easy to capture logs (`analysis-report.txt`, `ci-run.log`) for submissions and CI artifacts.
- **Date Handling**: Aging calculations default to the system date; tests pin deterministic dates to keep assertions stable.

---

## Output and Explanation

The analysis produces a comprehensive report with the following sections:

### 1. Sales Overview
Displays high-level metrics:
- **Total Net Revenue**: Sum of all net amounts (after discounts, taxes, refunds)
- **Total Units Sold**: Sum of all quantities
- **Average Order Value**: Total revenue divided by number of invoices
- **Average Selling Price per Unit**: Total revenue divided by total units

**Example:**
```
=== Sales Overview ===
Total Net Revenue: $1,785,120.58
Total Units Sold: 14,966
Average Order Value: $1,785.12
Average Selling Price per Unit: $119.28
```

### 2. Revenue by Segment and Region
Breaks down revenue by various dimensions:
- **Product Category**: Software, Service, Hardware, Subscription
- **Region**: Geographic regions (CA, TX, NY, FL, IL, WA)
- **Channel**: Sales channels (Online, Retail, Partner)
- **Customer Segment**: SMB, Freelancer, Enterprise, Startup

**Example:**
```
=== Revenue by Segment and Region ===

Revenue by Product Category:
  Software: $474,954.76
  Service: $448,498.58
  Hardware: $442,609.78
  Subscription: $419,057.46

Revenue by Region:
  TX: $317,318.02
  WA: $311,355.32
  FL: $299,093.55
  ...
```

### 3. Time-Based Trends
Shows revenue trends over time:
- **Revenue by Month**: Monthly revenue totals
- **Month-over-Month Revenue Change**: Percentage change from previous month
- **Rolling 3-Month Average Revenue**: Smoothed trend indicator

**Example:**
```
=== Time-Based Trends ===

Revenue by Month:
  2024-01: $83,056.62
  2024-02: $106,521.28
  2024-03: $91,875.61
  ...

Month-over-Month Revenue Change:
  2024-02: $23,464.66 (28.25%)
  2024-03: $-14,645.67 (-13.75%)
  ...

Rolling 3-Month Average Revenue:
  2024-02: $86,746.93
  2024-03: $93,817.84
  ...
```

### 4. Top Rankings
Lists top performers:
- **Top 5 Products by Revenue**: Highest revenue-generating products
- **Top 5 Customers by Revenue**: Highest revenue-generating customers
- **Top 10 Regions by Revenue**: Highest revenue-generating regions

**Example:**
```
=== Top Rankings ===

Top Products by Revenue:
  1. PROD-2: $159,265.86
  2. PROD-10: $154,864.39
  3. PROD-8: $154,163.56
  4. PROD-4: $151,769.34
  5. PROD-7: $150,825.89

Top Customers by Revenue:
  1. CUST-101: $44,380.00
  2. CUST-105: $43,395.57
  3. CUST-130: $41,088.37
  ...
```

### 5. Discounts and Refunds
Shows impact of discounts and refunds:
- **Total Discount Amount**: Sum of all discounts
- **Discount Share of Revenue**: Discounts as percentage of gross revenue
- **Total Refund Impact**: Net impact of refunds (negative amount)

**Example:**
```
=== Discounts and Refunds ===
Total Discount Amount: $8,862.69
Discount Share of Revenue: 0.49%
Total Refund Impact: $-84,242.43
```

### 6. Cash vs Accrual Revenue
Compares revenue recognition methods:
- **Accrual Revenue**: Revenue recognized by invoice date (when invoice is issued)
- **Cash Revenue**: Revenue recognized by payment date (when payment is received)
- **Difference**: Shows timing differences between recognition methods

**Example:**
```
=== Cash vs Accrual Revenue ===

Accrual Revenue (by Invoice Date):
  2024-01: $83,056.62
  2024-02: $106,521.28
  ...

Cash Revenue (by Payment Date):
  2024-01: $27,256.98
  2024-02: $20,852.76
  ...

Difference (Accrual - Cash):
  2024-01: $55,799.64
  2024-02: $85,668.52
  ...
```

### 7. Open Invoices and Aging
Analyzes outstanding invoices:
- **Total Outstanding Amount**: Sum of all unpaid invoice amounts
- **Total Open Invoices**: Count of unpaid invoices
- **Outstanding Amount by Aging Bucket**: Breakdown by days overdue:
  - Current (not yet due)
  - 0-30 days
  - 31-60 days
  - 61-90 days
  - 90+ days

**Example:**
```
=== Open Invoices and Aging ===
As of Date: 2025-11-25
Total Outstanding Amount: $1,222,504.22
Total Open Invoices: 654

Outstanding Amount by Aging Bucket:
  Current days: $14,296.79 (8 invoices)
  0-30 days: $67,518.93 (33 invoices)
  31-60 days: $58,319.66 (34 invoices)
  61-90 days: $22,208.31 (15 invoices)
  90+ days: $1,060,160.53 (564 invoices)
```

### 8. Profitability and Margins
Shows profitability analysis:
- **Total Gross Profit**: Revenue minus cost of goods sold
- **Overall Gross Margin**: Gross profit as percentage of revenue
- **Gross Margin by Product Category**: Margin breakdown by category
- **Gross Margin by Region**: Margin breakdown by region
- **Product Margin Profiles**: Detailed margin analysis for each product

**Example:**
```
=== Profitability and Margins ===
Total Gross Profit: $805,535.79
Overall Gross Margin: 45.13%

Gross Margin by Product Category:
  Software: 46.52% (Profit: $220,929.58)
  Hardware: 45.93% (Profit: $203,272.84)
  Service: 44.99% (Profit: $201,765.33)
  Subscription: 42.85% (Profit: $179,568.04)

Gross Margin by Region:
  WA: 46.14% (Profit: $143,650.01)
  FL: 45.95% (Profit: $137,426.35)
  ...

Product Margin Profiles:
  Tax Preparation Tool (PROD-2): Revenue $159,265.86, Margin 44.90%, Profile: Balanced
  Project Management (PROD-10): Revenue $154,864.39, Margin 45.54%, Profile: Balanced
  ...
```

---

## Testing

### Option 1: Using JUnit (Recommended)

#### Download JUnit Jars

Download the JUnit Platform Console Standalone JAR file directly (no build tools required):

```bash
# Create lib directory for JAR files
mkdir -p lib

# Download JUnit Platform Console Standalone (includes everything needed)
curl -L -o lib/junit-platform-console-standalone-1.10.0.jar \
  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.0/junit-platform-console-standalone-1.10.0.jar
```

On Windows (PowerShell):
```powershell
New-Item -ItemType Directory -Force -Path lib
Invoke-WebRequest -Uri 'https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/1.10.0/junit-platform-console-standalone-1.10.0.jar' -OutFile 'lib/junit-platform-console-standalone-1.10.0.jar'
```

**Note**: The URL above is just a direct download link. You're downloading a JAR file directly - no Maven, Gradle, or any other build tool is required or used.

#### Compile Tests

```bash
# Compile test source files with JUnit on classpath
javac -d out-test -encoding UTF-8 \
  -cp "out:lib/junit-platform-console-standalone-1.10.0.jar" \
  src/test/java/com/dataanalysis/**/*.java
```

On Windows:
```powershell
javac -d out-test -encoding UTF-8 `
  -cp "out;lib/junit-platform-console-standalone-1.10.0.jar" `
  src/test/java/com/dataanalysis/**/*.java
```

#### Run Tests

**Using Helper Scripts:**

**Linux/Mac:**
```bash
chmod +x run-tests.sh
./run-tests.sh
```

**Windows:**
```cmd
run-tests.bat
```

**Manual Execution:**
```bash
# Run tests using JUnit Platform Console Launcher
java -jar lib/junit-platform-console-standalone-1.10.0.jar \
  --class-path out:out-test \
  --scan-class-path
```

On Windows:
```powershell
java -jar lib/junit-platform-console-standalone-1.10.0.jar `
  --class-path "out;out-test" `
  --scan-class-path
```

### Option 2: Simple Test Runner (Alternative)

If you prefer not to use JUnit, you can create a simple test runner. See `TESTING.md` for details on creating assertion-based test classes.

---

## Test Output

Check folder /test-result for output
---

## Test Suites and Coverage

### Unit Tests

**Coverage**: Data generation, loading, and analysis integration.

### Test Coverage Summary

**Total Coverage**: 49 tests covering all major components and edge cases.

---

## CSV Format

See [docs/DATASET.md](docs/DATASET.md) for detailed CSV schema and dataset assumptions.

---

## License

This project is provided as-is for educational and demonstration purposes.
