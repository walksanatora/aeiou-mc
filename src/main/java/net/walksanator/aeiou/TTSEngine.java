package net.walksanator.aeiou;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TTSEngine {
    /**
     * speaks the given message
     * @param message the message to be spoken
     * @return raw pcm audio data unsigned 8-bit numbers @22050 hz
     */
    ByteBuffer renderMessage(String message) throws IOException;

    /**
     * set a config vale on this TTS
     * @param key
     * @param value
     */
    void updateConfig(String key, String value);

    /**
     * gets a value from the TTS config
     * @param key they config option to get
     * @return the value of the config
     */
    @Nullable
    String getConfig(String key);

    /**
     * gets all possible config keys accepted by this TTS
     * @return a list of all possibly config keys accepted by the TTS
     */
    List<String> getConfigs();

    /**
     * gets the default values of all configs
     * @return a map of all values and their defaults
     */
    Map<String,String> getDefaults();

    /**
     * gets the default values of all configs
     * @return a map of all values and their defaults
     */
    Map<String,String> getRandom();

    /**
     * store the TTS engine configs into
     * @return the TTS engines config stored in a NBT compound
     */
    Map<String,String> save();
}
