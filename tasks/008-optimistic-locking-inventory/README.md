# 008 — Optimistic locking for inventory

## Question

Implement `reserve` using the repository's compare-and-set style update.

Requirements:

- never let stock go below zero
- detect concurrent version conflicts
- retry conflicts a bounded number of times
- distinguish "out of stock" from "too much contention"
- do not serialize all products behind one global lock

Explain how this maps to SQL using a `version` column.

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
