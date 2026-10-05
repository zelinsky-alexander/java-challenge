# 004 — TTL cache without stampede

## Question

Implement a thread-safe in-memory `TtlCache<K,V>`.

Requirements:

- return a cached value before expiry
- after expiry, exactly one caller loads a value for a key
- concurrent callers for that key wait for/reuse the same load
- loaders for different keys run independently
- failed loads must not poison the key forever
- do not hold a global lock while executing the loader

Discuss eviction and memory-bounding for production use.

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
