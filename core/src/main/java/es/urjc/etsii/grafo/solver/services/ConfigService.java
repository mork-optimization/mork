package es.urjc.etsii.grafo.solver.services;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Fallback configuration service for when it is not viable to use a Config DTO.
 * Avoid using this class and use small Config classes
 * (examples: SolverConfig, ExcelConfig, etc.) whenever possible
 */
@Service
public class ConfigService {

    private static Environment env;

    /**
     * Set environment from spring
     *
     * @param env current environment
     */
    public ConfigService(Environment env) {
        ConfigService.env = env;
    }

    /**
     * Get config value as an integer. Throws exception if the value exists but it is not an Integer
     *
     * @param key Config key name
     * @return Config value for the given key
     */
    public static Optional<Integer> getInt(String key){
        return getString(key).map(Integer::valueOf);
    }

    /**
     * Get config value as a long. Throws exception if the value exists but it is not a long
     *
     * @param key Config key name
     * @return Config value for the given key
     */
    public static Optional<Long> getLong(String key){
        return getString(key).map(Long::valueOf);
    }

    /**
     * Get config value as a double. Throws exception if the value exists but it is not a double
     *
     * @param key Config key name
     * @return Config value for the given key
     */
    public static Optional<Double> getDouble(String key){
        return getString(key).map(Double::valueOf);
    }

    /**
     * Get config value as a boolean. Throws exception if the value exists but it is not an boolean
     *
     * @param key Config key name
     * @return Config value for the given key
     */
    public static Optional<Boolean> getBoolean(String key){
        return getString(key).map(Boolean::valueOf);
    }

    /**
     * Get config value as an integer. Throws exception if the value exists but it is not an Integer
     *
     * @param key Config key name
     * @param defaultValue value to return if config key does not exist.
     *                     If the key exists but the value is not an integer an exception is thrown instead.
     * @return Config value for the given key, or default value if config key is not defined in the current environment
     */
    public static int getInt(String key, int defaultValue){
        return getInt(key).orElse(defaultValue);
    }

    /**
     * Get config value as a long. Throws exception if the value exists but it is not a long
     *
     * @param key Config key name
     * @param defaultValue value to return if config key does not exist.
     *                     If the key exists but the value is not a long an exception is thrown instead.
     * @return Config value for the given key, or default value if config key is not defined in the current environment
     */
    public static long getLong(String key, long defaultValue){
        return getLong(key).orElse(defaultValue);
    }

    /**
     * Get config value as a double. Throws exception if the value exists but it is not a double
     *
     * @param key Config key name
     * @param defaultValue value to return if config key does not exist.
     *                     If the key exists but the value is not a double an exception is thrown instead.
     * @return Config value for the given key, or default value if config key is not defined in the current environment
     */
    public static double getDouble(String key, double defaultValue){
        return getDouble(key).orElse(defaultValue);
    }

    /**
     * Get config value as a boolean. Throws exception if the value exists but cannot be converted to a boolean.
     *
     * @param key Config key name
     * @param defaultValue value to return if config key does not exist.
     *                     If the key exists but the value is not a boolean an exception is thrown instead.
     * @return Config value for the given key, or default value if config key is not defined in the current environment
     */
    public static boolean getBoolean(String key, boolean defaultValue){
        return getBoolean(key).orElse(defaultValue);
    }

    /**
     * Get config value as a String. Throws exception if the value exists but cannot be converted to String
     *
     * @param key Config key name
     * @return Config value for the given key
     */
    public static Optional<String> getString(String key){
        var v = env.getProperty(key);
        if(v == null){
            return Optional.empty();
        } else {
            return Optional.of(v);
        }
    }

    /**
     * Get config value as a String. Throws exception if the value exists but cannot be converted to a String
     *
     * @param key Config key name
     * @param defaultValue value to return if config key does not exist.
     * @return Config value for the given key, or default value if config key is not defined in the current environment
     */
    public static String getString(String key, String defaultValue){
        return getString(key).orElse(defaultValue);
    }

}
