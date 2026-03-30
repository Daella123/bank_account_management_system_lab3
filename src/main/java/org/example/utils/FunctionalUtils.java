package org.example.utils;

import org.example.model.Account;
import org.example.model.Transaction;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Utility class providing reusable functional-programming helpers.
 * <p>
 * All methods are stateless and use the Streams API, lambdas, and
 * method references to demonstrate functional programming techniques
 * required by Lab 3.
 * </p>
 */
public final class FunctionalUtils {

    private FunctionalUtils() {}

    // -------------------------------------------------------------------------
    // Transaction helpers
    // -------------------------------------------------------------------------

    /**
     * Filters deposit transactions with amount ≥ {@code minAmount},
     * then sorts them in descending order by amount.
     *
     * @param transactions the full transaction list
     * @param minAmount    minimum deposit threshold
     * @return filtered and sorted list
     */
    public static List<Transaction> filterDepositsByAmount(
            List<Transaction> transactions, double minAmount) {
        return transactions.stream()
                .filter(t -> t.getType().startsWith("DEPOSIT"))
                .filter(t -> t.getAmount() >= minAmount)
                .sorted(Comparator.comparingDouble(Transaction::getAmount).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Sorts transactions by amount.
     *
     * @param transactions the full transaction list
     * @param descending   {@code true} for largest-first order
     * @return sorted list
     */
    public static List<Transaction> sortTransactionsByAmount(
            List<Transaction> transactions, boolean descending) {
        Comparator<Transaction> cmp = Comparator.comparingDouble(Transaction::getAmount);
        return transactions.stream()
                .sorted(descending ? cmp.reversed() : cmp)
                .collect(Collectors.toList());
    }

    /**
     * Prints all transactions that satisfy a given predicate,
     * using {@code System.out::println} as a method reference.
     *
     * @param transactions full transaction list
     * @param predicate    filter condition
     */
    public static void printFilteredTransactions(
            List<Transaction> transactions, Predicate<Transaction> predicate) {
        transactions.stream()
                .filter(predicate)
                .forEach(System.out::println);   // method reference
    }

    /**
     * Calculates the total amount of all deposits using a stream reduction.
     *
     * @param transactions the full transaction list
     * @return sum of all deposit amounts
     */
    public static double getTotalDeposits(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType().startsWith("DEPOSIT"))
                .mapToDouble(Transaction::getAmount)
                .reduce(0.0, Double::sum);       // explicit reduce for demonstration
    }

    /**
     * Calculates the total amount of all withdrawals using a stream reduction.
     *
     * @param transactions the full transaction list
     * @return sum of all withdrawal amounts
     */
    public static double getTotalWithdrawals(List<Transaction> transactions) {
        return transactions.stream()
                .filter(t -> t.getType().startsWith("WITHDRAWAL")
                          || t.getType().startsWith("TRANSFER_OUT"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    // -------------------------------------------------------------------------
    // Account helpers
    // -------------------------------------------------------------------------

    /**
     * Returns the account with the highest balance using stream max + method reference.
     *
     * @param accounts list of accounts
     * @return an Optional containing the richest account, or empty if list is empty
     */
    public static Optional<Account> getHighestBalanceAccount(List<Account> accounts) {
        return accounts.stream()
                .max(Comparator.comparingDouble(Account::getBalance));
    }

    /**
     * Prints a formatted summary of stream-based analytics to {@code System.out}.
     *
     * @param accounts     all accounts
     * @param transactions all transactions
     */
    public static void printAnalyticsSummary(List<Account> accounts, List<Transaction> transactions) {
        System.out.println("\n" + "=".repeat(55));
        System.out.println("  FUNCTIONAL STREAM ANALYTICS");
        System.out.println("=".repeat(55));

        // Stream: total balance
        double totalBalance = accounts.stream()
                .mapToDouble(Account::getBalance)
                .sum();
        System.out.printf("Total Bank Balance   : $%,.2f%n", totalBalance);

        // Stream: average balance
        double avg = accounts.stream()
                .mapToDouble(Account::getBalance)
                .average()
                .orElse(0.0);
        System.out.printf("Average Balance      : $%,.2f%n", avg);

        // Stream: richest account
        getHighestBalanceAccount(accounts).ifPresent(a ->
                System.out.printf("Highest Balance Acct : %s ($%,.2f)%n",
                        a.getAccountNumber(), a.getBalance()));

        // Stream: total deposits / withdrawals
        System.out.printf("Total Deposits       : $%,.2f%n", getTotalDeposits(transactions));
        System.out.printf("Total Withdrawals    : $%,.2f%n", getTotalWithdrawals(transactions));

        // Lambda: top 3 deposits
        System.out.println("\nTop 3 Deposits (by amount):");
        System.out.println("-".repeat(55));
        filterDepositsByAmount(transactions, 0)
                .stream()
                .limit(3)
                .forEach(t -> System.out.printf("  %-8s  $%,.2f  (%s)%n",
                        t.getAccountNumber(), t.getAmount(), t.getTimestamp()));

        System.out.println("=".repeat(55));
    }
}
