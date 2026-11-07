import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class Main {
    static final class TtlCache<K, V> {
        private static final class Entry<V> {
            final CompletableFuture<V> result = new CompletableFuture<>();
            volatile long loadedAt;
            volatile boolean finished;

            boolean expired(long now, long ttlNanos) {
                return finished && now - loadedAt >= ttlNanos;
            }
        }

        private final ConcurrentHashMap<K, Entry<V>> entries = new ConcurrentHashMap<>();
        private final long ttlNanos;

        TtlCache(Duration ttl) {
            Objects.requireNonNull(ttl);
            if (ttl.isNegative() || ttl.isZero()) throw new IllegalArgumentException("TTL must be positive");
            ttlNanos = ttl.toNanos();
        }

        V get(K key, Callable<V> loader) throws Exception {
            Objects.requireNonNull(key);
            Objects.requireNonNull(loader);
            @SuppressWarnings("unchecked")
            Entry<V>[] created = (Entry<V>[]) new Entry<?>[1];
            Entry<V> entry = entries.compute(key, (ignored, current) -> {
                if (current != null && !current.expired(System.nanoTime(), ttlNanos)) return current;
                return created[0] = new Entry<>();
            });

            if (created[0] == entry) {
                try {
                    V value = loader.call();
                    entry.loadedAt = System.nanoTime();
                    entry.finished = true;
                    entry.result.complete(value);
                } catch (Throwable failure) {
                    entry.result.completeExceptionally(failure);
                    entries.remove(key, entry); // A failed load is eligible for retry.
                }
            }
            try {
                return entry.result.get();
            } catch (ExecutionException failed) {
                Throwable cause = failed.getCause();
                if (cause instanceof Exception e) throw e;
                if (cause instanceof Error e) throw e;
                throw new AssertionError(cause);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        TtlCache<String, String> cache = new TtlCache<>(Duration.ofSeconds(10));
        System.out.println(cache.get("user:1", () -> "loaded"));
        System.out.println(cache.get("user:1", () -> "not loaded again"));
    }
}
