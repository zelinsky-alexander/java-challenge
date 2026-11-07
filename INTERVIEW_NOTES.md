# Java challenge — interview notes

These are the principal ideas to explain at the whiteboard. Each task's `Main.java` is an original implementation built on the repository's existing exercise scaffold; `Test.java` contains new JDK-only tests.

| Task | Core idea | Time / space | Important caveat |
| --- | --- | --- | --- |
| 001 Virtual threads | One virtual task per ID, `Semaphore` bounds access to scarce dependency, `Future.get` in submission order. | O(n) bookkeeping, O(n) tasks/results | Virtual threads are cheap, not a substitute for resource connection limits. |
| 002 Backpressure | Lock + two conditions: producers block at queue capacity, fixed workers drain; shutdown wakes both. | O(1) enqueue/dequeue, O(capacity) queue | Blocking producers can exhaust HTTP request threads; consider rejection/timeouts. |
| 003 Idempotency | Per-key `ConcurrentHashMap.compute` installs one promise; other callers await same result or exception. | Expected O(1) per key lookup, O(keys) storage | TTL is local and lazy; across pods, use durable storage and atomic claim/lease. |
| 004 TTL cache | Same per-key single-flight pattern; remove failed entries to allow retry. | Expected O(1) lookup, O(keys + values) | Need eviction/memory limits, loader timeouts, and metrics in production. |
| 005 Rate limiter | Refill fractional tokens from `System.nanoTime()` on each synchronized request; cap burst. | O(1) time and space | Per-instance limits are not global; cluster-wide policies need shared state. |
| 006 Deadline | Start all three calls, aggregate with `CompletableFuture`, wait only remaining overall budget, interrupt unfinished `FutureTask`s. | O(1) tasks for fixed 3 calls | Cancellation is best effort: downstream I/O must honor interrupts or its own timeout. |
| 007 Outbox | Write order and uniquely identified event in one DB transaction; publisher runs independently. | O(1) local operations | Publisher is at-least-once: mark sent only after acknowledgement; consumers dedupe event IDs. |
| 008 Optimistic lock | Read (stock, version), CAS update, reread on conflict up to `maxRetries`. | O(retries) time, O(1) space | SQL: `UPDATE ... SET stock=stock-?, version=version+1 WHERE sku=? AND version=? AND stock>=?`. |
| 009 TCP framing | Keep 4-byte header and incomplete payload between reads; validate length before allocation. | O(bytes) time, O(maxFrameSize) state | TCP is a byte stream: a read need not contain one whole message. |
| 010 Transfer | Acquire two account locks in an immutable unique order, update both while held. | O(1) time/space | All readers and writers must follow the same locking discipline. |

## Design constraints

- Tasks 003 and 004 intentionally do not implement background eviction. Large numbers of unique keys can therefore consume unbounded memory until a production eviction policy is added.
- Task 007 models atomic transaction scope; it does not implement a database engine, broker, or publisher.
- Task 008's repository must implement the version match atomically in persistent storage.
- No external libraries are used or bundled. The project licence should be reviewed by the repository owner before merging.
