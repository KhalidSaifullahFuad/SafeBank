package tests;

import models.Account;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import services.AccountService;
import services.TransactionService;
import utils.JDBCUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;

public class TransactionServiceTest {
    private AccountService accountService;
    private TransactionService transactionService;

    @Before
    public void setUp() {
        accountService = new AccountService();
        transactionService = new TransactionService();

        try (Connection conn = JDBCUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE Transactions, Accounts RESTART IDENTITY");
            stmt.execute("INSERT INTO Accounts (account_holder_name, balance) VALUES ('TestUser1', 10000), ('TestUser2', 20000)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() {
        try (Connection conn = JDBCUtil.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE Transactions, Accounts RESTART IDENTITY");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testConcurrentTransactions() throws InterruptedException {
        int numberOfThreads = 10;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        for (int i = 0; i < numberOfThreads; i++) {
            new Thread(() -> {
                try {
                    transactionService.deposit(1, new BigDecimal("100.00"));
                    transactionService.withdraw(1, new BigDecimal("50.00"));
                    transactionService.transfer(1, 2, new BigDecimal("200.00"));
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        Account account1 = accountService.getAccountDetails(1);
        Account account2 = accountService.getAccountDetails(2);

        BigDecimal expectedBalanceAccount1 = new BigDecimal("1000.00")
                .add(new BigDecimal("100.00").multiply(new BigDecimal(numberOfThreads)))
                .subtract(new BigDecimal("50.00").multiply(new BigDecimal(numberOfThreads)))
                .subtract(new BigDecimal("200.00").multiply(new BigDecimal(numberOfThreads)));

        BigDecimal expectedBalanceAccount2 = new BigDecimal("2000.00")
                .add(new BigDecimal("200.00").multiply(new BigDecimal(numberOfThreads)));

        assertEquals(expectedBalanceAccount1, account1.getBalance());
        assertEquals(expectedBalanceAccount2, account2.getBalance());
    }
}
