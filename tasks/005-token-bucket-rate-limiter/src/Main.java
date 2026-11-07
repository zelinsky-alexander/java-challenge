public class Main {
    static final class TokenBucket {
        private final long capacity;
        private final double tokensPerSecond;
        private double available;
        private long updatedAt;

        TokenBucket(long capacity, double tokensPerSecond) {
            if (capacity <= 0 || !Double.isFinite(tokensPerSecond) || tokensPerSecond <= 0) {
                throw new IllegalArgumentException("capacity and refill rate must be positive and finite");
            }
            this.capacity = capacity;
            this.tokensPerSecond = tokensPerSecond;
            this.available = capacity;
            this.updatedAt = System.nanoTime();
        }

        synchronized boolean tryAcquire() {
            long now = System.nanoTime();
            long elapsed = now - updatedAt;
            if (elapsed > 0) {
                available = Math.min((double) capacity, available + elapsed / 1_000_000_000.0 * tokensPerSecond);
                updatedAt = now;
            }
            if (available < 1.0) return false;
            available -= 1.0;
            return true;
        }
    }

    public static void main(String[] args) {
        TokenBucket limiter = new TokenBucket(2, 1.0);
        System.out.println(limiter.tryAcquire());
        System.out.println(limiter.tryAcquire());
        System.out.println(limiter.tryAcquire());
    }
}
