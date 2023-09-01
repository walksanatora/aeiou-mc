package net.walksanator.aeiou.engines;

import net.walksanator.aeiou.AeiouMod;
import net.walksanator.aeiou.BetterOutputReader;
import net.walksanator.aeiou.TTSEngine;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SAMEngine implements TTSEngine {
    private final Map<String,String> configs;
    private Process sam_instance;

    SAMEngine(Map<String,String> cfg) {
        this.configs = cfg;
        ProcessBuilder sam_inline = new ProcessBuilder("sam-inline");
        sam_inline.redirectError(ProcessBuilder.Redirect.INHERIT);
        try {
            sam_instance = sam_inline.start();
        } catch (IOException e) {
            sam_instance = null;
        }
    }
    @Override
    public ByteBuffer renderMessage(String message) throws IOException {
        if (sam_instance != null) {
            AeiouMod.LOGGER.info("sam instance is non-null, speaking");
            OutputStream out = sam_instance.getOutputStream();
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
            AeiouMod.LOGGER.info("written message to sam instance STDIN");

            InputStream input = sam_instance.getInputStream();
            return BetterOutputReader.betterRead(input,1000);
        }
        return null;
    }

    @Override
    public void updateConfig(String key, String value) {
        configs.put(key,value);
    }

    public static TTSEngine initialize(Map<String, String> cfg) {
        return new SAMEngine(cfg);
    }

    @Override
    public String getConfig(String key) {
        return configs.get(key);
    }

    @Override
    public List<String> getConfigs() {
        List<String> configs = new ArrayList<>();
        configs.add("pitch"); // who knows what these numbers do
        configs.add("speed");
        configs.add("throat");
        configs.add("mouth");
        return configs;
    }

    @Override
    public Map<String, String> getDefaults() {
        Map<String,String> configs = new HashMap<>();
        configs.put("mouth","0"); // who knows what these numbers do
        configs.put("pitch","0");
        configs.put("speed","0");
        configs.put("throat","0");
        return configs;
    }

    @Override
    public Map<String, String> getRandom() {
        HashMap<String,String> random = new HashMap<>();
        Random rng = new Random();
        random.put("pitch", String.valueOf(64+rng.nextInt(-32,32)));
        random.put("speed",String.valueOf(72+rng.nextInt(-36,36)));
        random.put("throat",String.valueOf(128+rng.nextInt(-64,64)));
        random.put("mouth",String.valueOf(128+rng.nextInt(-64,64)));

        return random;
    }

    @Override
    public Map<String, String> save() {
        sam_instance.destroy();
        return null;
    }
}
