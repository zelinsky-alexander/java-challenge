import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private static final AtomicLong NEXT_LOCK_ORDER = new AtomicLong();

    static final class Account {
        final long id;
        final ReentrantLock lock = new ReentrantLock();
        final long lockOrder = NEXT_LOCK_ORDER.getAndIncrement();
        long balance;

        Account(long id, long balance) {
            if (balance < 0) throw new IllegalArgumentException("balance must not be negative");
            this.id = id;
            this.balance = balance;
        }
    }

    static void transfer(Account from, Account to, long amount) {
        Objects.requireNonNull(from);
        Objects.requireNonNull(to);
        if (amount < 0) throw new IllegalArgumentException("amount must not be negative");
        if (from == to || amount == 0) return;

        // Unique per-instance order works even when two accounts have the same business ID.
        Account first = from.lockOrder < to.lockOrder ? from : to;
        Account second = first == from ? to : from;
        first.lock.lock();
        try {
            second.lock.lock();
            try {
                if (from.balance < amount) throw new IllegalStateException("Insufficient funds");
                long newDestinationBalance = Math.addExact(to.balance, amount);
                from.balance -= amount;
                to.balance = newDestinationBalance;
            } finally {
                second.lock.unlock();
            }
        } finally {
            first.lock.unlock();
        }
    }

    public static void main(String[] args) {
        Account a = new Account(1, 100);
        Account b = new Account(2, 50);
        transfer(a, b, 20);
        System.out.println("balances: " + a.balance + ", " + b.balance);
    }
}
