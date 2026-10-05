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
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public static void main(String[] args) {
        System.out.println("Task 008: implement reserve()");
    }
}
