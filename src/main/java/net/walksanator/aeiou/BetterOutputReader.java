package net.walksanator.aeiou;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class BetterOutputReader implements Runnable {

    private InputStream is;
    public ByteBuffer res;
    BetterOutputReader(InputStream is) {
        this.is = is;
        this.res = ByteBuffer.allocate(22050*10);
    }

    @Override
    public void run() {
        try {
            int bytesRead;
            while (!((bytesRead = is.read()) == -1) & (res.remaining()>=22050) ) {
                res.put((byte) bytesRead);
            }
        } catch (IOException ignored) {}
    }

    public static ByteBuffer betterRead(InputStream is, int timeout){
        BetterOutputReader bor = new BetterOutputReader(is);
        Thread reader = new Thread(bor);
        reader.start();
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ignored) {};
        reader.stop();//screw you
        return bor.res;
    }
}
