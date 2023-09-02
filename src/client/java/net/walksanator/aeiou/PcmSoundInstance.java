package net.walksanator.aeiou;

import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class PcmSoundInstance extends AbstractSoundInstance {
    PcmAudioStream PCM_INSTANCE;
    public PcmSoundInstance(ByteBuffer buf,int hz) {
        super(new Identifier("aeiou","pcm"),SoundCategory.PLAYERS,SoundInstance.createRandom());
        this.PCM_INSTANCE = new PcmAudioStream(buf,hz);
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.NONE;
    }

    public PcmSoundInstance(PcmAudioStream stream) {
        super(new Identifier("aeiou","pcm"),SoundCategory.PLAYERS,SoundInstance.createRandom());
        this.PCM_INSTANCE = stream;
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
        return CompletableFuture.completedFuture(PCM_INSTANCE);
    }
}
