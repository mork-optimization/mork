package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.util.Compression;

import java.lang.ref.SoftReference;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Memory-sensitive cache for loaded instances, keyed by canonical path.
 * Uses {@link SoftReference} so that cached instances can be reclaimed
 * by the garbage collector under memory pressure, avoiding OOM errors
 * when working with large instance data.
 *
 * @param <I> Instance class
 */
class InstanceCache<I extends Instance> {

    private final Map<String, SoftReference<I>> cache = new HashMap<>();

    /**
     * Retrieve a cached instance by path, or null if not cached or already evicted.
     *
     * @param path instance path (any format — will be canonicalized)
     * @return cached instance, or null
     */
    I get(String path) {
        var ref = cache.get(canonicalize(path));
        return ref == null ? null : ref.get();
    }

    /**
     * Store an instance in the cache under its canonical path.
     *
     * @param path     instance path used for loading
     * @param instance loaded instance
     */
    void put(String path, I instance) {
        cache.put(canonicalize(path), new SoftReference<>(instance));
    }

    /**
     * Remove all cached instances.
     */
    void clear() {
        cache.clear();
    }

    /**
     * Normalize an instance path to a canonical absolute form.
     * Handles compressed paths (containing {@link Compression#SEP}) by
     * resolving only the container portion to an absolute path.
     *
     * @param instancePath raw instance path
     * @return canonical path
     */
    static String canonicalize(String instancePath) {
        int index = instancePath.indexOf(Compression.SEP);
        if (index < 0) {
            return Path.of(instancePath).toAbsolutePath().toString();
        }
        String container = instancePath.substring(0, index);
        String entry = instancePath.substring(index);
        return Path.of(container).toAbsolutePath() + entry;
    }
}
