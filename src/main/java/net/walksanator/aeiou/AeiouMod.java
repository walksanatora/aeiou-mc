package net.walksanator.aeiou;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.walksanator.aeiou.engines.DectalkEngine;
import net.walksanator.aeiou.engines.SAMEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

import static java.lang.Math.min;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class AeiouMod implements ModInitializer {

	public static final Map<String, Function<Map<String,String>,TTSEngine>> engines = new HashMap<>();
	public static TTSPersistentState config_state;
	public static Map<UUID,TTSEngine> active_engines = new HashMap<>();
	private static byte rolling = -128;

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

		if (isProgramOnPath("dectalk")) {
			LOGGER.info("Enabling dectalk module");
			engines.put("dectalk", DectalkEngine::initialize);

		}
		if (isProgramOnPath("sam-inline")) {
			LOGGER.info("Enabling Software Automatic Mouth module");
			engines.put("sam", SAMEngine::initialize);
		}

		ServerLifecycleEvents.SERVER_STARTED.register((minecraftServer)-> config_state = TTSPersistentState.getServerState(minecraftServer));
		ServerLifecycleEvents.SERVER_STOPPING.register((minecraftServer)-> {
			for (UUID user : active_engines.keySet()) {
				TTSEngine engine = active_engines.get(user);
				config_state.put(user, engine.shutdownAndSave());
			}
			active_engines.clear();
		});


		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler,packetSender,minecraftServer)-> {
			if (config_state == null) {
				config_state = TTSPersistentState.getServerState(minecraftServer);
			}
			UUID new_player = serverPlayNetworkHandler.player.getUuid();
			String name = serverPlayNetworkHandler.getPlayer().getName().getString();
			Map<String,String> possibly_config = config_state.get(new_player);
			if (possibly_config == null) {
				//they don't have any config. generate a random one for them
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
				String engine = possibly_config.get("@engine");
				if (engine != null) {
					LOGGER.info("%s is using TTS: %s".formatted(name,engine));
					active_engines.put(new_player, engines.get(engine).apply(possibly_config));
				} else {
					LOGGER.error("%s has config, but no TTS!?".formatted(name));
					config_state.remove(new_player);
					serverPlayNetworkHandler.disconnect(Text.literal("Invalid TTS config, TTS config wiped,please rejoin"));
				}
			}
		});
		ServerPlayConnectionEvents.DISCONNECT.register((serverPlayNetworkHandler,minecraftServer) -> {
			UUID new_player = serverPlayNetworkHandler.player.getUuid();
			if (active_engines.containsKey(new_player)) {
				config_state.put(new_player,active_engines.remove(new_player).shutdownAndSave());
			} else {
				LOGGER.warn("%s disconnected without any running TTS engine!?".formatted(serverPlayNetworkHandler.getPlayer().getName().getString()));
			}
		});

		ServerMessageEvents.CHAT_MESSAGE.register((signedMessage,serverPlayerEntity,parameters)->{
			UUID player = serverPlayerEntity.getUuid();
			if (config_state.isBanned(player)) {
				return;
			}
			String message = signedMessage.getContent().getString();
			LOGGER.info(message);
			LOGGER.info(serverPlayerEntity.getUuidAsString());
			if (active_engines.containsKey(player)) {
				LOGGER.info("%s has active TTS program".formatted(player));
				try {
					LOGGER.info("rendering message");
					Pair<Integer,ByteBuffer> sound_data = active_engines.get(player).renderMessage(message);
					int hz = sound_data.getLeft();
					ByteBuffer sound = sound_data.getRight();
					LOGGER.info("rendered message");
					if (sound==null) {throw new IOException();}
					int size = sound.remaining();
					int buffers = (size/(22050*5))+1;
					LOGGER.info("we will need to send %d buffers for %d bytes".formatted(buffers,size));
					List<ServerPlayerEntity> players = serverPlayerEntity.getServer().getPlayerManager().getPlayerList();
					for (int i=1; i<=buffers;i++) {
						PacketByteBuf pbb = PacketByteBufs.create();
						pbb.writeUuid(player);
						pbb.writeByte(rolling);
						pbb.writeByte(buffers);
						pbb.writeByte(i);
						pbb.writeInt(hz);
						byte[] subarray = new byte[22050*5];
						sound.get(0,subarray,0,min(subarray.length,sound.remaining()));
						pbb.writeBytes(sound);
						for (ServerPlayerEntity reciever : players) {
							ServerPlayNetworking.send(reciever,S2CMessagePacketID,new PacketByteBuf(pbb.copy()));
						}
					}
					rolling+=1;

				} catch (IOException | InterruptedException e) {
					LOGGER.warn("Failed to render message");
					e.printStackTrace();
				}
			} else {
				LOGGER.warn("%s has no active TTS, it should have been made when they joined!!".formatted(serverPlayerEntity.getName().getString()));
			}
		});

		//noinspection CodeBlock2Expr
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment)-> {
			dispatcher.register(literal("tts")
					.then(literal("tts")
							.executes(context -> {
								UUID speaker = context.getSource().getEntityOrThrow().getUuid();
								TTSEngine engine = active_engines.get(speaker);
								if (engine != null) {
									context.getSource().sendMessage(Text.literal("Current engine: %s".formatted(engine.getConfig("@engine"))));
								} else {
									context.getSource().sendMessage(Text.literal("no active engine"));
								}
								return 1;
							})
							.then(argument("engine", StringArgumentType.word()).executes(context -> {
								String engine = context.getArgument("engine",String.class);
								UUID speaker = context.getSource().getEntityOrThrow().getUuid();
								ServerCommandSource source = context.getSource();
								Function<Map<String,String>,TTSEngine> selected = engines.get(engine);
								if (selected == null) {
									source.sendMessage(Text.literal("invalid TTS, must be one of %s".formatted(engines.keySet().toString())));
									return 0;
								}
								active_engines.remove(speaker).shutdownAndSave(); // dispose of TTS and all it's configs
								TTSEngine temp = selected.apply(new HashMap<>());
								Map<String,String> random_configs = temp.getRandom();
								random_configs.put("@engine",engine);
								random_configs.put("@enabled","true");
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
										" cfg: changes TTS config values",
										"      with no args, returns all config keys",
										"      with key arg, returns the value for the config",
										" ban: requires level 4, bans a user/uuid from getting their messages read",
										" unban: same as ban but it allows you to allow a persons message to get read",
										" isBanned: does not require OP, checks if a UUID/player is banned"
								};
								context.getSource().sendMessage(Text.literal(String.join("\n",helpMessage)));
								return 1;
							})
					).then(literal("ban")
							.then(literal("uuid").then(argument("user", UuidArgumentType.uuid())
									.requires(source -> source.hasPermissionLevel(4))
									.executes(ctx -> {
										UUID banned = ctx.getArgument("user",UUID.class);
										ServerCommandSource src = ctx.getSource();
										if (config_state.ban(banned)) {
											src.sendMessage(Text.literal("Banned \"User\""));
										} else {
											src.sendMessage(Text.literal("\"User\" was already banned"));
										}
										return 1;
									})
							)).then(literal("player").then(argument("user", EntityArgumentType.player())
									.requires(source -> source.hasPermissionLevel(4))
									.executes( ctx -> {
										EntitySelector player = ctx.getArgument("user", EntitySelector.class);
										ServerCommandSource src = ctx.getSource();
										UUID banned = player.getPlayer(src).getUuid();
										if (config_state.ban(banned)) {
											src.sendMessage(Text.literal("Banned \"User\""));
										} else {
											src.sendMessage(Text.literal("\"User\" was already banned"));
										}
										return 1;
									})
							))
					).then(literal("unban")
							.then(literal("uuid").then(argument("user", UuidArgumentType.uuid())
									.requires(source -> source.hasPermissionLevel(4))
									.executes(ctx -> {
										UUID banned = ctx.getArgument("user",UUID.class);
										ServerCommandSource src = ctx.getSource();
										if (config_state.unBan(banned)) {
											src.sendMessage(Text.literal("Unbanned \"User\""));
										} else {
											src.sendMessage(Text.literal("\"User\" was not banned"));
										}
										return 1;
									})
							)).then(literal("player").then(argument("user", EntityArgumentType.player())
									.requires(source -> source.hasPermissionLevel(4))
									.executes( ctx -> {
										EntitySelector player = ctx.getArgument("user", EntitySelector.class);
										ServerCommandSource src = ctx.getSource();
										UUID banned = player.getPlayer(src).getUuid();
										if (config_state.unBan(banned)) {
											src.sendMessage(Text.literal("Unbanned \"User\""));
										} else {
											src.sendMessage(Text.literal("\"User\" was not banned"));
										}
										return 1;
									})
							))
					).then(literal("isBanned")
							.then(literal("uuid").then(argument("user", UuidArgumentType.uuid())
									.executes(ctx -> {
										UUID banned = ctx.getArgument("user",UUID.class);
										ServerCommandSource src = ctx.getSource();
										if (config_state.isBanned(banned)) {
											src.sendMessage(Text.literal("\"User\" is banned and will not have their messages spoken"));
										} else {
											src.sendMessage(Text.literal("\"User\" is not banned and will have their messages spoken"));
										}
										return 1;
									})
							)).then(literal("player").then(argument("user", EntityArgumentType.player())
									.executes( ctx -> {
										EntitySelector player = ctx.getArgument("user", EntitySelector.class);
										ServerCommandSource src = ctx.getSource();
										UUID banned = player.getPlayer(src).getUuid();
										if (config_state.isBanned(banned)) {
											src.sendMessage(Text.literal("\"User\" is banned and will not have their messages spoken"));
										} else {
											src.sendMessage(Text.literal("\"User\" is not banned and will have their messages spoken"));
										}
										return 1;
									})
							))
					).then(literal("cfg")
									.then(argument("key",StringArgumentType.string())
											.then(argument("value",StringArgumentType.string())
													.executes( ctx -> {
														ServerCommandSource src = ctx.getSource();
														UUID speaker = src.getEntityOrThrow().getUuid();
														String key = ctx.getArgument("key",String.class);
														String value = ctx.getArgument("value",String.class);
														if (active_engines.containsKey(speaker)) {
															TTSEngine engine = active_engines.get(speaker);
															if (engine.getConfigs().contains(key) & Objects.equals(value, "")) {
																engine.resetConfig(key);
																src.sendMessage(Text.literal("reset value for \"%s\"".formatted(key)));
															} else if (engine.getConfigs().contains(key)) {
																engine.updateConfig(key,value);
																src.sendMessage(Text.literal("changed config value \"%s\" to \"%s\"".formatted(key,value)));
															}
														} else {
															src.sendMessage(Text.literal("you don't have a TTS engine instance setup somehow?, you may wanna relog"));
															return 0;
														}
														return 1;
													})
									).executes(ctx -> {
										ServerCommandSource src = ctx.getSource();
										UUID speaker = src.getEntityOrThrow().getUuid();
										String key = ctx.getArgument("key",String.class);
										if (active_engines.containsKey(speaker)) {
											TTSEngine engine = active_engines.get(speaker);
											String value = engine.getConfig(key);
											if (value != null) {
												src.sendMessage(Text.literal("current config value of \"%s\" is \"%s\"".formatted(key,value)));
											} else {
												src.sendMessage(Text.literal("config value is at default or is invalid"));
											}
										} else {
											src.sendMessage(Text.literal("you don't have a TTS engine instance setup somehow?, you may wanna relog"));
											return 0;
										}
										return 1;
									})
							).executes(ctx -> {
								ServerCommandSource src = ctx.getSource();
								UUID speaker = src.getEntityOrThrow().getUuid();
								if (active_engines.containsKey(speaker)) {
									TTSEngine engine = active_engines.get(speaker);
									src.sendMessage(Text.literal("you are currently running engine %s".formatted(engine.getConfig("@engine"))));
									src.sendMessage(Text.literal("possible configs are %s".formatted(engine.getDefaults().keySet().toString())));
								} else {
									src.sendMessage(Text.literal("you don't have a TTS engine instance setup somehow?, you may wanna relog"));
								}
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