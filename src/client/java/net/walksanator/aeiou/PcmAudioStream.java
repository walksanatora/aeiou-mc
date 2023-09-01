package net.walksanator.aeiou;

import net.minecraft.client.sound.AudioStream;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;

import static java.lang.Math.min;

public class PcmAudioStream implements AudioStream {
    static AudioFormat FORMAT = new AudioFormat(22050,8,1,false,false);
    private final ByteBuffer buffer;

    public PcmAudioStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public AudioFormat getFormat() {
        return FORMAT;
    }

    @Nullable
    @Override
    public synchronized ByteBuffer getBuffer(int capacity) {
        ByteBuffer result = BufferUtils.createByteBuffer(capacity);
        int toRead = Math.min(buffer.remaining(), result.remaining());
        result.put(result.position(), buffer, buffer.position(), toRead);
        result.position(result.position() + toRead);
        buffer.position(buffer.position() + toRead);
        result.flip();

        // This is naughty, but ensures we're not enqueuing empty buffers when the stream is exhausted.
        return result.remaining() == 0 ? null : result;
    }

    @Override
    public void close() throws IOException {

    }
}
