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
            this.database = database;
        }

        UUID placeOrder(String customerId) throws Exception {
            // TODO
            throw new UnsupportedOperationException("TODO");
        }
    }

    public static void main(String[] args) {
        System.out.println("Task 007: implement OrderService.placeOrder()");
    }
}
