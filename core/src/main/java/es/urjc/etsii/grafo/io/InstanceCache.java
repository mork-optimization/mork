package es.urjc.etsii.grafo.io;

import es.urjc.etsii.grafo.util.IOUtil;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Memory-sensitive cache for loaded instances, keyed by canonical load path.
 * Uses {@link SoftReference} so that cached instances can be reclaimed
 * by the garbage collector under memory pressure, avoiding OOM errors
 * when working with large instance data.
 *
 * @param <I> Instance class
 */
class InstanceCache<I extends Instance> {

    private final Map<String, SoftReference<I>> cache = new ConcurrentHashMap<>();

    /**
     * Retrieve a cached instance by load path, or null if not cached or already evicted.
     *
     * @param path instance load path. It may be absolute, relative, or compressed.
     * @return cached instance, or null
     */
    I get(String path) {
        var ref = cache.get(canonicalize(path));
        return ref == null ? null : ref.get();
    }

    /**
     * Store an instance in the cache under its canonical load path.
     *
     * @param path     instance load path used for loading
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
     * Normalize an instance load path to a canonical absolute form.
     *
     * @param instancePath raw instance load path
     * @return canonical load path
     */
    static String canonicalize(String instancePath) {
        return IOUtil.absoluteLoadPath(instancePath);
    }
}
