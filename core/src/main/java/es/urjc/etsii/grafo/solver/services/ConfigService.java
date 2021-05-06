package es.urjc.etsii.grafo.solver.services;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConfigService {

    private static Environment env;

    public ConfigService(Environment env) {
        ConfigService.env = env;
    }

    public static Optional<Integer> getInt(String key){
        return getString(key).map(Integer::valueOf);
    }

    public static Optional<Long> getLong(String key){
        return getString(key).map(Long::valueOf);
    }

    public static Optional<Double> getDouble(String key){
        return getString(key).map(Double::valueOf);
    }

    public static Optional<Boolean> getBoolean(String key){
        return getString(key).map(Boolean::valueOf);
    }

    public static int getInt(String key, int defaultValue){
        return getInt(key).orElse(defaultValue);
    }

    public static long getLong(String key, long defaultValue){
        return getLong(key).orElse(defaultValue);
    }

    public static double getDouble(String key, double defaultValue){
        return getDouble(key).orElse(defaultValue);
    }

    public static boolean getBoolean(String key, boolean defaultValue){
        return getBoolean(key).orElse(defaultValue);
    }

    public static Optional<String> getString(String key){
        var v = env.getProperty(key);
        if(v == null){
            return Optional.empty();
        } else {
            return Optional.of(v);
        }
    }

    public static String getString(String key, String defaultValue){
        return getString(key).orElse(defaultValue);
    }

}
