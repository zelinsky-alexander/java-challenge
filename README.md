# Java Challenge

Ten self-contained senior Java backend interview exercises, each with an implementation and a JDK-only regression test.

## Requirements

- Linux (or another system with Bash)
- JDK 21 or newer (the original scaffold targeted JDK 25)
- No third-party dependencies

## Run all tests

```bash
./test-all.sh
```

Each task also compiles on its own:

```bash
cd tasks/001-virtual-thread-bulkhead
javac -d out src/Main.java src/Test.java
java -ea -cp out Test
java -cp out Main
```

The tests use Java assertions, so **always pass `-ea`**.

## Tasks

1. `001-virtual-thread-bulkhead` — limit simultaneous scarce-dependency calls with a semaphore
2. `002-bounded-executor-backpressure` — bounded queue, blocking submit, graceful shutdown
3. `003-idempotency-store` — per-key execution coalescing and cached outcomes
4. `004-ttl-cache-stampede` — coalesce concurrent cache loads and expire values
5. `005-token-bucket-rate-limiter` — monotonic-time, thread-safe token bucket
6. `006-completable-future-deadline` — parallel calls with one deadline and cancellation
7. `007-transactional-outbox` — atomic order and outbox-row insertion
8. `008-optimistic-locking-inventory` — compare-and-set with bounded retries
9. `009-tcp-length-prefixed-decoder` — incremental framing across arbitrary TCP reads
10. `010-deadlock-free-transfer` — atomic transfers using deterministic lock ordering

For concise explanations and complexity notes, see [INTERVIEW_NOTES.md](INTERVIEW_NOTES.md).

The implementations intentionally use JDK APIs only; the in-memory designs in tasks 003, 004, 007 and 008 are teaching examples rather than full distributed/production services.
