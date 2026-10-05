import java.util.concurrent.TimeUnit;

public class Main {
    static final class BoundedExecutor implements AutoCloseable {
        BoundedExecutor(int workers, int queueCapacity) {
            // TODO
        }

        void submit(Runnable task) throws InterruptedException {
            // TODO
            throw new UnsupportedOperationException("TODO");
        }

        void shutdown() {
            // TODO
        }

        boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            // TODO
            throw new UnsupportedOperationException("TODO");
        }

        @Override
        public void close() {
            shutdown();
        }
    }

    public static void main(String[] args) {
        System.out.println("Task 002: implement BoundedExecutor");
    }
}
