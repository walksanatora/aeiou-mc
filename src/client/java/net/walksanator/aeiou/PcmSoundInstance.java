package net.walksanator.aeiou;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.*;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class PcmSoundInstance extends AbstractSoundInstance {
    PcmAudioStream PCM_INSTANCE;
    Vec3d pos;
    public PcmSoundInstance(ByteBuffer buf, int hz, Vec3d pos,float dist) {
        super(new Identifier("aeiou","pcm"),SoundCategory.PLAYERS,SoundInstance.createRandom());
        this.PCM_INSTANCE = new PcmAudioStream(buf,hz);
        this.pos = pos;
        this.volume = dist>0?dist:1.0f;
    }

    @Override
    public boolean isRelative() {
        return false;
    }

    @Override
    public double getX() {
        return pos.x;
    }

    @Override
    public double getY() {
        return pos.y;
    }

    @Override
    public double getZ() {
        return pos.z;
    }

    @Override
    public AttenuationType getAttenuationType() {
        return volume>0? AttenuationType.LINEAR:AttenuationType.NONE;
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
        return CompletableFuture.completedFuture(PCM_INSTANCE);
    }
}
