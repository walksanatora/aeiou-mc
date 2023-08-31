package net.walksanator.aeiou;

import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class PcmSoundInstance implements SoundInstance {
    PcmAudioStream PCM_INSTANCE;
    public PcmSoundInstance(ByteBuffer buf) {
        this.PCM_INSTANCE = new PcmAudioStream(buf);
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
        return CompletableFuture.completedFuture(PCM_INSTANCE);
    }

    @Override
    public Identifier getId() {
        return new Identifier("aeiou","pcm");
    }

    @Nullable
    @Override
    public WeightedSoundSet getSoundSet(SoundManager soundManager) {
        return null;
    }

    @Override
    public Sound getSound() {
        return null;
    }

    @Override
    public SoundCategory getCategory() {
        return SoundCategory.PLAYERS;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public int getRepeatDelay() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public float getPitch() {
        return 0;
    }

    @Override
    public double getX() {
        return 0;
    }

    @Override
    public double getY() {
        return 0;
    }

    @Override
    public double getZ() {
        return 0;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return AttenuationType.NONE;
    }
}
