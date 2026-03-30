# Git Workflow — Bank Account Management System (Lab 2)

## Branch Strategy

This project uses a feature-branch workflow with three development branches
that are eventually merged into `main`.

```
main
├── feature/refactor      (Phase 1 — Clean code & JavaDoc)
├── feature/exceptions    (Phase 2 — Custom exceptions & validation)
└── feature/testing       (Phase 3 — JUnit 5 unit tests)
```

---

## Branch Details

### `main`
- Contains the stable, production-ready code at each milestone.
- All feature branches are merged here after review.

### `feature/refactor`
**Files:**
- `src/main/java/org/example/Main.java` (refactored menu)
- `src/main/java/org/example/model/Account.java`
- `src/main/java/org/example/model/SavingsAccount.java`
- `src/main/java/org/example/model/CheckingAccount.java`
- `src/main/java/org/example/model/Customer.java`
- `src/main/java/org/example/model/RegularCustomer.java`
- `src/main/java/org/example/model/PremiumCustomer.java`
- `src/main/java/org/example/model/Transaction.java`
- `src/main/java/org/example/service/AccountManager.java`
- `src/main/java/org/example/service/TransactionManager.java`
- `src/main/java/org/example/service/StatementGenerator.java`

**Commits to make:**
```
git checkout -b feature/refactor
git add .
git commit -m "Refactor: clean up Account, SavingsAccount, CheckingAccount with JavaDoc"
git commit -m "Refactor: modularize AccountManager and TransactionManager methods"
git commit -m "Refactor: add StatementGenerator service for account statements"
git commit -m "Refactor: update Main.java with 5-option menu and better UX"
```

---

### `feature/exceptions`
**Files:**
- `src/main/java/org/example/model/exceptions/InvalidAmountException.java`
- `src/main/java/org/example/model/exceptions/InsufficientFundsException.java`
- `src/main/java/org/example/model/exceptions/InvalidAccountException.java`
- `src/main/java/org/example/model/exceptions/OverdraftExceededException.java`
- `src/main/java/org/example/utils/ValidationUtils.java`

**Commits to make:**
```
git checkout -b feature/exceptions
git add src/main/java/org/example/model/exceptions/
git commit -m "Exceptions: add InvalidAmountException and InsufficientFundsException"
git commit -m "Exceptions: add OverdraftExceededException and InvalidAccountException"
git add src/main/java/org/example/utils/ValidationUtils.java
git commit -m "Exceptions: add ValidationUtils with reusable validation helpers"
```

---

### `feature/testing`
**Files:**
- `src/test/java/org/example/AccountTest.java`
- `src/test/java/org/example/TransactionManagerTest.java`
- `src/test/java/org/example/ExceptionTest.java`
- `pom.xml` (JUnit 5 dependency + Surefire plugin)

**Commits to make:**
```
git checkout -b feature/testing
git add pom.xml
git commit -m "Testing: add JUnit 5 (Jupiter) dependency and Surefire plugin to pom.xml"
git add src/test/java/org/example/AccountTest.java
git commit -m "Testing: add AccountTest covering deposit, withdraw, and overdraft cases"
git add src/test/java/org/example/TransactionManagerTest.java
git commit -m "Testing: add TransactionManagerTest covering deposit, withdraw, transfer"
git add src/test/java/org/example/ExceptionTest.java
git commit -m "Testing: add ExceptionTest verifying all custom exception scenarios"
```

---

## Merging Strategy

### Merge feature branches into main

```bash
# Merge refactoring
git checkout main
git merge feature/refactor
git commit -m "Merge: feature/refactor into main"

# Merge exceptions
git checkout main
git merge feature/exceptions
git commit -m "Merge: feature/exceptions into main"

# Merge tests
git checkout main
git merge feature/testing
git commit -m "Merge: feature/testing into main"
```

---

## Cherry-Pick Example

To bring a specific commit (e.g., the ValidationUtils commit) from
`feature/exceptions` into `feature/testing`:

```bash
# Get the commit hash
git log feature/exceptions --oneline

# Cherry-pick it
git checkout feature/testing
git cherry-pick <commit-hash-of-ValidationUtils>
git commit -m "Cherry-pick: bring ValidationUtils into testing branch"
```

---

## Running Tests

```bash
# Run all JUnit 5 tests via Maven
mvn test

# Run a single test class
mvn test -Dtest=AccountTest
mvn test -Dtest=TransactionManagerTest
mvn test -Dtest=ExceptionTest
```

---

## Commit Message Convention

Follow the format: `<Type>: <Short description>`

| Type       | When to use                             |
|------------|-----------------------------------------|
| `Refactor` | Code restructuring without behaviour change |
| `Exceptions` | Adding or modifying custom exceptions |
| `Testing`  | Adding or modifying test classes         |
| `Fix`      | Bug fix                                  |
| `Docs`     | Documentation updates                    |
| `Merge`    | Branch merge commits                     |
