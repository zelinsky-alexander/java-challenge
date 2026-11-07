import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public class Test {
    static final class MemoryDatabase implements Main.Database {
        final List<Main.Order> orders = new ArrayList<>();
        final List<Main.OutboxEvent> events = new ArrayList<>();
        boolean failOnOutbox;
        Main.Transaction transaction;

        @Override
        public <T> T inTransaction(Callable<T> work) throws Exception {
            List<Main.Order> stagedOrders = new ArrayList<>();
            List<Main.OutboxEvent> stagedEvents = new ArrayList<>();
            transaction = new Main.Transaction() {
                public void insertOrder(Main.Order order) { stagedOrders.add(order); }
                public void insertOutbox(Main.OutboxEvent event) {
                    if (failOnOutbox) throw new IllegalStateException("outbox insert failed");
                    stagedEvents.add(event);
                }
            };
            try {
                T result = work.call();
                orders.addAll(stagedOrders);
                events.addAll(stagedEvents);
                return result;
            } finally { transaction = null; }
        }
        public Main.Transaction currentTransaction() { return transaction; }
    }

    public static void main(String[] args) throws Exception {
        MemoryDatabase database = new MemoryDatabase();
        Main.OrderService service = new Main.OrderService(database);
        UUID orderId = service.placeOrder("customer-1");
        assert database.orders.size() == 1 && database.events.size() == 1;
        assert database.orders.get(0).id().equals(orderId);
        assert database.events.get(0).aggregateId().equals(orderId);
        assert database.events.get(0).id() != null;
        database.failOnOutbox = true;
        try {
            service.placeOrder("customer-2");
            throw new AssertionError("transaction should fail");
        } catch (IllegalStateException expected) { }
        assert database.orders.size() == 1 && database.events.size() == 1 : "partial commit";
    }
}
