# Dataset Documentation

## Overview

This project uses a synthetic sales dataset stored as a CSV file. The dataset is generated programmatically by the `CsvDataGenerator` class, ensuring:

- The schema exactly matches the analytics requirements
- There is enough variety in customers, products, dates, and payments to exercise grouping and aggregation logic
- It is easy to scale the dataset size (e.g., 1k, 10k, 100k rows) for both correctness and performance testing

## CSV Schema

Each row represents a single line item on an invoice.

| Column Name | Type | Example | Purpose |
|------------|------|---------|---------|
| transaction_id | String | TXN-1001 | Line-level ID |
| invoice_id | String | INV-2001 | Group items into invoices |
| invoice_date | Date | 2025-01-15 | When the invoice was issued |
| payment_date | Date | 2025-02-02 or empty | When payment was completed |
| due_date | Date | 2025-02-01 | Used for aging |
| customer_id | String | CUST-101 | Customer key |
| customer_segment | String | SMB, Freelancer, Startup | Segmentation |
| product_id | String | PROD-1 | Product key |
| product_name | String | Bookkeeping Software | Display name |
| product_category | String | Subscription, Hardware | Category |
| quantity | Integer | 3 | Units sold |
| unit_price | Double | 49.99 | Price per unit |
| discount_amount | Double | 5.00 | Discount per line |
| tax_amount | Double | 3.75 | Tax per line |
| cost_of_goods_sold | Double | 20.00 | Per-unit cost |
| currency | String | USD | Currency code |
| region | String | CA, TX, NY | Geographic region |
| country | String | US | Country |
| channel | String | Online, Retail, Partner | Sales channel |
| is_refund | Boolean | true / false | Refund flag |
| is_subscription | Boolean | true / false | Subscription item |
| subscription_plan | String | Monthly, Annual | Plan type |
| payment_status | String | PAID, PARTIAL, UNPAID | Invoice payment state |
| tax_jurisdiction | String | CA-STATE, CA-LOCAL | Tax grouping key |

## Dataset Assumptions

1. **Single Currency**: Each dataset uses a single currency (typically USD). No foreign exchange conversions are performed.

2. **Single Company Context**: The dataset represents a single company's sales. No consolidation across multiple entities is performed.

3. **Partial Payments**: Partial payments are simplified and modeled via `payment_status` and a single `payment_date`. More complex partial payment scenarios are not currently supported.

4. **Tax Calculation**: `tax_amount` is stored directly in the CSV and not recalculated from tax rules. The generator calculates tax as a percentage of the net amount (after discounts).

5. **Refunds**: Refunds are represented as separate rows with `is_refund = true`. The net amount for refund rows is negative, contributing negatively to net revenue.

6. **Cost of Goods Sold**: `cost_of_goods_sold` is constant per product over time. The generator assigns a cost between 30-70% of the unit price for each product.

7. **Payment Terms**: Payment terms are typically 15-30 days from invoice date. Some invoices may be paid late, enabling aging analysis.

8. **Date Range**: By default, the generator creates records spanning the last 24 months. This can be customized when instantiating the generator.

## Data Generation

The `CsvDataGenerator` class generates realistic synthetic data with:

- **Multiple Customers**: 50 unique customers across different segments
- **Product Catalog**: 12 different products across 4 categories
- **Geographic Diversity**: 6 regions (CA, TX, NY, FL, IL, WA)
- **Sales Channels**: Online, Retail, Partner
- **Payment Mix**: Paid, partially paid, and unpaid invoices
- **Payment Timing**: On-time and late payments
- **Refunds**: Approximately 5% of transactions are refunds
- **Discounts**: Approximately 30% of transactions include discounts
- **Subscriptions**: Subscription products have recurring monthly/quarterly/annual plans

## Usage

Generate a dataset:

```bash
# First compile the project (see README.md)
# Then run:
java -cp out com.dataanalysis.app.Main generate 5000 data/sales.csv
```

This creates a CSV file with 5000 line items (approximately 1000-2500 invoices, depending on line items per invoice).

## File Format

- **Encoding**: UTF-8
- **Delimiter**: Comma (,)
- **Header Row**: First row contains column names
- **Date Format**: YYYY-MM-DD (ISO 8601)
- **Boolean Values**: true/false (lowercase)
- **Empty Values**: Empty strings for optional fields (e.g., payment_date for unpaid invoices)

## Validation

The `CsvSalesLoader` validates each row:

- All required fields must be present
- Dates must be in valid format
- Numeric fields must be parseable
- Boolean fields are parsed flexibly (true/1/yes)

Invalid rows are logged and skipped, allowing the analysis to proceed with valid data.
