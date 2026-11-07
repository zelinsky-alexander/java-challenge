import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;

public class Test {
    public static void main(String[] args) throws Exception {
        try (var pool = Executors.newFixedThreadPool(3)) {
            var dashboard = Main.loadDashboard(Duration.ofSeconds(1), pool,
                    () -> "user", () -> "orders", () -> "recommendations");
            assert dashboard.equals(new Main.Dashboard("user", "orders", "recommendations"));
            long started = System.nanoTime();
            try {
                Main.loadDashboard(Duration.ofMillis(100), pool,
                        () -> { Thread.sleep(1000); return "slow"; },
                        () -> { Thread.sleep(1000); return "slow"; },
                        () -> { Thread.sleep(1000); return "slow"; });
                throw new AssertionError("timeout not enforced");
            } catch (TimeoutException expected) { }
            assert (System.nanoTime() - started) / 1e6 < 600;
            IOException error = new IOException("backend failed");
            started = System.nanoTime();
            try {
                Main.loadDashboard(Duration.ofSeconds(2), pool,
                        () -> { throw error; },
                        () -> { Thread.sleep(1000); return "slow"; },
                        () -> "fast");
                throw new AssertionError("failure was swallowed");
            } catch (IOException got) { assert got == error; }
            assert (System.nanoTime() - started) / 1e6 < 600;
        }
    }
}
