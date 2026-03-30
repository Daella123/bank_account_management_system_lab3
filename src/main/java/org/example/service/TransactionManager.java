package org.example.service;

import org.example.model.Account;
import org.example.model.Transaction;
import org.example.model.exceptions.InsufficientFundsException;
import org.example.model.exceptions.InvalidAccountException;
import org.example.model.exceptions.InvalidAmountException;
import org.example.model.exceptions.OverdraftExceededException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manages all transaction records and provides thread-safe deposit,
 * withdrawal, and transfer operations.
 * <p>
 * Stores transactions in an {@link ArrayList}. All financial operations
 * that mutate shared account state are {@code synchronized} on the
 * target account object to prevent race conditions during concurrent
 * simulation.
 * </p>
 */
public class TransactionManager {

    private final List<Transaction> transactions = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Transaction recording
    // -------------------------------------------------------------------------

    /**
     * Appends a {@link Transaction} to the in-memory list.
     *
     * @param transaction the transaction to record
     */
    public synchronized void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    // -------------------------------------------------------------------------
    // Financial operations (thread-safe via synchronized on account)
    // -------------------------------------------------------------------------

    /**
     * Deposits {@code amount} into {@code account} and records the transaction.
     * Synchronized on the account object to prevent concurrent balance corruption.
     *
     * @param account the target account
     * @param amount  the positive amount to deposit
     * @return the recorded {@link Transaction}
     * @throws InvalidAmountException if {@code amount} is zero or negative
     */
    public Transaction deposit(Account account, double amount)
            throws InvalidAmountException {
        synchronized (account) {
            account.deposit(amount);
            Transaction txn = new Transaction(account.getAccountNumber(), "DEPOSIT", amount, account.getBalance());
            addTransaction(txn);
            return txn;
        }
    }

    /**
     * Withdraws {@code amount} from {@code account} and records the transaction.
     * Synchronized on the account object to prevent concurrent balance corruption.
     *
     * @param account the source account
     * @param amount  the positive amount to withdraw
     * @return the recorded {@link Transaction}
     * @throws InvalidAmountException      if {@code amount} is zero or negative
     * @throws InsufficientFundsException  if the account has insufficient funds
     * @throws OverdraftExceededException  if the withdrawal exceeds the overdraft limit
     */
    public Transaction withdraw(Account account, double amount)
            throws InvalidAmountException, InsufficientFundsException,
                   OverdraftExceededException {
        synchronized (account) {
            account.withdraw(amount);
            Transaction txn = new Transaction(account.getAccountNumber(), "WITHDRAWAL", amount, account.getBalance());
            addTransaction(txn);
            return txn;
        }
    }

    /**
     * Transfers {@code amount} from {@code source} to {@code destination}.
     * Both accounts are locked in a consistent order (by account number) to
     * avoid deadlock.
     *
     * @param source      the account to debit
     * @param destination the account to credit
     * @param amount      the positive amount to transfer
     * @throws InvalidAmountException      if {@code amount} is zero or negative
     * @throws InsufficientFundsException  if the source has insufficient funds
     * @throws OverdraftExceededException  if the debit exceeds the overdraft limit
     * @throws InvalidAccountException     if source and destination are the same account
     */
    public void transfer(Account source, Account destination, double amount)
            throws InvalidAmountException, InsufficientFundsException,
                   OverdraftExceededException, InvalidAccountException {
        if (source.getAccountNumber().equals(destination.getAccountNumber())) {
            throw new InvalidAccountException(
                    "Transfer failed: source and destination accounts cannot be the same.");
        }
        // Lock in consistent order to prevent deadlock
        Account first  = source.getAccountNumber().compareTo(destination.getAccountNumber()) < 0 ? source : destination;
        Account second = first == source ? destination : source;
        synchronized (first) {
            synchronized (second) {
                source.withdraw(amount);
                destination.deposit(amount);
                String note = "→ " + destination.getAccountNumber();
                addTransaction(new Transaction(source.getAccountNumber(),
                        "TRANSFER_OUT(" + note + ")", amount, source.getBalance()));
                addTransaction(new Transaction(destination.getAccountNumber(),
                        "TRANSFER_IN(← " + source.getAccountNumber() + ")",
                        amount, destination.getBalance()));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Querying & display
    // -------------------------------------------------------------------------

    /**
     * Prints all transactions for {@code accountNumber} in reverse chronological
     * order (newest first), using stream-based filtering.
     *
     * @param accountNumber the account number to filter by
     */
    public void viewTransactionsByAccount(String accountNumber) {
        List<Transaction> accountTxns = getTransactionsByAccount(accountNumber);

        if (accountTxns.isEmpty()) {
            System.out.println("\nNo transactions recorded for this account.");
            return;
        }

        System.out.println("\nTRANSACTION HISTORY\n");
        System.out.println("TXN ID  | DATE/TIME           | TYPE              | AMOUNT       | BALANCE");
        System.out.println("--------|---------------------|-------------------|--------------|-------------");

        // Reverse order (newest first)
        List<Transaction> reversed = accountTxns.stream()
                .sorted(Comparator.comparing(Transaction::getTransactionId).reversed())
                .collect(Collectors.toList());
        reversed.forEach(this::printTransactionRow);

        printTransactionSummary(accountNumber, accountTxns.size());
    }

    /**
     * Returns all transactions belonging to the given account (stream-filtered).
     *
     * @param accountNumber the account to filter by
     * @return list of matching transactions
     */
    public List<Transaction> getTransactionsByAccount(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber))
                .collect(Collectors.toList());
    }

    /**
     * Prints a single row in the transaction table.
     *
     * @param txn the transaction to print
     */
    private void printTransactionRow(Transaction txn) {
        String sign = txn.getType().startsWith("DEPOSIT")
                      || txn.getType().startsWith("TRANSFER_IN") ? "+" : "-";
        String amountStr = sign + "$" + String.format("%,.2f", txn.getAmount());
        System.out.printf("%-8s| %-20s| %-18s| %-13s| $%s%n",
                txn.getTransactionId(),
                txn.getTimestamp(),
                txn.getType(),
                amountStr,
                String.format("%,.2f", txn.getBalanceAfter()));
    }

    /**
     * Prints the deposit/withdrawal summary for an account.
     *
     * @param accountNumber the account to summarise
     * @param count         the number of transactions found
     */
    private void printTransactionSummary(String accountNumber, int count) {
        double totalDeposits    = calculateTotalDeposits(accountNumber);
        double totalWithdrawals = calculateTotalWithdrawals(accountNumber);
        double netChange        = totalDeposits - totalWithdrawals;

        System.out.println("\nTotal Transactions : " + count);
        System.out.printf ("Total Deposits     : $%,.2f%n", totalDeposits);
        System.out.printf ("Total Withdrawals  : $%,.2f%n", totalWithdrawals);
        System.out.print  ("Net Change         : ");
        System.out.printf ("%s$%,.2f%n", netChange >= 0 ? "+" : "-", Math.abs(netChange));
    }

    // -------------------------------------------------------------------------
    // Aggregates (stream-based)
    // -------------------------------------------------------------------------

    /**
     * Calculates total deposit amounts for a given account using stream reduction.
     *
     * @param accountNumber the account to total
     * @return sum of all deposit amounts
     */
    public double calculateTotalDeposits(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber)
                          && t.getType().startsWith("DEPOSIT"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /**
     * Calculates total withdrawal amounts for a given account using stream reduction.
     *
     * @param accountNumber the account to total
     * @return sum of all withdrawal amounts
     */
    public double calculateTotalWithdrawals(String accountNumber) {
        return transactions.stream()
                .filter(t -> t.getAccountNumber().equals(accountNumber)
                          && (t.getType().startsWith("WITHDRAWAL")
                              || t.getType().startsWith("TRANSFER_OUT")))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** @return an unmodifiable view of all transactions */
    public List<Transaction> getAllTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    /** @return total number of recorded transactions */
    public int getTransactionCount() { return transactions.size(); }

    /**
     * Replaces the current transaction store with a freshly loaded list.
     * Used by {@link FilePersistenceService} after loading from file.
     *
     * @param loaded the transactions loaded from persistence
     */
    public synchronized void replaceAll(List<Transaction> loaded) {
        transactions.clear();
        transactions.addAll(loaded);
    }
}
