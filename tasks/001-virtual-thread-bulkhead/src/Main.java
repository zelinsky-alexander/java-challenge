import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

public class Main {
    @FunctionalInterface
    interface Fetcher {
        String fetch(String id) throws Exception;
    }

    static List<String> fetchAll(List<String> ids, int maxConcurrent) throws Exception {
        return fetchAll(ids, maxConcurrent, id -> "data:" + id);
    }

    // Overload permits testing the concurrency limit with a real or simulated dependency.
    static List<String> fetchAll(List<String> ids, int maxConcurrent, Fetcher dependency) throws Exception {
        Objects.requireNonNull(ids);
        Objects.requireNonNull(dependency);
        if (maxConcurrent <= 0) throw new IllegalArgumentException("maxConcurrent must be positive");

        Semaphore permits = new Semaphore(maxConcurrent);
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<String>> results = new ArrayList<>(ids.size());
            for (String id : ids) {
                results.add(executor.submit(() -> {
                    permits.acquire();
                    try {
                        return dependency.fetch(id);
                    } finally {
                        permits.release();
                    }
                }));
            }

            List<String> ordered = new ArrayList<>(ids.size());
            try {
                for (Future<String> result : results) {
                    try {
                        ordered.add(result.get());
                    } catch (ExecutionException failed) {
                        Throwable cause = failed.getCause();
                        if (cause instanceof Exception e) throw e;
                        if (cause instanceof Error e) throw e;
                        throw new AssertionError(cause);
                    }
                }
            } catch (Exception | Error failed) {
                for (Future<?> result : results) result.cancel(true);
                throw failed;
            }
            return ordered;
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println(fetchAll(List.of("A", "B", "C"), 2));
    }
}
