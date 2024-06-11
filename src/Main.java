import models.Account;
import services.AccountService;
import services.TransactionService;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        AccountService accountService = new AccountService();
        TransactionService transactionService = new TransactionService();

        accountService.createAccount("Fuad", new BigDecimal("1000.00"));
        accountService.createAccount("Mahedi", new BigDecimal("2000.00"));

        Runnable depositRunnable = new Runnable() {
            @Override
            public void run() {
                transactionService.deposit(1, new BigDecimal("100.00"));
            }
        };

        Runnable withdrawRunnable = new Runnable() {
            @Override
            public void run() {
                transactionService.withdraw(1, new BigDecimal("50.00"));
            }
        };

        Runnable transferRunnable = new Runnable() {
            @Override
            public void run() {
                transactionService.transfer(1, 2, new BigDecimal("200.00"));
            }
        };

        Thread depositThread = new Thread(depositRunnable);
        Thread withdrawThread = new Thread(withdrawRunnable);
        Thread transferThread = new Thread(transferRunnable);

        depositThread.start();
        withdrawThread.start();
        transferThread.start();

        try {
            depositThread.join();
            withdrawThread.join();
            transferThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Account account1 = accountService.getAccountDetails(1);
        Account account2 = accountService.getAccountDetails(2);

        System.out.println("Final balance of account 1: " + account1.getBalance());
        System.out.println("Final balance of account 2: " + account2.getBalance());
    }
}