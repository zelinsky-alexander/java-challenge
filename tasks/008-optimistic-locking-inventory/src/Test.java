public class Test {
    static class Repository implements Main.InventoryRepository {
        Main.Item item = new Main.Item("sku", 5, 1);
        @Override public synchronized Main.Item get(String sku) { return item; }
        @Override public synchronized boolean updateIfVersionMatches(String sku, long version, int count) {
            if (item.version() != version) return false;
            item = new Main.Item(sku, count, version + 1);
            return true;
        }
    }
    public static void main(String[] args) {
        Repository repository = new Repository();
        assert Main.reserve(repository, "sku", 3, 2);
        assert !Main.reserve(repository, "sku", 3, 2);
        assert repository.item.available() == 2;
        Main.InventoryRepository conflicts = new Main.InventoryRepository() {
            public Main.Item get(String sku) { return new Main.Item(sku, 10, 1); }
            public boolean updateIfVersionMatches(String sku, long version, int available) { return false; }
        };
        try {
            Main.reserve(conflicts, "sku", 1, 2);
            throw new AssertionError("expected contention error");
        } catch (IllegalStateException expected) { }
    }
}
