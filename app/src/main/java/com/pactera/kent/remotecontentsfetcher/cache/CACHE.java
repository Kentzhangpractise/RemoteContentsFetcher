package com.pactera.kent.remotecontentsfetcher.cache;

import android.content.Context;

/**
 * Created by Kent on 18/03/2015.
 */
public class CACHE {
    public static final String TAG = CACHE.class.getSimpleName();

    public static ICache mFastCache = null;
    public static ICache mSlowCache = null;

    public static synchronized ICache getInstance(Context ctx, ICache.CACHE_TYPE type){
        ICache ret = null;

        switch(type){
            case FAST_CACHE:
                if(mFastCache == null){
                    mFastCache = new FastCache(ctx);
                }
                ret = mFastCache;
                break;
            case SLOW_CACHE:
                if(mSlowCache == null){
                    mSlowCache = new SlowCache(ctx);
                }
                ret = mSlowCache;
                break;
            default:
                break;
        }
        return ret;
    }
}
