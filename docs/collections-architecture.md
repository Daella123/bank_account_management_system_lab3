# Collections Architecture — Lab 3

## Overview

This document describes the data structure decisions made in Lab 3 when migrating
from fixed-size arrays (Lab 2) to Java Collections Framework.

---

## Before (Lab 2 — Arrays)

| Component           | Data Structure   | Limitation             |
|---------------------|------------------|------------------------|
| `AccountManager`    | `Account[50]`    | Max 50 accounts        |
| `TransactionManager`| `Transaction[200]`| Max 200 transactions  |
| `findAccount()`     | Linear scan O(n) | Slow on large datasets |

---

## After (Lab 3 — Collections)

| Component           | Data Structure                         | Benefit                          |
|---------------------|----------------------------------------|----------------------------------|
| `AccountManager`    | `ArrayList<Account>` + `HashMap<String, Account>` | Unlimited size, O(1) lookup |
| `TransactionManager`| `ArrayList<Transaction>`               | Unlimited size, streams-friendly |

---

## Why Both ArrayList AND HashMap?

- **ArrayList**: preserves insertion order for display (`viewAllAccounts()` shows accounts
  in the order they were added)
- **HashMap**: O(1) lookup by account number — critical for `findAccount()` called
  on every transaction

---

## Functional Programming with Collections

```java
// Stream: total balance (replaces manual loop)
accounts.stream()
        .mapToDouble(Account::getBalance)
        .sum();

// Stream: filter deposits + sort + print
transactions.stream()
    .filter(t -> t.getType().equals("DEPOSIT"))
    .sorted(Comparator.comparing(Transaction::getAmount).reversed())
    .forEach(System.out::println);

// Stream: highest balance account
accounts.stream()
    .max(Comparator.comparingDouble(Account::getBalance));
```

---

## File Persistence with NIO

```java
// Save — writes each account's toFileString() as one line
Files.write(path, accounts.stream()
        .map(Account::toFileString)
        .collect(Collectors.toList()),
    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

// Load — reads lines, parses each back to Account
Files.lines(path)
    .filter(l -> !l.isBlank())
    .map(FilePersistenceService::parseAccount)
    .collect(Collectors.toList());
```

---

## Concurrency Model

All deposit/withdraw operations in `TransactionManager` are `synchronized` on the
account object:

```java
public Transaction deposit(Account account, double amount) throws InvalidAmountException {
    synchronized (account) {          // Lock on THIS account only
        account.deposit(amount);
        Transaction txn = new Transaction(...);
        addTransaction(txn);
        return txn;
    }
}
```

Transfer locks **both** accounts in a consistent alphabetical order by account number
to prevent **deadlock** while still ensuring atomicity.
