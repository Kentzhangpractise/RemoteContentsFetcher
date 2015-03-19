package com.pactera.kent.remotecontentsfetcher.cache;

import android.graphics.Bitmap;

/**
 * Created by Kent on 18/03/2015.
 */
public interface ICache {

    /**
     * Insert the Bitmap into Cache with a specified key.
     *
     * @param key
     *            the key.
     * @param val
     *            the value.
     * @return
     *            TRUE: if inserted successfully. FALSE otherwise
     */
    public boolean put(String key, Bitmap val);

    /**
     * Clear all cached Bitmap Data.
     */
    public void clear();

    /**
     * Check if Data existed in Cache with the specified key.
     *
     * @param key
     *            the key.
     * @return TRUE if key existed, FALSE otherwise
     *
     */
    public boolean isExistedInCache(String key);

    /**
     * Returns the value of the Cache with the specified key.
     *
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or null
     *         if no mapping for the specified key is found.
     */
    public byte[] obtainByteArrayInCache(String key);

    /**
     * Returns the value of the Cache with the specified key.
     *
     * @param key
     *            the key.
     * @return the value of the mapping with the specified key, or null
     *         if no mapping for the specified key is found.
     */
    public Bitmap obtainBitmapDataInCache(String key);

    /**
     * Dump all data in Cache, for debug purpose.
     */
    public void dump();


    /**
     * Currently supported Cache type.
     *
     * FAST_CACHE: Cache data in Memory
     * SLOW_CACHE: Cache data on FS
     *
     */
    public enum CACHE_TYPE{
        FAST_CACHE,
        SLOW_CACHE
    }
}
