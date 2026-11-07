import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Main {
    record Dashboard(String user, String orders, String recommendations) {}

    private static final class Stage {
        final CompletableFuture<String> result = new CompletableFuture<>();
        final FutureTask<String> running;

        Stage(Callable<String> call, Executor executor) {
            running = new FutureTask<>(call);
            executor.execute(() -> {
                running.run();
                try {
                    result.complete(running.get());
                } catch (ExecutionException failure) {
                    result.completeExceptionally(failure.getCause());
                } catch (InterruptedException failure) {
                    Thread.currentThread().interrupt();
                    result.completeExceptionally(failure);
                } catch (CancellationException failure) {
                    result.cancel(false);
                }
            });
        }

        void cancel() {
            running.cancel(true); // Unlike CompletableFuture.cancel, interrupts a running Callable.
            result.cancel(true);
        }
    }

    static Dashboard loadDashboard(
            Duration deadline,
            Executor executor,
            Callable<String> userCall,
            Callable<String> ordersCall,
            Callable<String> recommendationsCall) throws Exception {
        Objects.requireNonNull(deadline);
        Objects.requireNonNull(executor);
        if (deadline.isNegative() || deadline.isZero()) throw new IllegalArgumentException("deadline must be positive");
        long budget = deadline.toNanos();
        long started = System.nanoTime();
        Stage[] stages = new Stage[3];
        boolean success = false;
        try {
            stages[0] = new Stage(userCall, executor);
            stages[1] = new Stage(ordersCall, executor);
            stages[2] = new Stage(recommendationsCall, executor);
            CompletableFuture<Dashboard> combined = new CompletableFuture<>();
            CompletableFuture.allOf(stages[0].result, stages[1].result, stages[2].result)
                    .thenApply(ignored -> new Dashboard(
                            stages[0].result.join(), stages[1].result.join(), stages[2].result.join()))
                    .whenComplete((value, error) -> {
                        if (error == null) combined.complete(value);
                        else combined.completeExceptionally(error);
                    });
            // A failed call ends aggregation promptly without waiting for slow peers.
            for (Stage stage : stages) {
                stage.result.whenComplete((value, failure) -> {
                    if (failure != null) combined.completeExceptionally(failure);
                });
            }
            long remaining = budget - (System.nanoTime() - started);
            if (remaining <= 0) throw new TimeoutException("dashboard deadline expired");
            try {
                Dashboard dashboard = combined.get(remaining, TimeUnit.NANOSECONDS);
                success = true;
                return dashboard;
            } catch (ExecutionException failure) {
                Throwable original = failure.getCause();
                while (original instanceof java.util.concurrent.CompletionException && original.getCause() != null) {
                    original = original.getCause();
                }
                if (original instanceof Exception e) throw e;
                if (original instanceof Error e) throw e;
                throw new AssertionError(original);
            }
        } finally {
            if (!success) {
                for (Stage stage : stages) if (stage != null) stage.cancel();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(3)) {
            System.out.println(loadDashboard(Duration.ofSeconds(2), executor,
                    () -> "Ada", () -> "4 orders", () -> "books"));
        }
    }
}
