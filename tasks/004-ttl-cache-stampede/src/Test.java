import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) throws Exception {
        Main.TtlCache<String, Integer> cache = new Main.TtlCache<>(Duration.ofMillis(120));
        AtomicInteger loads = new AtomicInteger();
        try (var pool = Executors.newFixedThreadPool(10)) {
            List<Future<Integer>> work = new ArrayList<>();
            for (int i = 0; i < 10; i++) work.add(pool.submit(() -> cache.get("same", () -> {
                loads.incrementAndGet(); Thread.sleep(30); return 42;
            })));
            for (Future<Integer> task : work) assert task.get() == 42;
        }
        assert loads.get() == 1 : loads;
        assert cache.get("same", () -> 99) == 42;
        Thread.sleep(150);
        assert cache.get("same", () -> 43) == 43;
        try {
            cache.get("bad", () -> { throw new IOException("loader failed"); });
            throw new AssertionError("expected load error");
        } catch (IOException expected) { }
        assert cache.get("bad", () -> 7) == 7;
    }
}
