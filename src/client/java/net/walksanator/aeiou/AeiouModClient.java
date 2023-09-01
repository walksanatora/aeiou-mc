package net.walksanator.aeiou;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.UUID;

public class AeiouModClient implements ClientModInitializer {
	public static HashMap<Integer,BufWrapper> bufs = new HashMap<Integer, BufWrapper>();
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		//ClientPacketNetworking.registerGlobalReciever(AeiouMod.S2CMessagePacket);
		ClientPlayNetworking.registerGlobalReceiver(AeiouMod.S2CMessagePacketID,(client,handler,buf,response) -> {
			AeiouMod.LOGGER.info("recieved packet on client");
			UUID speaker = buf.readUuid();
			int roller = ((int)buf.readByte())+128;//convert the index into a unsigned number
			byte total_buffers = buf.readByte();
			byte current_buffer = buf.readByte();
			ByteBuffer bbuf = buf.readBytes(buf.readableBytes()).nioBuffer();
			BufWrapper wrapped = bufs.getOrDefault(roller,new BufWrapper());
			wrapped.append(bbuf);
			AeiouMod.LOGGER.info("message from %s buffer %d/%d roller: %d audio size: %d".formatted(speaker.toString(),current_buffer,total_buffers,roller,bbuf.remaining()));
			if (total_buffers == current_buffer) {
				AeiouMod.LOGGER.info("final buffer recieved. playing audio");
				ByteBuffer audio = wrapped.concat();
				client.getSoundManager().play(new PcmSoundInstance(audio));
				bufs.remove(roller);
			} else {
				bufs.put(roller,wrapped);
			}
		});
	}
}