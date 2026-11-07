import java.util.Objects;
import java.util.UUID;

public class Main {
    record Order(UUID id, String customerId) {}
    record OutboxEvent(UUID id, UUID aggregateId, String type, String payload) {}

    interface Transaction {
        void insertOrder(Order order);
        void insertOutbox(OutboxEvent event);
    }

    interface Database {
        <T> T inTransaction(java.util.concurrent.Callable<T> work) throws Exception;
        Transaction currentTransaction();
    }

    static final class OrderService {
        private final Database database;

        OrderService(Database database) {
            this.database = Objects.requireNonNull(database);
        }

        UUID placeOrder(String customerId) throws Exception {
            Objects.requireNonNull(customerId);
            if (customerId.isBlank()) throw new IllegalArgumentException("customerId must not be blank");
            return database.inTransaction(() -> {
                UUID orderId = UUID.randomUUID();
                Transaction tx = database.currentTransaction();
                tx.insertOrder(new Order(orderId, customerId));
                // Event ID is stable in the committed outbox row for publisher deduplication.
                tx.insertOutbox(new OutboxEvent(UUID.randomUUID(), orderId,
                        "OrderCreated", customerId));
                return orderId;
            });
        }
    }

    public static void main(String[] args) {
        System.out.println("Provide a transactional Database implementation to call OrderService.placeOrder().");
    }
}
