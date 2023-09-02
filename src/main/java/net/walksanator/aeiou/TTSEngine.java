package net.walksanator.aeiou;

import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;

public interface TTSEngine {
    /**
     * speaks the given message
     * @param message the message to be spoken
     * @return a Pair of the rate (hz) and a byte buffer of unsigned 8-bit pcm in little endian at the returned hz
     */
    Pair<Integer, ByteBuffer> renderMessage(String message) throws IOException, InterruptedException;

    /**
     * set a config vale on this TTS
     * there can be certain configs prefixed with @ which are meant to be used internally and not directly configured
     * current ones are
     * \@enabled: whether to speak messages by this user, "true" or "false"
     * \@engine should be ignored as this shouldn't change
     * @param key the key to set
     * @param value the value to set it to
     */
    void updateConfig(String key, String value);

    void resetConfig(String key);

    /**
     * gets a value from the TTS config
     * there can be certain configs prefixed with @ which are meant to be used internally and not directly configured
     * current ones are
     * \@enabled: whether to speak messages by this user, "true" or "false"
     * \@engine: the key in AeiouMod.engines to get this TTS engines initializer
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
     * store the TTS engine configs and shutdown anything you need
     * @return the TTS engines config stored in a Map of config/value
     */
    Map<String,String> shutdownAndSave();
}
