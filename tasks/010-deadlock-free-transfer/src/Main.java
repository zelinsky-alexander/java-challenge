import java.util.concurrent.locks.ReentrantLock;

public class Main {
    static final class Account {
        final long id;
        final ReentrantLock lock = new ReentrantLock();
        long balance;

        Account(long id, long balance) {
            this.id = id;
            this.balance = balance;
        }
    }

    static void transfer(Account from, Account to, long amount) {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public static void main(String[] args) {
        System.out.println("Task 010: implement transfer()");
    }
}
