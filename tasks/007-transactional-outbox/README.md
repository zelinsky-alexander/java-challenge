# 007 — Transactional outbox

## Question

Complete `OrderService.placeOrder`.

The provided transaction abstraction represents one database transaction.

Requirements:

- insert the order and its outbox event in the same transaction
- never publish to the broker directly inside `placeOrder`
- make the outbox event identifiable for idempotent publishing
- keep business state and event creation consistent if an exception occurs

Then explain how a separate publisher would safely deliver and mark outbox rows.

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
