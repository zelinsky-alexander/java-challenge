# 010 — Deadlock-free account transfer

## Question

Implement `transfer` for two in-memory accounts accessed concurrently by many threads.

Requirements:

- debit and credit must be atomic as one logical operation
- reject insufficient funds
- preserve total balance
- avoid deadlock when two threads transfer in opposite directions
- do not use one global lock for every account
- transferring from an account to itself must be safe

Explain the lock-ordering rule you chose.

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
