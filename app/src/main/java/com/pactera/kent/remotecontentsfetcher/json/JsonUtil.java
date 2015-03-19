package com.pactera.kent.remotecontentsfetcher.json;

import android.util.Log;

import com.google.gson.Gson;

/**
 * Created by Kent on 2015/3/18.
 */
public class JsonUtil {
    public static final String TAG = JsonUtil.class.getSimpleName();

    public static JsonContentsDataStructure Parse(String jsonString){


        JsonContentsDataStructure ret;

        try {
            ret = new Gson().fromJson(jsonString,
                    JsonContentsDataStructure.class);
        }catch(Exception e){
            Log.e(TAG, "JSON String parse error");
            e.printStackTrace();
            return null;
        }

        return ret;
    }
}
