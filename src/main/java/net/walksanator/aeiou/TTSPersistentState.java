package net.walksanator.aeiou;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TTSPersistentState extends PersistentState {
    Map<UUID, Map<String,String>> configurations;
    TTSPersistentState() {
        this.configurations = new HashMap<>();
    }
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (UUID key : configurations.keySet()) {
            String cfg_owner = key.toString();
            Map<String,String> subtag = configurations.get(key);
            NbtCompound configs = new NbtCompound();
            for (String cfg : subtag.keySet()) {
                configs.put(cfg, NbtString.of(subtag.get(cfg)));
            }
            nbt.put(cfg_owner,configs);
        }
        return nbt;
    }

    public static TTSPersistentState createFromNbt(NbtCompound tag) {
        TTSPersistentState serverState = new TTSPersistentState();
        NbtCompound configs = tag.getCompound("tts_configs");
        for (String key : configs.getKeys()) {
            UUID cfg_owner = UUID.fromString(key);
            NbtCompound subtag = configs.getCompound(key);
            Map<String,String> cfgs = new HashMap<>();
            for (String cfg : subtag.getKeys()) {
                cfgs.put(cfg, subtag.getString(cfg));
            }
            serverState.configurations.put(cfg_owner,cfgs);
        }
        return serverState;
    }

    public Map<String,String> get(UUID player) {
        return configurations.get(player);
    }
    public Map<String,String> put(UUID player,Map<String,String> cfgs) {
        return configurations.put(player,cfgs);
    }
    public Map<String,String> remove(UUID player) {
        return configurations.remove(player);
    }

    public static TTSPersistentState getServerState(MinecraftServer server) {
        // First we get the persistentStateManager for the OVERWORLD
        PersistentStateManager persistentStateManager = server
                .getWorld(World.OVERWORLD).getPersistentStateManager();

        // Calling this reads the file from the disk if it exists, or creates a new one and saves it to the disk
        // You need to use a unique string as the key. You should already have a MODID variable defined by you somewhere in your code. Use that.
        TTSPersistentState serverState = persistentStateManager.getOrCreate(
                TTSPersistentState::createFromNbt,
                TTSPersistentState::new,
                "aeiou"
        );

        return serverState;
    }
}
