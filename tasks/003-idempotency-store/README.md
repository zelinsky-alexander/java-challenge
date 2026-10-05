# 003 — Concurrent idempotency store

## Question

Implement `executeOnce` for an HTTP-style idempotency key.

If many threads concurrently call it with the same key:

- the action must execute at most once
- every caller must receive the same result or same failure
- different keys should proceed independently
- completed entries should expire after a configurable TTL
- avoid a single global lock

Discuss what must change when the service runs on multiple JVM instances.

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
