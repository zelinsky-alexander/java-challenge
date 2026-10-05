# 002 — Bounded executor with backpressure

## Question

Implement `BoundedExecutor` for backend work submission.

Requirements:

- fixed number of worker threads
- bounded queue
- when saturated, apply backpressure instead of silently dropping work
- reject submissions after shutdown
- implement graceful shutdown
- no unbounded queues

Be ready to explain how queue capacity affects latency and memory under overload.

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
