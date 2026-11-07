import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) throws Exception {
        Main.TokenBucket bucket = new Main.TokenBucket(2, 20.0);
        assert bucket.tryAcquire();
        assert bucket.tryAcquire();
        assert !bucket.tryAcquire();
        Thread.sleep(70);
        assert bucket.tryAcquire();
        Main.TokenBucket parallel = new Main.TokenBucket(7, 1e-9);
        AtomicInteger successes = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(12)) {
            List<Future<?>> tasks = new ArrayList<>();
            for (int i = 0; i < 50; i++) tasks.add(executor.submit(() -> {
                if (parallel.tryAcquire()) successes.incrementAndGet();
            }));
            for (Future<?> task : tasks) task.get();
        }
        assert successes.get() == 7 : successes;
    }
}
