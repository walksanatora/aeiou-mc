package net.walksanator.aeiou.engines;

import net.minecraft.util.Pair;
import net.walksanator.aeiou.AeiouMod;
import net.walksanator.aeiou.TTSEngine;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DectalkEngine implements TTSEngine {
    private final static String[] voices = new String[]{"[:nb]","[:nd]","[:nf]","[:nh]","[:nk]","[:np]","[:nr]","[:nu]","[:nw]"};
    private final Map<String,String> configs;

    DectalkEngine(Map<String,String> cfg) {
        this.configs = cfg;
    }

    @Override
    public Pair<Integer,ByteBuffer> renderMessage(String message) throws IOException, InterruptedException {
        ProcessBuilder dectalk = new ProcessBuilder();
        dectalk.command(
                "dectalk",
                "-fo", "stdout:raw",
                "-e", "2",
                "-pre", configs.getOrDefault("init","[:phoneme on][:err ignore]"),
                "-a", configs.getOrDefault("pre","[:np]"),
                "-a", message
        );
        dectalk.redirectError(ProcessBuilder.Redirect.INHERIT);
        dectalk.redirectOutput(ProcessBuilder.Redirect.PIPE);
        AeiouMod.LOGGER.info("dectalk built");
        Process dt = dectalk.start();
        InputStream input = dt.getInputStream();
        dt.waitFor(500, TimeUnit.MILLISECONDS);
        byte[] temp = input.readAllBytes();
        return new Pair<>(11025,ByteBuffer.wrap(temp));
    }

    @Override
    public void updateConfig(String key, String value) {
        configs.put(key,value);
    }

    public static TTSEngine initialize(Map<String, String> cfg) {
        return new DectalkEngine(cfg);
    }

    @Nullable
    @Override
    public String getConfig(String key) {
        return configs.get(key);
    }

    @Override
    public List<String> getConfigs() {
        //dectalk doesn't really have that many options
        List<String> configs = new ArrayList<>();
        configs.add("init"); //sent to "setup" the TTS engine eg: setting default voice
        configs.add("pre");  //prepended to every message sent to the TTS
        return configs;
    }

    @Override
    public Map<String, String> getDefaults() {
        HashMap<String,String> defaults = new HashMap<>();
        defaults.put("init","");
        defaults.put("pre","");
        return defaults;
    }

    @Override
    public Map<String, String> getRandom() {
        HashMap<String,String> values = new HashMap<>();
        Random rng = new Random();
        values.put(
                "pre",
                voices[rng.nextInt(voices.length)]
        );
        return values;
    }

    @Override
    public Map<String, String> shutdownAndSave() {
        return configs;
    }
}
