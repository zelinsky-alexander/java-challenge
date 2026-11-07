import java.util.ArrayDeque;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static final class BoundedExecutor implements AutoCloseable {
        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition();
        private final Condition notFull = lock.newCondition();
        private final ArrayDeque<Runnable> queue = new ArrayDeque<>();
        private final int queueCapacity;
        private final CountDownLatch terminated;
        private boolean shutdown;

        BoundedExecutor(int workers, int queueCapacity) {
            if (workers <= 0 || queueCapacity <= 0) {
                throw new IllegalArgumentException("workers and queueCapacity must be positive");
            }
            this.queueCapacity = queueCapacity;
            this.terminated = new CountDownLatch(workers);
            for (int i = 0; i < workers; i++) {
                new Thread(this::runWorker, "bounded-worker-" + i).start();
            }
        }

        void submit(Runnable task) throws InterruptedException {
            Objects.requireNonNull(task);
            lock.lockInterruptibly();
            try {
                while (queue.size() == queueCapacity && !shutdown) {
                    notFull.await(); // Producer waits for capacity: no silent task loss.
                }
                if (shutdown) throw new RejectedExecutionException("executor is shut down");
                queue.addLast(task);
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        private void runWorker() {
            try {
                while (true) {
                    Runnable task;
                    lock.lock();
                    try {
                        while (queue.isEmpty() && !shutdown) {
                            try {
                                notEmpty.await();
                            } catch (InterruptedException ignored) {
                                // No public worker interruption API; continue draining work.
                            }
                        }
                        if (queue.isEmpty()) return;
                        task = queue.removeFirst();
                        notFull.signal();
                    } finally {
                        lock.unlock();
                    }
                    try {
                        task.run();
                    } catch (Throwable failure) {
                        System.err.println("Worker task failed: " + failure);
                    }
                }
            } finally {
                terminated.countDown();
            }
        }

        void shutdown() {
            lock.lock();
            try {
                shutdown = true;
                notEmpty.signalAll();
                notFull.signalAll(); // Release blocked submitters to reject them.
            } finally {
                lock.unlock();
            }
        }

        boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return terminated.await(timeout, unit);
        }

        @Override
        public void close() {
            shutdown();
        }
    }

    public static void main(String[] args) throws Exception {
        try (BoundedExecutor executor = new BoundedExecutor(2, 2)) {
            for (int i = 0; i < 5; i++) {
                int taskId = i;
                executor.submit(() -> System.out.println("task " + taskId));
            }
            executor.shutdown();
            System.out.println("finished: " + executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }
}
