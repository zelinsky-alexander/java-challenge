import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class Test {
    static byte[] frame(String text) {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        return ByteBuffer.allocate(4 + data.length).putInt(data.length).put(data).array();
    }
    public static void main(String[] args) {
        Main.FrameDecoder decoder = new Main.FrameDecoder(10);
        byte[] first = frame("abc");
        assert decoder.feed(ByteBuffer.wrap(first, 0, 2)).isEmpty();
        assert decoder.feed(ByteBuffer.wrap(first, 2, 3)).isEmpty();
        List<byte[]> frames = decoder.feed(ByteBuffer.wrap(first, 5, first.length - 5));
        assert frames.size() == 1 && new String(frames.get(0), StandardCharsets.UTF_8).equals("abc");
        byte[] second = frame("z");
        byte[] together = ByteBuffer.allocate(second.length + 4).put(second).putInt(0).array();
        frames = decoder.feed(ByteBuffer.wrap(together));
        assert frames.size() == 2 && frames.get(1).length == 0;
        try {
            new Main.FrameDecoder(3).feed(ByteBuffer.allocate(4).putInt(4).flip());
            throw new AssertionError("oversized frame allowed");
        } catch (IllegalArgumentException expected) { }
        try {
            new Main.FrameDecoder(3).feed(ByteBuffer.allocate(4).putInt(-1).flip());
            throw new AssertionError("negative length allowed");
        } catch (IllegalArgumentException expected) { }
    }
}
