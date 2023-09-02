package net.walksanator.aeiou;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BufWrapper {
    private List<ByteBuffer> buffers;
    BufWrapper() {
        buffers = new ArrayList<>();
    }
    public void append(ByteBuffer buf) {
        buffers.add(buf);
    }
    public ByteBuffer concat() {
        AtomicInteger total = new AtomicInteger();
        buffers.stream().map(Buffer::remaining).forEach(total::addAndGet);
        ByteBuffer joined = ByteBuffer.allocate(total.get());
        for (ByteBuffer buf : buffers) {
            joined.put(buf);
        }
        buffers.clear();
        joined.position(0);
        return joined;
    }
}
