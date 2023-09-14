package net.walksanator.aeiou;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.util.math.Vec3d;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class AeiouModClient implements ClientModInitializer {
	public static HashMap<Byte,BufWrapper> bufs = new HashMap<>();
	private static final List<UUID> shushed = new ArrayList<>();
	@Override
	public void onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		//ClientPacketNetworking.registerGlobalReciever(AeiouMod.S2CMessagePacket);
		ClientPlayNetworking.registerGlobalReceiver(AeiouMod.S2CMessagePacketID,(client,handler,buf,response) -> {
			AeiouMod.LOGGER.debug("recieved packet on client");
			UUID speaker = buf.readUuid();
			if (shushed.contains(speaker)) {return;}
			byte roller = buf.readByte();
			byte total_buffers = buf.readByte();
			byte current_buffer = buf.readByte();
			int hz = buf.readInt();
			boolean is_positional = buf.readBoolean();
			Vec3d pos = new Vec3d(0,0,0); float range = 0.0f;
			if (is_positional) {
				double x = buf.readDouble();
				double y = buf.readDouble();
				double z = buf.readDouble();
				pos = new Vec3d(x,y,z);
				range = buf.readFloat();
			}
			ByteBuffer bbuf = buf.readBytes(buf.readableBytes()).nioBuffer();
			BufWrapper wrapped = bufs.getOrDefault(roller,new BufWrapper());
			wrapped.append(bbuf);
			AeiouMod.LOGGER.debug("message from %s buffer %d/%d roller: %d audio size: %d".formatted(speaker.toString(),current_buffer,total_buffers,roller,bbuf.remaining()));
			if (total_buffers == current_buffer) {
				AeiouMod.LOGGER.debug("final buffer recieved. playing audio");
				ByteBuffer audio = wrapped.concat();
				client.getSoundManager().play(new PcmSoundInstance(audio,hz,is_positional? pos : client.player.getPos(), range));
				bufs.remove(roller);
			} else {
				bufs.put(roller,wrapped);
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(AeiouMod.S2CMuteCommand,(client,handler,buf,response) -> {
			boolean ban = buf.readBoolean();
			UUID target = buf.readUuid();
			if (ban) {
				shushed.add(target);
			} else {
				shushed.remove(target);
			}
		});
	}
}