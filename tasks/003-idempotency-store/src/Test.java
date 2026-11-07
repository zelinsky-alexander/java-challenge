import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) throws Exception {
        Main.IdempotencyStore store = new Main.IdempotencyStore(Duration.ofMillis(120));
        AtomicInteger called = new AtomicInteger();
        try (var executor = Executors.newFixedThreadPool(12)) {
            List<Future<String>> work = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                work.add(executor.submit(() -> store.executeOnce("one", () -> {
                    called.incrementAndGet();
                    Thread.sleep(40);
                    return "ok";
                })));
            }
            for (Future<String> task : work) assert task.get().equals("ok");
        }
        assert called.get() == 1 : called;
        IOException error = new IOException("same error");
        for (int i = 0; i < 2; i++) {
            try {
                store.executeOnce("failure", () -> { throw error; });
                throw new AssertionError("expected failure");
            } catch (IOException got) { assert got == error; }
        }
        Thread.sleep(150);
        assert store.executeOnce("one", () -> "new").equals("new");
    }
}
