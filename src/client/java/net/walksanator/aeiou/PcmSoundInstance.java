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
    public PcmSoundInstance(ByteBuffer buf) {
        super(new Identifier("aeiou","pcm"),SoundCategory.PLAYERS,SoundInstance.createRandom());
        this.PCM_INSTANCE = new PcmAudioStream(buf);
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
        AeiouMod.LOGGER.info("getting audio stream");
        return CompletableFuture.completedFuture(PCM_INSTANCE);
    }
}
