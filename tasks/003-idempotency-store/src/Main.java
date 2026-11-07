import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class Main {
    static final class IdempotencyStore {
        private static final class Entry {
            final CompletableFuture<Object> result = new CompletableFuture<>();
            volatile long completedAt;
            volatile boolean finished;

            boolean expired(long now, long ttlNanos) {
                return finished && now - completedAt >= ttlNanos;
            }
        }

        private final ConcurrentHashMap<String, Entry> entries = new ConcurrentHashMap<>();
        private final long ttlNanos;

        IdempotencyStore(Duration ttl) {
            Objects.requireNonNull(ttl);
            if (ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException("TTL must be positive");
            ttlNanos = ttl.toNanos();
        }

        <T> T executeOnce(String key, Callable<T> action) throws Exception {
            Objects.requireNonNull(key);
            Objects.requireNonNull(action);
            Entry[] created = new Entry[1];
            Entry entry = entries.compute(key, (ignored, existing) -> {
                if (existing != null && !existing.expired(System.nanoTime(), ttlNanos)) return existing;
                return created[0] = new Entry();
            });

            if (created[0] == entry) {
                try {
                    T value = action.call();
                    entry.completedAt = System.nanoTime();
                    entry.finished = true;
                    entry.result.complete(value);
                } catch (Throwable failure) {
                    entry.completedAt = System.nanoTime();
                    entry.finished = true;
                    entry.result.completeExceptionally(failure);
                }
            }
            try {
                @SuppressWarnings("unchecked")
                T value = (T) entry.result.get();
                return value;
            } catch (ExecutionException failed) {
                Throwable cause = failed.getCause();
                if (cause instanceof Exception e) throw e;
                if (cause instanceof Error e) throw e;
                throw new AssertionError(cause);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        IdempotencyStore store = new IdempotencyStore(Duration.ofSeconds(30));
        System.out.println(store.executeOnce("request-1", () -> "created"));
        System.out.println(store.executeOnce("request-1", () -> "not executed"));
    }
}
