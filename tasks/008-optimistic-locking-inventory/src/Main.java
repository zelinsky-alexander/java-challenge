import java.util.NoSuchElementException;
import java.util.Objects;

public class Main {
    record Item(String sku, int available, long version) {}

    interface InventoryRepository {
        Item get(String sku);
        boolean updateIfVersionMatches(String sku, long expectedVersion, int newAvailable);
    }

    static boolean reserve(
            InventoryRepository repository,
            String sku,
            int quantity,
            int maxRetries) {
        Objects.requireNonNull(repository);
        Objects.requireNonNull(sku);
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        if (maxRetries < 0) throw new IllegalArgumentException("maxRetries must not be negative");

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            Item current = repository.get(sku);
            if (current == null) throw new NoSuchElementException("Unknown SKU: " + sku);
            if (current.available() < quantity) return false;
            if (repository.updateIfVersionMatches(sku, current.version(), current.available() - quantity)) {
                return true;
            }
        }
        throw new IllegalStateException("Too much contention while reserving " + sku);
    }

    public static void main(String[] args) {
        System.out.println("Use reserve(repository, sku, quantity, maxRetries) with a CAS repository.");
    }
}
