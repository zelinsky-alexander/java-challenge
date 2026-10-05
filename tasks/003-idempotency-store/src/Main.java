import java.time.Duration;
import java.util.concurrent.Callable;

public class Main {
    static final class IdempotencyStore {
        IdempotencyStore(Duration ttl) {
            // TODO
        }

        <T> T executeOnce(String key, Callable<T> action) throws Exception {
            // TODO
            throw new UnsupportedOperationException("TODO");
        }
    }

    public static void main(String[] args) {
        System.out.println("Task 003: implement IdempotencyStore");
    }
}
