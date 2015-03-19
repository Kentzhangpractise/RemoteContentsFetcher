package com.pactera.kent.remotecontentsfetcher.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Kent on 18/03/2015.
 */
public class FastCache implements ICache{
    public static final String TAG = FastCache.class.getSimpleName();

    private Context mCtx = null;
    private Map<String, Bitmap> mMappedCacheData = null;

    public FastCache(Context ctx){
        mCtx = ctx;
        mMappedCacheData = new HashMap<String, Bitmap>();
    }

    @Override
    public synchronized boolean put(String key, Bitmap val) {
        Log.v(TAG, "put Cache");

        if(mMappedCacheData == null){
            Log.e(TAG, "put, Cache Map not initialized properly");
            return false;
        }

        try{
            mMappedCacheData.put(URLEncoder.encode(key,"UTF-8"),val);
        }catch(Exception e){
            Log.e(TAG, "put, Failed to put value in Map");
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    public synchronized void clear() {
        Log.v(TAG, "Clear Cache");
        if(mMappedCacheData != null){
            mMappedCacheData.clear();
        }
    }

    @Override
    public boolean isExistedInCache(String key) {
        try {
            return mMappedCacheData.containsKey(URLEncoder.encode(key,"UTF-8"));
        }catch(Exception e){
            Log.e(TAG, "obtainBitmapDataInCache()" + "Possibility:  convert key error");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] obtainByteArrayInCache(String key) {
        return new byte[0];
    }

    @Override
    public synchronized Bitmap obtainBitmapDataInCache(String key) {
        if(mMappedCacheData == null){
            Log.e(TAG, "obtainBitmapDataInCache, Cache Map not initialized properly");
            return null;
        }

        try {
            return mMappedCacheData.get(URLEncoder.encode(key, "UTF-8"));
        }catch(Exception e){
            Log.e(TAG, "obtainBitmapDataInCache()" + "Possibility:  convert key error");
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void dump() {
        Log.i(TAG, "**********Start dumping data in Fast Cache**********");

        if(mMappedCacheData == null){
            Log.i(TAG, "EMPTY...");
            Log.i(TAG, "**********End dumping data in Fast Cache**********");
            return ;
        }

        Set<Map.Entry<String, Bitmap>> entrySet = mMappedCacheData.entrySet();

        for(Map.Entry<String, Bitmap> entry: entrySet){
            Log.i(TAG, "KEY: " + entry.getKey() + "/VALUE Bitmap Size:"
                    + entry.getValue().getRowBytes());
        }

        Log.i(TAG, "**********End dumping data in Fast Cache**********");
    }
}
