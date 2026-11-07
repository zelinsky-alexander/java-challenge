public class Test {
    public static void main(String[] args) throws Exception {
        Main.Account a = new Main.Account(1, 1000);
        Main.Account b = new Main.Account(1, 1000); // Duplicate business ID must not break ordering.
        Runnable forward = () -> { for (int i = 0; i < 2000; i++) Main.transfer(a, b, 1); };
        Runnable reverse = () -> { for (int i = 0; i < 2000; i++) Main.transfer(b, a, 1); };
        // Each direction alone can temporarily run out of funds; use larger initial balances.
        a.balance = 10000;
        b.balance = 10000;
        Thread first = new Thread(forward);
        Thread second = new Thread(reverse);
        first.start(); second.start();
        first.join(3000); second.join(3000);
        assert !first.isAlive() && !second.isAlive() : "possible deadlock";
        assert a.balance + b.balance == 20000;
        Main.transfer(a, a, 500);
        Main.Account rich = new Main.Account(3, Long.MAX_VALUE);
        Main.Account payer = new Main.Account(4, 10);
        try {
            Main.transfer(payer, rich, 1);
            throw new AssertionError("overflow not rejected");
        } catch (ArithmeticException expected) { }
        assert rich.balance == Long.MAX_VALUE && payer.balance == 10;
        try {
            Main.transfer(payer, a, 11);
            throw new AssertionError("insufficient funds not rejected");
        } catch (IllegalStateException expected) { }
    }
}
