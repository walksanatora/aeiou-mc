package net.walksanator.aeiou.engines;

import net.minecraft.util.Pair;
import net.walksanator.aeiou.AeiouMod;
import net.walksanator.aeiou.TTSEngine;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class NullEngine implements TTSEngine {
    private final Map<String, String> configs;

    NullEngine(Map<String,String> cfg) {
        this.configs = cfg;
    }

    @Override
    public Pair<Integer,ByteBuffer> renderMessage(String message) throws IOException, InterruptedException {
        throw new IOException("Tried to render message on a Null engine");
    }

    @Override
    public void updateConfig(String key, String value) {
        configs.put(key,value);
    }

    @Override
    public void resetConfig(String key) {configs.remove(key);}

    @Nullable
    @Override
    public String getConfig(String key) {
        return configs.get(key);
    }

    @Override
    public List<String> getConfigs() {
        return new ArrayList<>();
    }

    @Override
    public Map<String, String> getDefaults() {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> getRandom() {
        return new HashMap<>();
    }

    @Override
    public Map<String, String> shutdownAndSave() {
        return configs;
    }

    public static TTSEngine build(Map<String,String>cfg) {
        return new NullEngine(cfg);
    }
}
