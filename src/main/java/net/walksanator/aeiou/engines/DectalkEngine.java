package net.walksanator.aeiou.engines;

import net.walksanator.aeiou.AeiouMod;
import net.walksanator.aeiou.BetterOutputReader;
import net.walksanator.aeiou.TTSEngine;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DectalkEngine implements TTSEngine {
    private final Map<String,String> configs;
    private Process dectalk;
    private Process sox;

    DectalkEngine(Map<String,String> cfg) {
        this.configs = cfg;
        ProcessBuilder sox_command = new ProcessBuilder("sox -t raw -r 11025 -b 8 -c 1 -e unsigned-integer - -t raw -r 22050 -b 8 -c 1 -e unsigned-integer - vol 1.4".split(" "));
        sox_command.redirectErrorStream(false);
        try {
            sox = sox_command.start();

        } catch (IOException e) {
            sox = null;
        }
        ProcessBuilder dec_command = new ProcessBuilder("dectalk", "-fo", "stdout:raw", "-e,", "2", "-pre",cfg.getOrDefault("init","[:phoneme on][:err off]"));
        dec_command.redirectErrorStream(false);
        try {
            dectalk = dec_command.start();
        } catch (IOException e) {
            dectalk = null;
        }

    }

    @Override
    public ByteBuffer renderMessage(String message) throws IOException {
        if ( (dectalk != null) & (sox != null) ) {
            AeiouMod.LOGGER.info("dectalk and sox instance is non-null, speaking");
            OutputStream out = dectalk.getOutputStream();
            out.write(message.getBytes(StandardCharsets.UTF_8));
            out.flush();
            AeiouMod.LOGGER.info("written message to dectalk instance STDIN");
            InputStream input = dectalk.getInputStream();
            OutputStream sox_output = sox.getOutputStream();
            input.transferTo(sox_output);
            AeiouMod.LOGGER.info("written dectalk data to sox");
            InputStream sox_input = sox.getInputStream();
            BetterOutputReader.betterRead(sox_input,1000);
            AeiouMod.LOGGER.info("finished audio processing steps");
            return result;
        }
        return null;
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
        //dectalk doesen't really have that many options
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
        HashMap<String,String> random = new HashMap<>();
        return random;
    }

    @Override
    public Map<String, String> save() {
        sox.destroy();
        dectalk.destroy();
        return configs;
    }
}
