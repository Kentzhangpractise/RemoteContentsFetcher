package com.pactera.kent.remotecontentsfetcher.cache;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URLEncoder;

/**
 * Created by Kent on 18/03/2015.
 */
public class SlowCache implements ICache{
    public static final String TAG = SlowCache.class.getSimpleName();

    private Context mCtx = null;
    private String mCacheDataLocationPath = null;

    public SlowCache(Context ctx){
        mCtx = ctx;
        mCacheDataLocationPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/" + mCtx.getPackageName();

        File root = new File(mCacheDataLocationPath);

        if(!root.exists()){
            root.mkdir();
        }

    }

    @Override
    public boolean put(String key, Bitmap val) {
        FileOutputStream fileOutputStream = null;

        try {
            File cacheFile = new File(mCacheDataLocationPath + File.separator + URLEncoder.encode(key,"UTF-8"));

            if(cacheFile.exists()){
                Log.w(TAG, "obtainBitmapDataInCache, Cache file Existed, key:" + URLEncoder.encode(key,"UTF-8") );
                return true;
            }

            //Save uncompressed object to FS
            fileOutputStream = new FileOutputStream(cacheFile);
            val.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
        }catch(Exception e){
            Log.e(TAG, "put, Failed");
            e.printStackTrace();
            return false;
        }finally{
            try {
                fileOutputStream.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return true;
    }

    @Override
    public void clear() {

        try{
            File folder = new File(mCacheDataLocationPath);

            if(folder.isDirectory() && folder.exists()){
                for(File f: folder.listFiles()){
                    f.delete();
                }
            }

        }catch(Exception e){
            Log.e(TAG, "clear Failed");
            e.printStackTrace();
        }
    }

    @Override
    public boolean isExistedInCache(String key) {

        try {
            File cacheFile = new File(mCacheDataLocationPath + File.separator + URLEncoder.encode(key, "UTF-8"));
            return cacheFile.exists();
        }catch(Exception e){
            Log.e(TAG, "isExistedInCache()" + "Possibility:  convert key error");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public byte[] obtainByteArrayInCache(String key) {
        FileInputStream fileInputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte [] buffer = null;

        try {
            File cacheFile = new File(mCacheDataLocationPath + File.separator + URLEncoder.encode(key, "UTF-8"));

            if (!cacheFile.exists()) {
                Log.w(TAG, "obtainBitmapDataInCache, No Cache file for, key:" + URLEncoder.encode(key, "UTF-8"));
                return null;
            }

            fileInputStream = new FileInputStream(cacheFile);
            byteArrayOutputStream = new ByteArrayOutputStream();

            int ch;
            while ((ch = fileInputStream.read()) != -1) {
                byteArrayOutputStream.write(ch);
            }

            buffer = byteArrayOutputStream.toByteArray();
            return buffer;
        } catch (Exception e) {
            return new byte[0];
        }finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                }catch(Exception e){
                }
            }
        }
    }

    @Override
    public Bitmap obtainBitmapDataInCache(String key) {
        FileInputStream fileInputStream = null;

        try {
            File cacheFile = new File(mCacheDataLocationPath + File.separator + URLEncoder.encode(key, "UTF-8"));

            if(!cacheFile.exists()){
                Log.w(TAG, "obtainBitmapDataInCache, No Cache file for, key:" + URLEncoder.encode(key,"UTF-8") );
                return null;
            }

            fileInputStream = new FileInputStream(cacheFile);

            //Return uncompressed bitmap from FS
            return BitmapFactory.decodeStream(fileInputStream);
        }catch(Exception e){
            Log.e(TAG, "obtainBitmapDataInCache, Failed");
            e.printStackTrace();
        }finally{
            try {
                if(fileInputStream != null) {
                    fileInputStream.close();
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public void dump() {
        Log.i(TAG, "**********Start dumping data in Slow Cache**********");

        if(mCacheDataLocationPath == null){
            Log.i(TAG, "Cache path is Empty");
            Log.i(TAG, "**********End dumping data in Slow Cache**********");
            return ;
        }

        try{
            File folder = new File(mCacheDataLocationPath);

            if(folder.isDirectory() && folder.exists()){
                for(File f: folder.listFiles()){
                    Log.i(TAG, "Cache File Name: " + f.getName());
                    Log.i(TAG, "Cache File Size: " + f.length());
                }
            }

        }catch(Exception e){
            Log.e(TAG, "clear Failed");
            e.printStackTrace();
        }
        Log.i(TAG, "**********End dumping data in Slow Cache**********");
    }


}
