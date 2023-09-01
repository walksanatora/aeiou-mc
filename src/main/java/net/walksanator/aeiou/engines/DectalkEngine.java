package net.walksanator.aeiou.engines;

import net.walksanator.aeiou.AeiouMod;
import net.walksanator.aeiou.TTSEngine;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DectalkEngine implements TTSEngine {
    private final Map<String,String> configs;
    private final ProcessBuilder dectalk;
    private final ProcessBuilder sox;

    DectalkEngine(Map<String,String> cfg) {
        this.configs = cfg;
        sox = new ProcessBuilder("sox -t raw -r 11025 -b 8 -c 1 -e unsigned-integer - -t raw -r 22050 -b 8 -c 1 -e unsigned-integer - vol 1.4".split(" "));
        sox.redirectErrorStream(false);
        dectalk = new ProcessBuilder("dectalk", "-fo", "stdout:raw", "-e,", "2", "-pre",cfg.getOrDefault("init","[:phoneme on][:err off]"));
        dectalk.redirectErrorStream(false);
    }

    @Override
    public ByteBuffer renderMessage(String message) throws IOException {
        AeiouMod.LOGGER.info("dectalk and sox instance is non-null, speaking");
        Process dt = dectalk.start();
        OutputStream out = dt.getOutputStream();
        out.write(message.getBytes(StandardCharsets.UTF_8));
        out.flush();
        out.close();
        AeiouMod.LOGGER.info("written message to dectalk instance STDIN");
        InputStream input = dt.getInputStream();
        Process sox_conv = sox.start();
        OutputStream sox_output = sox_conv.getOutputStream();
        input.transferTo(sox_output);
        sox_output.close();
        dt.destroy(); //we already finished with it so :shrugs:
        AeiouMod.LOGGER.info("written dectalk data to sox");
        InputStream sox_input = sox_conv.getInputStream();
        ByteBuffer result = ByteBuffer.wrap(sox_input.readAllBytes());
        AeiouMod.LOGGER.info("finished audio processing steps");
        sox_conv.destroy();
        return result;
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
        return new HashMap<>();
    }

    @Override
    public Map<String, String> shutdownAndSave() {
        return configs;
    }
}
