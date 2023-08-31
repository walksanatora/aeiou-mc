package net.walksanator.aeiou;

import net.minecraft.client.sound.AudioStream;

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

    @Override
    public ByteBuffer getBuffer(int size) throws IOException {
        ByteBuffer poppedBytes = ByteBuffer.allocate(size);
        for (int i = min(size,buffer.remaining());i>0;i--) {
            poppedBytes.put(buffer.get());
        }
        poppedBytes.flip();
        AeiouMod.LOGGER.info("yielding %d bytes".formatted(size));
        return poppedBytes.hasRemaining()?poppedBytes:null;
    }

    @Override
    public void close() throws IOException {

    }
}
