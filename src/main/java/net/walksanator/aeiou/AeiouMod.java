package net.walksanator.aeiou;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.walksanator.aeiou.engines.DectalkEngine;
import net.walksanator.aeiou.engines.SAMEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static net.minecraft.server.command.CommandManager.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

public class AeiouMod implements ModInitializer {

	public static final Map<String, Function<Map<String,String>,TTSEngine>> engines = new HashMap<>();
	public static TTSPersistentState config_state;
	public static Map<UUID,TTSEngine> active_engines = new HashMap<>();

	public static final Identifier S2CMessagePacketID = new Identifier("aeiou","pcm_audio");

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger("aeiou");
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		if (isProgramOnPath("dectalk")&&isProgramOnPath("sox")) {
			LOGGER.info("Enabling dectalk module");
			engines.put("dectalk", DectalkEngine::initialize);

		}
		if (isProgramOnPath("sam-inline")) {
			LOGGER.info("Enabling Software Automatic Mouth module");
			engines.put("sam", SAMEngine::initialize);
		}

		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer)-> {
			config_state = TTSPersistentState.getServerState(minecraftServer);
		});

		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler,packetSender,minecraftServer)-> {
			if (config_state == null) {
				config_state = TTSPersistentState.getServerState(minecraftServer);
			}
			UUID new_player = serverPlayNetworkHandler.player.getUuid();
			String name = serverPlayNetworkHandler.getPlayer().getName().getString();
			Map<String,String> mabey_config = config_state.get(new_player);
			if (mabey_config == null) {
				//they dont have any config. generate a random one for them
				Random rng = new Random();
				List<String> keys = new ArrayList<>(engines.keySet());
				String engine = keys.get(rng.nextInt(keys.size()));
				Function<Map<String,String>,TTSEngine> builder = engines.get(
						engine
				);
				TTSEngine temp = builder.apply(new HashMap<>());
				Map<String,String> random_configs = temp.getRandom();
				random_configs.put("@engine",engine);
				random_configs.put("@enabled","false");
				LOGGER.info("Created new random configs for %s using %s".formatted(name,engine));
				active_engines.put(new_player,builder.apply(random_configs));
			} else {
				String engine = mabey_config.get("@engine");
				if (engine != null) {
					LOGGER.info("%s is using TTS: %s".formatted(name,engine));
					active_engines.put(new_player, engines.get(engine).apply(mabey_config));
				} else {
					LOGGER.error("%s has config, but no TTS!?".formatted(name));
					config_state.remove(new_player);
					serverPlayNetworkHandler.disconnect(Text.literal("Invalid TTS config, TTS config wiped,please rejoin"));
				}
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler,minecraftServer) -> {
			LOGGER.warn("LEAVE EVENT IS NYI");
			UUID new_player = serverPlayNetworkHandler.player.getUuid();
			if (active_engines.containsKey(new_player)) {
				config_state.put(new_player,active_engines.remove(new_player).save());
			} else {
				LOGGER.warn("%s disconnected without any running TTS engine!?".formatted(serverPlayNetworkHandler.getPlayer().getName().getString()));
			}
		});

		ServerMessageEvents.CHAT_MESSAGE.register((signedMessage,serverPlayerEntity,parameters)->{
			String message = signedMessage.getContent().getString();
			LOGGER.info(message);
			LOGGER.info(serverPlayerEntity.getUuidAsString());
			UUID player = serverPlayerEntity.getUuid();
			if (active_engines.containsKey(player)) {
				LOGGER.info("%s has active TTS program".formatted(player));
				try {
					LOGGER.info("rendering message");
					ByteBuffer sound = active_engines.get(player).renderMessage(message);
					LOGGER.info("rendered message");
					if (sound==null) {throw new IOException();}
					int size = sound.remaining();
					int buffers = (size/(22050*5))+1;

				} catch (IOException e) {
					LOGGER.warn("Failed to render message");
					e.printStackTrace();
				}
				//ServerPlayNetworking.send(serverPlayerEntity,S2CMessagePacketID,);
			} else {
				LOGGER.warn("%s has no active TTS, it should have been made when they joined!!".formatted(serverPlayerEntity.getName().getString()));
			}
		});

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment)-> {
			dispatcher.register(literal("tts")
					.then(literal("tts")
							.then(argument("engine", StringArgumentType.word()).executes(context -> {
								String engine = context.getArgument("engine",String.class);
								UUID speaker = context.getSource().getEntityOrThrow().getUuid();
								ServerCommandSource source = context.getSource();
								Function<Map<String,String>,TTSEngine> selected = engines.get(engine);
								if (selected == null) {
									source.sendMessage(Text.literal("invalid TTS, must be one of %s".formatted(engines.keySet().toString())));
									return 0;
								}
								active_engines.remove(speaker).save(); // dispose of TTS and all it's configs
								TTSEngine temp = selected.apply(new HashMap<>());
								Map<String,String> random_configs = temp.getRandom();
								random_configs.put("@engine",engine);
								random_configs.put("@enabled","false");
								LOGGER.info("Created new random configs for %s using %s".formatted(speaker,engine));
								active_engines.put(speaker,selected.apply(random_configs));
								source.sendMessage(Text.literal("changed TTS to %s".formatted(engine)));
								return 1;
							})
							)
					).then(literal("help")
							.executes(context -> {
								String[] helpMessage = new String[] {
										"TTS has some subcommands.",
										" tts: switched TTS engine (one of %s)".formatted(engines.keySet().toString()),
										" cfg: changes TTS config values"
								};
								context.getSource().sendMessage(Text.literal(String.join("\n",helpMessage)));
								return 1;
							})
					)
			);
		});

	}


	public static boolean isProgramOnPath(String programName) {
		String[] paths = System.getenv("PATH").split(System.getProperty("path.separator"));

		for (String path : paths) {
			if (path.endsWith("/") || path.endsWith("\\")) {
				path = path + programName;
			} else {
				path = path + "/" + programName;
			}

			if (new java.io.File(path).exists()) {
				return true;
			}
		}

		return false;
	}
}