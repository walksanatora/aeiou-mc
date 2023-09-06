package net.walksanator.aeiou.engines;

import net.minecraft.util.Pair;
import net.walksanator.aeiou.AeiouMod;
import net.walksanator.aeiou.Functions;
import net.walksanator.aeiou.TTSEngine;
import net.walksanator.aeiou.wasm.LinearMemorySupport;
import net.walksanator.aeiou.wasm.SamWasm;
import wasm_rt_impl.Memory;
import wasm_rt_impl.ModuleRegistry;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.function.Function;

public class SAMWasmEngine implements TTSEngine {
    private final ModuleRegistry mr;
    private final SamWasm sam;
    private final Map<String,String> configs;

    SAMWasmEngine(Map<String,String> cfg) {
        mr = new ModuleRegistry();
        Functions wasmVM = new Functions(mr);
        wasmVM.setupModuleRegister();
        sam = new SamWasm(mr,"wasam");
        this.configs = cfg;
    }
    @Override
    public Pair<Integer,ByteBuffer> renderMessage(String message) throws IOException {
        Function<String,Integer> x = (k)-> Integer.parseInt(configs.getOrDefault(k,"0"));
        sam.w2k_setupSpeak(
                x.apply("pitch"),
                x.apply("speed"),
                x.apply("throat"),
                x.apply("mouth")
        );
        int s_alloc = sam.w2k_dlmalloc(256);
        Memory mem = mr.importMemory("Z_env","Z_memory");
        LinearMemorySupport.INSTANCE.writeCString(mem,s_alloc,message);
        AeiouMod.LOGGER.info("SAM memory setup");
        int res_ptr = sam.w2k_speakText(s_alloc);
        if (mem.i32_load(res_ptr)==0) {
            throw new IOException("Failed to render message");
        }
        int pcm_buf_start = mem.i32_load(res_ptr,8);
        int pcm_buf_size = mem.i32_load(res_ptr,4);
        byte[] buffer = new byte[pcm_buf_size];
        for (int i = 0;i<pcm_buf_size;i++) {
            buffer[i] = (byte) mem.i32_load8_s(pcm_buf_start,i);
        }
        ByteBuffer temp = ByteBuffer.wrap(buffer);
        sam.w2k_dlfree(s_alloc);
        sam.w2k_dlfree(res_ptr);

        return new Pair<>(22050,temp);
    }

    @Override
    public void resetConfig(String key) {configs.remove(key);}

    @Override
    public void updateConfig(String key, String value) {
        configs.put(key,value);
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
    public Map<String, String> shutdownAndSave() {
        return configs;
    }

    public static TTSEngine build(Map<String,String> cfg) {
        return new SAMWasmEngine(cfg);
    }
}

