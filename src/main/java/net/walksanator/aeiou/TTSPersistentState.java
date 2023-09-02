package net.walksanator.aeiou;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import java.util.*;

public class TTSPersistentState extends PersistentState {
    Map<UUID, Map<String,String>> configurations;
    List<UUID> banned;
    TTSPersistentState() {
        this.configurations = new HashMap<>();
        this.banned = new ArrayList<>();
    }
    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtCompound tts_configs = new NbtCompound();
        for (UUID key : configurations.keySet()) {
            String cfg_owner = key.toString();
            Map<String,String> subtag = configurations.get(key);
            NbtCompound configs = new NbtCompound();
            for (String cfg : subtag.keySet()) {
                configs.put(cfg, NbtString.of(subtag.get(cfg)));
            }
            tts_configs.put(cfg_owner,configs);
        }
        nbt.put("tts_configs",tts_configs);

        NbtList banned_users = new NbtList();
        for (UUID banned : banned) {
            banned_users.add(
                    NbtString.of(banned.toString())
            );
        }
        nbt.put("banned",banned_users);
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

        NbtList banned_users = tag.getList("banned", NbtString.STRING_TYPE);
        for (NbtElement banned_string : banned_users) {
            String uuid = banned_string.toString();
            UUID cfg_owner = UUID.fromString(uuid);
            serverState.banned.add(cfg_owner);
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

    public boolean ban(UUID speaker) {
        return banned.add(speaker);
    }
    public boolean isBanned(UUID speaker) {
        return banned.contains(speaker);
    }
    public boolean unBan(UUID speaker) {
        return banned.remove(speaker);
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
