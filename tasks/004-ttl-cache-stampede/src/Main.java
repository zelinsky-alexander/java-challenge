import java.time.Duration;
import java.util.concurrent.Callable;

public class Main {
    static final class TtlCache<K, V> {
        TtlCache(Duration ttl) {
            // TODO
        }

        V get(K key, Callable<V> loader) throws Exception {
            // TODO
            throw new UnsupportedOperationException("TODO");
        }
    }

    public static void main(String[] args) {
        System.out.println("Task 004: implement TtlCache");
    }
}
