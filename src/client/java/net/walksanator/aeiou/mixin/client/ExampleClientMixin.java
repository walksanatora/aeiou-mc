package net.walksanator.aeiou.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.sound.SoundManager;
import net.walksanator.aeiou.AeiouMod;
import net.walksanator.aeiou.MassiveFileForB64Data;
import net.walksanator.aeiou.PcmSoundInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(TitleScreen.class)
public abstract class ExampleClientMixin {

	@Inject(at = @At("HEAD"), method = "init")
	private void run(CallbackInfo info) {
		SoundManager sm = MinecraftClient.getInstance().getSoundManager();
		if (sm!=null) {
			sm.play(new PcmSoundInstance(ByteBuffer.wrap(MassiveFileForB64Data.HELLO)));
		}
	}
}