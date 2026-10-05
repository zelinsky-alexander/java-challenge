# 005 — Token-bucket rate limiter

## Question

Implement a thread-safe token-bucket limiter.

Constructor parameters:

- `capacity`: maximum burst size
- `tokensPerSecond`: refill rate

`tryAcquire()` must:

- return immediately
- allow bursts up to capacity
- refill according to elapsed monotonic time
- never exceed capacity
- remain correct under concurrent callers

Do not create a background refill thread.

## Build and run on Linux

From this task directory:

```bash
rm -rf out
mkdir -p out
javac -d out src/Main.java
java -cp out Main
```
