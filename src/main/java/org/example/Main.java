package org.example;

import org.example.model.*;
import org.example.model.exceptions.*;
import org.example.service.AccountManager;
import org.example.service.FilePersistenceService;
import org.example.service.StatementGenerator;
import org.example.service.TransactionManager;
import org.example.utils.ConcurrencyUtils;
import org.example.utils.FunctionalUtils;
import org.example.utils.ValidationUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * Entry point for the Bank Account Management System (Lab 3).
 * <p>
 * Enhancements over Lab 2:
 * <ul>
 *   <li>Collections: ArrayList + HashMap replace fixed arrays</li>
 *   <li>File Persistence: load on startup, save on exit (NIO)</li>
 *   <li>Regex Validation: email validation via {@link ValidationUtils}</li>
 *   <li>Concurrency: thread-safe simulation via {@link ConcurrencyUtils}</li>
 *   <li>Functional: stream analytics via {@link FunctionalUtils}</li>
 * </ul>
 * Menu now has 6 options (adds Save/Load Data and Concurrent Simulation).
 * </p>
 */
public class Main {

    // -------------------------------------------------------------------------
    // Data file paths (relative to project root)
    // -------------------------------------------------------------------------

    private static final Path DATA_DIR        = Paths.get("data");
    private static final Path ACCOUNTS_FILE   = DATA_DIR.resolve("accounts.txt");
    private static final Path TRANSACTIONS_FILE = DATA_DIR.resolve("transactions.txt");

    // -------------------------------------------------------------------------
    // Shared state
    // -------------------------------------------------------------------------

    private static final AccountManager       accountManager    = new AccountManager();
    private static final TransactionManager   transactionManager = new TransactionManager();
    private static final StatementGenerator   statementGenerator = new StatementGenerator();
    private static final FilePersistenceService persistence      = new FilePersistenceService();
    private static final Scanner              scanner           = new Scanner(System.in);

    // -------------------------------------------------------------------------
    // Entry point
    // -------------------------------------------------------------------------

    /**
     * Application entry point.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        loadDataOrInitialize();
        runMainLoop();
        autoSaveOnExit();
        scanner.close();
    }

    // -------------------------------------------------------------------------
    // Startup: load or seed
    // -------------------------------------------------------------------------

    /**
     * Attempts to load persisted data from disk.
     * Falls back to sample data if no files exist yet.
     */
    private static void loadDataOrInitialize() {
        List<Account>     loadedAccounts     = persistence.loadAccounts(ACCOUNTS_FILE);
        List<Transaction> loadedTransactions = persistence.loadTransactions(TRANSACTIONS_FILE);

        if (!loadedAccounts.isEmpty()) {
            accountManager.replaceAll(loadedAccounts);
            transactionManager.replaceAll(loadedTransactions);
            System.out.println("\n✓ Data loaded from disk (" + loadedAccounts.size() + " accounts).");
        } else {
            System.out.println("\nNo saved data found. Initializing with sample data...");
            initializeSampleData();
        }
    }

    /** Auto-saves data when the application exits cleanly. */
    private static void autoSaveOnExit() {
        persistence.saveAll(
                accountManager.getAllAccounts(),
                transactionManager.getAllTransactions(),
                ACCOUNTS_FILE, TRANSACTIONS_FILE);
    }

    // -------------------------------------------------------------------------
    // Main loop
    // -------------------------------------------------------------------------

    /** Displays the main menu and dispatches to the appropriate handler. */
    private static void runMainLoop() {
        boolean running = true;
        while (running) {
            displayMainMenu();
            int choice = getIntInput("Enter your choice: ");
            switch (choice) {
                case 1 -> manageAccounts();
                case 2 -> performTransactions();
                case 3 -> generateStatement();
                case 4 -> saveLoadMenu();
                case 5 -> runConcurrentSimulation();
                case 6 -> {
                    printExit();
                    running = false;
                }
                default -> System.out.println("\n❌ Invalid choice. Please select 1–6.");
            }
            if (running) pauseForUser();
        }
    }

    /** Prints the main menu. */
    private static void displayMainMenu() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("   BANK ACCOUNT MANAGEMENT SYSTEM — Lab 3");
        System.out.println("=".repeat(50));
        System.out.println("  Main Menu:");
        System.out.println("  " + "-".repeat(30));
        System.out.println("  1. Manage Accounts");
        System.out.println("  2. Perform Transactions");
        System.out.println("  3. Generate Account Statements");
        System.out.println("  4. Save / Load Data");
        System.out.println("  5. Run Concurrent Simulation");
        System.out.println("  6. Exit");
        System.out.println("=".repeat(50));
    }

    // -------------------------------------------------------------------------
    // Option 1: Manage Accounts
    // -------------------------------------------------------------------------

    /** Sub-menu for account management. */
    private static void manageAccounts() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("MANAGE ACCOUNTS");
            System.out.println("=".repeat(50));
            System.out.println("1. Create New Account");
            System.out.println("2. View All Accounts");
            System.out.println("3. Back to Main Menu");
            int sub = getIntInputInRange("Select option: ", 1, 3);

            if (sub == 1) {
                createAccount();
            } else if (sub == 2) {
                accountManager.viewAllAccounts();
            } else {
                return;
            }
        }
    }

    /** Guides the user through creating a new account with full validation (incl. email). */
    private static void createAccount() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("ACCOUNT CREATION");
        System.out.println("=".repeat(50));

        String name    = readValidName("Enter customer name: ", "Name");
        int    age     = readValidAge("Enter customer age: ");
        String email   = readValidEmail("Enter customer email: ");
        String contact = readNonBlank("Enter customer contact: ");
        String address = readNonBlank("Enter customer address: ");

        System.out.println("\nCustomer type:");
        System.out.println("  1. Regular Customer");
        System.out.println("  2. Premium Customer (min balance $10,000)");
        int customerType = getIntInputInRange("Select type (1-2): ", 1, 2);

        Customer customer = (customerType == 2)
                ? new PremiumCustomer(name, age, email, contact, address)
                : new RegularCustomer(name, age, email, contact, address);

        System.out.println("\nAccount type:");
        System.out.println("  1. Savings Account (Interest: 3.5%, Min Balance: $500)");
        System.out.println("  2. Checking Account (Overdraft: $1,000, Monthly Fee: $10)");
        int accountType = getIntInputInRange("Select type (1-2): ", 1, 2);

        double initialDeposit = getPositiveAmount("Enter initial deposit amount: $");

        Account account = (accountType == 1)
                ? new SavingsAccount(customer, initialDeposit)
                : new CheckingAccount(customer, initialDeposit);

        accountManager.addAccount(account);

        System.out.println("\n✓ Account created successfully!");
        System.out.println("=".repeat(50));
        account.displayAccountDetails();
        System.out.println("=".repeat(50));
    }

    // -------------------------------------------------------------------------
    // Option 2: Perform Transactions
    // -------------------------------------------------------------------------

    /** Sub-menu for performing deposits, withdrawals, and transfers. */
    private static void performTransactions() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("PROCESS TRANSACTION");
        System.out.println("=".repeat(50));

        Account account = readExistingAccount("Enter Account Number: ");

        System.out.println("\nCustomer : " + account.getCustomer().getName());
        System.out.println("Type     : " + account.getAccountType());
        System.out.printf ("Balance  : $%,.2f%n", account.getBalance());

        while (true) {
            System.out.println("\nTransaction type:");
            System.out.println("  1. Deposit");
            System.out.println("  2. Withdrawal");
            System.out.println("  3. Transfer");
            int txType = getIntInputInRange("Select type (1-3): ", 1, 3);

            boolean completed;
            if (txType == 1) {
                completed = handleDeposit(account);
            } else if (txType == 2) {
                completed = handleWithdrawal(account);
            } else {
                completed = handleTransfer(account);
            }

            if (completed) return;
            System.out.println("Operation canceled. Returning to transaction menu.");
        }
    }

    private static boolean handleDeposit(Account account) {
        while (true) {
            double amount = getPositiveAmount("Enter deposit amount: $");
            if (!confirmTransaction()) return false;
            try {
                transactionManager.deposit(account, amount);
                System.out.printf("%n✓ Deposit successful! New balance: $%,.2f%n", account.getBalance());
                return true;
            } catch (InvalidAmountException e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static boolean handleWithdrawal(Account account) {
        while (true) {
            double amount = getPositiveAmount("Enter withdrawal amount: $");
            if (!confirmTransaction()) return false;
            try {
                transactionManager.withdraw(account, amount);
                System.out.printf("%n✓ Withdrawal successful! New balance: $%,.2f%n", account.getBalance());
                return true;
            } catch (InvalidAmountException e) {
                System.out.println("❌ Error: " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.printf("❌ Transaction Failed: %s%n", e.getMessage());
            } catch (OverdraftExceededException e) {
                System.out.printf("❌ Transaction Failed: %s (Overdraft limit: $%,.2f)%n",
                        e.getMessage(), e.getOverdraftLimit());
            }
        }
    }

    private static boolean handleTransfer(Account source) {
        Account destination = readExistingAccount("Enter destination Account Number: ");
        while (true) {
            double amount = getPositiveAmount("Enter transfer amount: $");
            if (!confirmTransaction()) return false;
            try {
                transactionManager.transfer(source, destination, amount);
                System.out.printf("%n✓ Transfer successful!%n");
                System.out.printf("  From %-8s — new balance: $%,.2f%n",
                        source.getAccountNumber(), source.getBalance());
                System.out.printf("  To   %-8s — new balance: $%,.2f%n",
                        destination.getAccountNumber(), destination.getBalance());
                return true;
            } catch (InvalidAmountException e) {
                System.out.println("❌ Error: " + e.getMessage());
            } catch (InsufficientFundsException e) {
                System.out.printf("❌ Transaction Failed: %s%n", e.getMessage());
            } catch (OverdraftExceededException e) {
                System.out.printf("❌ Transaction Failed: %s%n", e.getMessage());
            } catch (InvalidAccountException e) {
                System.out.println("❌ Error: " + e.getMessage());
                destination = readExistingAccount("Re-enter destination Account Number: ");
            }
        }
    }

    // -------------------------------------------------------------------------
    // Option 3: Generate Statement
    // -------------------------------------------------------------------------

    private static void generateStatement() {
        Account account = readExistingAccount("Enter Account Number: ");
        statementGenerator.generate(account, transactionManager);
    }

    // -------------------------------------------------------------------------
    // Option 4: Save / Load Data
    // -------------------------------------------------------------------------

    /** Sub-menu for file persistence and stream analytics. */
    private static void saveLoadMenu() {
        while (true) {
            System.out.println("\n" + "=".repeat(50));
            System.out.println("SAVE / LOAD DATA");
            System.out.println("=".repeat(50));
            System.out.println("1. Save all data to disk");
            System.out.println("2. Reload data from disk");
            System.out.println("3. View stream analytics");
            System.out.println("4. View transactions sorted by amount");
            System.out.println("5. Back to Main Menu");
            int sub = getIntInputInRange("Select option: ", 1, 5);

            switch (sub) {
                case 1 -> persistence.saveAll(
                        accountManager.getAllAccounts(),
                        transactionManager.getAllTransactions(),
                        ACCOUNTS_FILE, TRANSACTIONS_FILE);
                case 2 -> reloadFromDisk();
                case 3 -> FunctionalUtils.printAnalyticsSummary(
                        accountManager.getAllAccounts(),
                        transactionManager.getAllTransactions());
                case 4 -> showSortedTransactions();
                case 5 -> { return; }
            }
        }
    }

    /** Reloads all data from disk, replacing current in-memory state. */
    private static void reloadFromDisk() {
        List<Account> accts = persistence.loadAccounts(ACCOUNTS_FILE);
        List<Transaction> txns = persistence.loadTransactions(TRANSACTIONS_FILE);
        if (accts.isEmpty()) {
            System.out.println("⚠ No saved data found. Current data unchanged.");
        } else {
            accountManager.replaceAll(accts);
            transactionManager.replaceAll(txns);
            System.out.println("✓ Data reloaded successfully.");
        }
    }

    /** Shows all transactions sorted by amount (descending). */
    private static void showSortedTransactions() {
        var sorted = FunctionalUtils.sortTransactionsByAmount(
                transactionManager.getAllTransactions(), true);
        if (sorted.isEmpty()) {
            System.out.println("\nNo transactions recorded yet.");
            return;
        }
        System.out.println("\nTRANSACTIONS SORTED BY AMOUNT (highest first):");
        System.out.println("-".repeat(65));
        // Stream + filter + sorted (lab spec example)
        sorted.stream()
                .filter(t -> t.getType().startsWith("DEPOSIT"))
                .forEach(t -> System.out.printf("  DEPOSIT  %-8s  $%,.2f  (%s)%n",
                        t.getAccountNumber(), t.getAmount(), t.getTimestamp()));
        System.out.println();
        sorted.stream()
                .filter(t -> t.getType().startsWith("WITHDRAWAL"))
                .forEach(t -> System.out.printf("  WITHDRAW %-8s  $%,.2f  (%s)%n",
                        t.getAccountNumber(), t.getAmount(), t.getTimestamp()));
    }

    // -------------------------------------------------------------------------
    // Option 5: Run Concurrent Simulation
    // -------------------------------------------------------------------------

    /** Prompts user for an account and launches the concurrent thread simulation. */
    private static void runConcurrentSimulation() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("CONCURRENT SIMULATION");
        System.out.println("=".repeat(50));
        System.out.println("This simulation runs 3 threads simultaneously on one account:");
        System.out.println("  Thread-1: deposits $500");
        System.out.println("  Thread-2: deposits $300");
        System.out.println("  Thread-3: withdraws $200");
        System.out.println();

        Account account = readExistingAccount("Enter Account Number to use: ");
        ConcurrencyUtils.runConcurrentSimulation(account, transactionManager);
    }

    // -------------------------------------------------------------------------
    // Exit
    // -------------------------------------------------------------------------

    private static void printExit() {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Thank you for using the Bank Account Management System!");
        System.out.println("Data will be saved before exit...");
        System.out.println("Remember to commit your latest changes to Git!");
        System.out.println("Goodbye!");
        System.out.println("=".repeat(50));
    }

    // -------------------------------------------------------------------------
    // Sample data
    // -------------------------------------------------------------------------

    /** Initialises sample accounts and performs some initial transactions. */
    private static void initializeSampleData() {
        Customer c1 = new RegularCustomer("Mpamo Avy",     35, "mpamo@bank.com",    "+250-789-331-259", "KG 480, Kigali");
        Customer c2 = new RegularCustomer("Igabe Lanuja",  28, "igabe@bank.com",    "+250-782-471-299", "KG 340, Kigali");
        Customer c3 = new PremiumCustomer("Bigwi Axel",    45, "bigwi@bank.com",    "+250-781-437-239", "KG 261, Kigali");
        Customer c4 = new RegularCustomer("Ineza Annick",  31, "ineza@bank.com",    "+250-788-831-282", "KG 453, Kigali");
        Customer c5 = new PremiumCustomer("Nkota Leslie",  52, "nkota@bank.com",    "+250-788-301-245", "KG 367, Kigali");

        Account a1 = new SavingsAccount(c1,   5250.0);
        Account a2 = new CheckingAccount(c2,  3450.0);
        Account a3 = new SavingsAccount(c3,  15750.0);
        Account a4 = new CheckingAccount(c4,   880.0);
        Account a5 = new SavingsAccount(c5,  25200.0);

        accountManager.addAccount(a1);
        accountManager.addAccount(a2);
        accountManager.addAccount(a3);
        accountManager.addAccount(a4);
        accountManager.addAccount(a5);

        // Seed some initial transactions for demo purposes
        try {
            transactionManager.deposit(a1, 1500.0);
            transactionManager.withdraw(a1, 750.0);
            transactionManager.deposit(a2, 500.0);
            transactionManager.deposit(a3, 2000.0);
            transactionManager.withdraw(a2, 200.0);
        } catch (Exception e) {
            // Seed failures should not crash startup
        }
    }

    // -------------------------------------------------------------------------
    // Input helpers
    // -------------------------------------------------------------------------

    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a whole number.");
            }
        }
    }

    private static int getIntInputInRange(String prompt, int min, int max) {
        while (true) {
            int value = getIntInput(prompt);
            if (value >= min && value <= max) return value;
            System.out.println("❌ Please enter a number between " + min + " and " + max + ".");
        }
    }

    private static double getPositiveAmount(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                String raw = scanner.nextLine().trim().replace("$", "").replace(",", "");
                double value = Double.parseDouble(raw);
                if (value <= 0) {
                    System.out.println("❌ Error: Amount must be greater than 0.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid amount.");
            }
        }
    }

    private static String readValidName(String prompt, String field) {
        while (true) {
            String value = readNonBlank(prompt);
            try {
                ValidationUtils.validateName(value, field);
                return value;
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static int readValidAge(String prompt) {
        while (true) {
            int age = getIntInput(prompt);
            try {
                ValidationUtils.validateAge(age);
                return age;
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    /**
     * Reads and validates an email address using {@link ValidationUtils#validateEmail}.
     * Shows user-friendly error message on failure and re-prompts.
     *
     * @param prompt the display prompt
     * @return a validated email string
     */
    private static String readValidEmail(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            try {
                ValidationUtils.validateEmail(value);
                System.out.println("✓ Email accepted!");
                return value;
            } catch (IllegalArgumentException e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static Account readExistingAccount(String prompt) {
        while (true) {
            System.out.print(prompt);
            String accountNumber = scanner.nextLine().trim().toUpperCase();
            try {
                return accountManager.findAccount(accountNumber);
            } catch (InvalidAccountException e) {
                System.out.println("❌ Error: " + e.getMessage());
            }
        }
    }

    private static boolean confirmTransaction() {
        while (true) {
            System.out.print("Do you want to proceed? (yes/no): ");
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("yes")) return true;
            if (input.equalsIgnoreCase("no")) return false;
            System.out.println("❌ Invalid input. Please enter yes or no.");
        }
    }

    private static String readNonBlank(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = scanner.nextLine().trim();
            if (!value.isBlank()) return value;
            System.out.println("❌ This field cannot be empty.");
        }
    }

    private static void pauseForUser() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}