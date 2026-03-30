package org.example.service;

import org.example.model.Account;
import org.example.model.Transaction;

import java.util.List;

/**
 * Generates formatted account statements.
 * <p>
 * A statement includes account metadata, a reverse-chronological transaction
 * list, and a financial summary (total deposits, withdrawals, and net change).
 * This class is completely stateless — each {@link #generate} call is
 * self-contained.
 * </p>
 */
public class StatementGenerator {

    /** Width of the horizontal separator line in output. */
    private static final int SEPARATOR_WIDTH = 60;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Generates and prints a full account statement to {@code System.out}.
     *
     * @param account            the account for which to generate the statement
     * @param transactionManager the manager that holds all transactions
     */
    public void generate(Account account, TransactionManager transactionManager) {
        printHeader(account);
        List<Transaction> relevantTxns = filterTransactions(account, transactionManager);
        int count = relevantTxns.size();
        printTransactions(relevantTxns, count);
        printSummary(account, transactionManager, count);
        System.out.println("\n✓ Statement generated successfully.");
    }

    // -------------------------------------------------------------------------
    // Header section
    // -------------------------------------------------------------------------

    /**
     * Prints the statement header showing account owner and current balance.
     *
     * @param account the account being reported on
     */
    private void printHeader(Account account) {
        System.out.println("\n" + "=".repeat(SEPARATOR_WIDTH));
        System.out.println("GENERATE ACCOUNT STATEMENT");
        System.out.println("=".repeat(SEPARATOR_WIDTH));
        System.out.println("Account      : " + account.getAccountNumber());
        System.out.println("Customer     : " + account.getCustomer().getName()
                + " (" + account.getCustomer().getCustomerType() + ")");
        System.out.println("Account Type : " + account.getAccountType());
        System.out.printf ("Balance      : $%,.2f%n", account.getBalance());
        System.out.println("-".repeat(SEPARATOR_WIDTH));
    }

    // -------------------------------------------------------------------------
    // Transaction section
    // -------------------------------------------------------------------------

    /**
     * Filters all transactions in the manager to those belonging to {@code account}.
     *
     * @param account            the account to filter by
     * @param transactionManager the source of all transactions
     * @return list of matching transactions
     */
    private List<Transaction> filterTransactions(Account account, TransactionManager transactionManager) {
        return transactionManager.getTransactionsByAccount(account.getAccountNumber());
    }

    /**
     * Prints the transaction rows in reverse chronological order (newest first).
     *
     * @param txns  the filtered transactions list
     * @param count the number of valid entries
     */
    private void printTransactions(List<Transaction> txns, int count) {
        System.out.println("\nTransactions (newest first):");
        System.out.println("_".repeat(SEPARATOR_WIDTH));

        if (count == 0) {
            System.out.println("  No transactions recorded for this account.");
            System.out.println("_".repeat(SEPARATOR_WIDTH));
            return;
        }

        System.out.printf("%-8s| %-11s| %-12s| %s%n",
                "TXN ID", "TYPE", "AMOUNT", "BALANCE AFTER");
        System.out.println("-".repeat(SEPARATOR_WIDTH));

        for (int i = txns.size() - 1; i >= 0; i--) {
            printStatementRow(txns.get(i));
        }
        System.out.println("_".repeat(SEPARATOR_WIDTH));
    }

    /**
     * Prints a single formatted row in the statement transaction list.
     *
     * @param txn the transaction to display
     */
    private void printStatementRow(Transaction txn) {
        boolean isCredit = txn.getType().startsWith("DEPOSIT")
                        || txn.getType().startsWith("TRANSFER_IN");
        String sign      = isCredit ? "+" : "-";
        String amountStr = sign + "$" + String.format("%,.2f", txn.getAmount());
        String typeLabel = txn.getType().length() > 11
                           ? txn.getType().substring(0, 11) : txn.getType();

        System.out.printf("%-8s| %-11s| %-12s| $%s%n",
                txn.getTransactionId(),
                typeLabel,
                amountStr,
                String.format("%,.2f", txn.getBalanceAfter()));
    }

    // -------------------------------------------------------------------------
    // Summary section
    // -------------------------------------------------------------------------

    /**
     * Prints financial totals for the statement.
     *
     * @param account            the account being reported
     * @param transactionManager transaction source for aggregate calculations
     * @param count              the total number of transactions found
     */
    private void printSummary(Account account,
                               TransactionManager transactionManager,
                               int count) {
        String accNum = account.getAccountNumber();
        double deposits    = transactionManager.calculateTotalDeposits(accNum);
        double withdrawals = transactionManager.calculateTotalWithdrawals(accNum);
        double netChange   = deposits - withdrawals;

        System.out.println("\nSUMMARY");
        System.out.printf("Total Transactions : %d%n", count);
        System.out.printf("Total Deposits     : +$%,.2f%n", deposits);
        System.out.printf("Total Withdrawals  : -$%,.2f%n", withdrawals);
        System.out.printf("Net Change         : %s$%,.2f%n",
                netChange >= 0 ? "+" : "-", Math.abs(netChange));
        System.out.printf("Current Balance    : $%,.2f%n", account.getBalance());
    }

}
