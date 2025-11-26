package com.dataanalysis.loader;

import com.dataanalysis.model.SalesRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvSalesLoaderTest {

    @Test
    void testLoadValidCsv(@TempDir Path tempDir) throws Exception {
        Path csvFile = tempDir.resolve("sales.csv");
        String csvContent = """
                transaction_id,invoice_id,invoice_date,payment_date,due_date,customer_id,customer_segment,product_id,product_name,product_category,quantity,unit_price,discount_amount,tax_amount,cost_of_goods_sold,currency,region,country,channel,is_refund,is_subscription,subscription_plan,payment_status,tax_jurisdiction
                TXN-1001,INV-2001,2025-01-15,2025-02-02,2025-02-01,CUST-101,SMB,PROD-1,Bookkeeping Software,Subscription,3,49.99,5.00,3.75,20.00,USD,CA,US,Online,false,true,Monthly,PAID,CA-STATE
                TXN-1002,INV-2002,2025-01-20,,2025-02-05,CUST-102,Startup,PROD-2,Tax Tool,Software,1,99.99,0.00,8.00,50.00,USD,TX,US,Retail,false,false,,UNPAID,TX-STATE
                """;

        Files.write(csvFile, csvContent.getBytes());

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvFile);

        assertEquals(2, records.size());
        SalesRecord first = records.get(0);
        assertEquals("TXN-1001", first.transactionId());
        assertEquals("INV-2001", first.invoiceId());
        assertEquals("CUST-101", first.customerId());
        assertEquals(3, first.quantity());
        assertEquals(49.99, first.unitPrice(), 0.01);
        assertTrue(first.isPaid());

        SalesRecord second = records.get(1);
        assertEquals("TXN-1002", second.transactionId());
        assertFalse(second.isPaid());
        assertNull(second.paymentDate());
    }

    @Test
    void testLoadWithInvalidRows(@TempDir Path tempDir) throws Exception {
        Path csvFile = tempDir.resolve("sales.csv");
        String csvContent = """
                transaction_id,invoice_id,invoice_date,payment_date,due_date,customer_id,customer_segment,product_id,product_name,product_category,quantity,unit_price,discount_amount,tax_amount,cost_of_goods_sold,currency,region,country,channel,is_refund,is_subscription,subscription_plan,payment_status,tax_jurisdiction
                TXN-1001,INV-2001,2025-01-15,2025-02-02,2025-02-01,CUST-101,SMB,PROD-1,Bookkeeping Software,Subscription,3,49.99,5.00,3.75,20.00,USD,CA,US,Online,false,true,Monthly,PAID,CA-STATE
                invalid,line,with,too,few,fields
                TXN-1002,INV-2002,2025-01-20,,2025-02-05,CUST-102,Startup,PROD-2,Tax Tool,Software,1,99.99,0.00,8.00,50.00,USD,TX,US,Retail,false,false,,UNPAID,TX-STATE
                """;

        Files.write(csvFile, csvContent.getBytes());

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvFile);

        // Should load 2 valid records and skip the invalid one
        assertEquals(2, records.size());
    }

    @Test
    void testLoadEmptyFile(@TempDir Path tempDir) throws Exception {
        Path csvFile = tempDir.resolve("empty.csv");
        String csvContent = """
                transaction_id,invoice_id,invoice_date,payment_date,due_date,customer_id,customer_segment,product_id,product_name,product_category,quantity,unit_price,discount_amount,tax_amount,cost_of_goods_sold,currency,region,country,channel,is_refund,is_subscription,subscription_plan,payment_status,tax_jurisdiction
                """;

        Files.write(csvFile, csvContent.getBytes());

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvFile);

        assertTrue(records.isEmpty());
    }

    @Test
    void testParseBoolean() throws Exception {
        Path csvFile = Files.createTempFile("test", ".csv");
        String csvContent = """
                transaction_id,invoice_id,invoice_date,payment_date,due_date,customer_id,customer_segment,product_id,product_name,product_category,quantity,unit_price,discount_amount,tax_amount,cost_of_goods_sold,currency,region,country,channel,is_refund,is_subscription,subscription_plan,payment_status,tax_jurisdiction
                TXN-1001,INV-2001,2025-01-15,2025-02-02,2025-02-01,CUST-101,SMB,PROD-1,Product,Category,1,10.00,0.00,0.00,5.00,USD,CA,US,Online,true,false,,PAID,CA-STATE
                TXN-1002,INV-2002,2025-01-15,2025-02-02,2025-02-01,CUST-101,SMB,PROD-1,Product,Category,1,10.00,0.00,0.00,5.00,USD,CA,US,Online,false,true,Monthly,PAID,CA-STATE
                """;

        Files.write(csvFile, csvContent.getBytes());

        CsvSalesLoader loader = new CsvSalesLoader();
        List<SalesRecord> records = loader.load(csvFile);

        assertEquals(2, records.size());
        assertTrue(records.get(0).isRefund());
        assertFalse(records.get(0).isSubscription());
        assertFalse(records.get(1).isRefund());
        assertTrue(records.get(1).isSubscription());
    }
}

