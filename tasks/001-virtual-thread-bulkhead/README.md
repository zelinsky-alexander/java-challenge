# 001 — Virtual-thread bulkhead

## Question

Implement `fetchAll` so each input is processed concurrently using **virtual threads**, but no more than `maxConcurrent` calls may enter the simulated scarce dependency at once.

Requirements:

- preserve result order
- propagate failures
- do not busy-wait
- always release permits/resources
- explain why virtual threads alone do not protect a database or remote service

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
