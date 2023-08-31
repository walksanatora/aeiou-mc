package net.walksanator.aeiou;

import net.minecraft.client.sound.AudioStream;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.nio.ByteBuffer;

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
        for (int i = size;i>0;i--) {
            poppedBytes.put(buffer.get());
        }
        poppedBytes.flip();
        return poppedBytes;
    }

    @Override
    public void close() throws IOException {

    }
}
