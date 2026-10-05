# Java Challenge

Ten short senior-level Java backend interview tasks.

## Requirements

- Linux
- JDK 25 or newer
- No third-party dependencies

Check Java:

```bash
java --version
javac --version
```

Each task is self-contained under `tasks/` and includes:

- a short interview-style question
- a small compilable scaffold
- Linux build/run commands

No solutions are included.

## Tasks

1. `001-virtual-thread-bulkhead` — bound a virtual-thread workload around a scarce dependency
2. `002-bounded-executor-backpressure` — implement bounded submission/backpressure
3. `003-idempotency-store` — make duplicate request handling concurrency-safe
4. `004-ttl-cache-stampede` — build a TTL cache that coalesces concurrent misses
5. `005-token-bucket-rate-limiter` — implement a thread-safe token bucket
6. `006-completable-future-deadline` — aggregate parallel calls under one deadline
7. `007-transactional-outbox` — model atomic business state + outbox publishing
8. `008-optimistic-locking-inventory` — prevent lost updates with version checks
9. `009-tcp-length-prefixed-decoder` — decode fragmented TCP frames safely
10. `010-deadlock-free-transfer` — transfer between accounts without deadlock

These tasks intentionally use JDK APIs only so the interview focus stays on Java/backend reasoning rather than framework setup.
