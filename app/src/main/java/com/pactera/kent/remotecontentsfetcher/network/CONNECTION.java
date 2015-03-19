package com.pactera.kent.remotecontentsfetcher.network;

import android.content.Context;

/**
 * Created by Kent on 18/03/2015.
 */
public class CONNECTION {
    public static final String TAG = CONNECTION.class.getSimpleName();

    public static IConnect mConnect = null;

    public static synchronized IConnect getInstance(Context ctx){

        if(mConnect == null){
            mConnect = new HttpConnection(ctx);
        }

        return mConnect;
    }
}
