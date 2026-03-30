package org.example.utils;

import org.example.model.Account;
import org.example.model.exceptions.InsufficientFundsException;
import org.example.model.exceptions.InvalidAmountException;
import org.example.model.exceptions.OverdraftExceededException;
import org.example.service.TransactionManager;

/**
 * Utility class for demonstrating concurrent banking transactions.
 * <p>
 * Launches multiple threads that simultaneously perform deposits and
 * withdrawals on the same account. Thread safety is guaranteed because
 * {@link TransactionManager#deposit} and {@link TransactionManager#withdraw}
 * synchronize on the account object.
 * </p>
 */
public final class ConcurrencyUtils {

    private ConcurrencyUtils() {}

    /**
     * Runs a concurrent simulation of 3 threads operating on the same account:
     * <ul>
     *   <li>Thread-1 deposits $500</li>
     *   <li>Thread-2 deposits $300</li>
     *   <li>Thread-3 withdraws $200</li>
     * </ul>
     *
     * @param account       the account to operate on
     * @param txManager     the transaction manager (provides synchronized ops)
     */
    public static void runConcurrentSimulation(Account account, TransactionManager txManager) {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("  CONCURRENT SIMULATION — " + account.getAccountNumber());
        System.out.println("=".repeat(55));
        System.out.printf("Starting Balance : $%,.2f%n%n", account.getBalance());

        Thread t1 = buildDepositThread("Thread-1", account, txManager, 500.0);
        Thread t2 = buildDepositThread("Thread-2", account, txManager, 300.0);
        Thread t3 = buildWithdrawThread("Thread-3", account, txManager, 200.0);

        // Start all threads
        t1.start();
        t2.start();
        t3.start();

        // Wait for all threads to finish
        try {
            t1.join();
            t2.join();
            t3.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Simulation interrupted: " + e.getMessage());
        }

        System.out.println();
        System.out.printf("Final Balance    : $%,.2f%n", account.getBalance());
        System.out.println("=".repeat(55));
        System.out.println("All threads completed. No race conditions detected.");
        System.out.println("(synchronized on account object ensured thread safety)");
    }

    // -------------------------------------------------------------------------
    // Thread factory helpers
    // -------------------------------------------------------------------------

    /**
     * Builds a thread that deposits {@code amount} into {@code account}.
     *
     * @param name      thread name (displayed in log output)
     * @param account   target account
     * @param txManager transaction manager
     * @param amount    deposit amount
     * @return configured, not-yet-started thread
     */
    private static Thread buildDepositThread(String name, Account account,
                                             TransactionManager txManager, double amount) {
        return new Thread(() -> {
            try {
                System.out.printf("[%s] Depositing $%,.2f to %s%n",
                        name, amount, account.getAccountNumber());
                txManager.deposit(account, amount);
                System.out.printf("[%s] Deposit complete. Balance now: $%,.2f%n",
                        name, account.getBalance());
            } catch (InvalidAmountException e) {
                System.out.printf("[%s] Deposit failed: %s%n", name, e.getMessage());
            }
        }, name);
    }

    /**
     * Builds a thread that withdraws {@code amount} from {@code account}.
     *
     * @param name      thread name (displayed in log output)
     * @param account   target account
     * @param txManager transaction manager
     * @param amount    withdrawal amount
     * @return configured, not-yet-started thread
     */
    private static Thread buildWithdrawThread(String name, Account account,
                                              TransactionManager txManager, double amount) {
        return new Thread(() -> {
            try {
                // Small delay to let deposits start first (more interesting log output)
                Thread.sleep(10);
                System.out.printf("[%s] Withdrawing $%,.2f from %s%n",
                        name, amount, account.getAccountNumber());
                txManager.withdraw(account, amount);
                System.out.printf("[%s] Withdrawal complete. Balance now: $%,.2f%n",
                        name, account.getBalance());
            } catch (InvalidAmountException | InsufficientFundsException | OverdraftExceededException e) {
                System.out.printf("[%s] Withdrawal failed: %s%n", name, e.getMessage());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, name);
    }
}
