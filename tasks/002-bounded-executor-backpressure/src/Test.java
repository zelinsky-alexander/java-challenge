import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) throws Exception {
        Main.BoundedExecutor executor = new Main.BoundedExecutor(1, 1);
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch allow = new CountDownLatch(1);
        AtomicInteger count = new AtomicInteger();
        executor.submit(() -> {
            entered.countDown();
            try { allow.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            count.incrementAndGet();
        });
        assert entered.await(1, TimeUnit.SECONDS);
        executor.submit(count::incrementAndGet);
        CountDownLatch submitted = new CountDownLatch(1);
        Thread producer = new Thread(() -> {
            try { executor.submit(count::incrementAndGet); submitted.countDown(); }
            catch (InterruptedException e) { throw new AssertionError(e); }
        });
        producer.start();
        assert !submitted.await(40, TimeUnit.MILLISECONDS) : "missing backpressure";
        allow.countDown();
        assert submitted.await(2, TimeUnit.SECONDS);
        executor.shutdown();
        assert executor.awaitTermination(2, TimeUnit.SECONDS);
        assert count.get() == 3 : count;
        try {
            executor.submit(() -> {});
            throw new AssertionError("shutdown executor accepted work");
        } catch (RejectedExecutionException expected) {
            // expected
        }
    }
}
