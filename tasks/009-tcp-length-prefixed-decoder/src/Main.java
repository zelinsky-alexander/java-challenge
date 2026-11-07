import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Main {
    static final class FrameDecoder {
        private final int maxFrameSize;
        private final ByteBuffer header = ByteBuffer.allocate(Integer.BYTES);
        private ByteBuffer payload;

        FrameDecoder(int maxFrameSize) {
            if (maxFrameSize < 0) throw new IllegalArgumentException("maxFrameSize must not be negative");
            this.maxFrameSize = maxFrameSize;
        }

        // One decoder belongs to one TCP connection; external synchronization if shared.
        List<byte[]> feed(ByteBuffer bytes) {
            Objects.requireNonNull(bytes);
            List<byte[]> frames = new ArrayList<>();
            while (bytes.hasRemaining()) {
                if (payload == null) {
                    transfer(bytes, header);
                    if (header.hasRemaining()) break;
                    header.flip(); // Big-endian is ByteBuffer's default byte order.
                    int length = header.getInt();
                    header.clear();
                    if (length < 0 || length > maxFrameSize) {
                        throw new IllegalArgumentException("Invalid frame length: " + length);
                    }
                    if (length == 0) {
                        frames.add(new byte[0]);
                        continue;
                    }
                    payload = ByteBuffer.allocate(length);
                }
                transfer(bytes, payload);
                if (!payload.hasRemaining()) {
                    frames.add(payload.array());
                    payload = null;
                }
            }
            return frames;
        }

        private static void transfer(ByteBuffer source, ByteBuffer destination) {
            while (source.hasRemaining() && destination.hasRemaining()) {
                destination.put(source.get());
            }
        }
    }

    public static void main(String[] args) {
        FrameDecoder decoder = new FrameDecoder(1024);
        System.out.println(decoder.feed(ByteBuffer.wrap(new byte[]{0, 0, 0, 2, 'O', 'K'})).size());
    }
}
