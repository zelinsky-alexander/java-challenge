import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) throws Exception {
        AtomicInteger active = new AtomicInteger();
        AtomicInteger peak = new AtomicInteger();
        List<String> ids = java.util.stream.IntStream.range(0, 30).mapToObj(Integer::toString).toList();
        List<String> result = Main.fetchAll(ids, 3, id -> {
            int now = active.incrementAndGet();
            peak.accumulateAndGet(now, Math::max);
            try {
                Thread.sleep(12);
                return "value:" + id;
            } finally {
                active.decrementAndGet();
            }
        });
        assert result.size() == ids.size();
        for (int i = 0; i < ids.size(); i++) assert result.get(i).equals("value:" + ids.get(i));
        assert peak.get() <= 3 && peak.get() >= 2 : peak;
        IOException error = new IOException("service failed");
        try {
            Main.fetchAll(List.of("a", "b"), 1, id -> { throw error; });
            throw new AssertionError("failure was swallowed");
        } catch (IOException got) {
            assert got == error;
        }
    }
}
