package com.pactera.kent.remotecontentsfetcher.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Kent on 18/03/2015.
 */
public class HttpConnection implements IConnect {

    public static final String TAG = HttpConnection.class.getSimpleName();

    private Context mContext = null;
    private static final int TIME_OUT = 10000;

    public HttpConnection(Context ctx){
        mContext = ctx;
    }

    @Override
    public boolean isConnectionValid() {


        NetworkInfo activeNetwork =
                        ((ConnectivityManager)mContext
                        .getSystemService(Context.CONNECTIVITY_SERVICE))
                        .getActiveNetworkInfo();

        return  activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    @Override
    public void obtainRemoteDataByBitmapAsync(String url, INotify cb) {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = obtainRemoteData(url);

            if (urlConnection == null) {
                cb.NotifyCallerByBitmap(IConnect.RESULT.NG, url, null);
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());

            if(bitmap != null) {
                Log.i(TAG, "obtainRemoteData, size:" + bitmap.getRowBytes());
                cb.NotifyCallerByBitmap(RESULT.OK, url, bitmap);
            }else{
                Log.w(TAG, "obtainRemoteData, internet connection is fine, but bitmap data is empty");
                cb.NotifyCallerByBitmap(IConnect.RESULT.NG, url, null);
            }
        }catch(Exception e){
            e.printStackTrace();
            return;
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    public void obtainRemoteDataByByteArrayAsync(String url, INotify cb) {

        HttpURLConnection urlConnection = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        InputStream inputStream = null;
        byte buffer[] = null;


        try {
            urlConnection = obtainRemoteData(url);

            if (urlConnection == null) {
                cb.NotifyCallerByBitmap(IConnect.RESULT.NG, url, null);
                return;
            }

            byteArrayOutputStream = new ByteArrayOutputStream();
            inputStream = urlConnection.getInputStream();

            int ch;
            while ((ch = inputStream.read()) != -1) {
                byteArrayOutputStream.write(ch);
            }

            buffer = byteArrayOutputStream.toByteArray();

            Log.i(TAG, "obtainRemoteDataByByteArray, size:" + buffer.length);
            cb.NotifyCallerByByteArray(RESULT.OK, url, buffer);
        }catch(Exception e){
            e.printStackTrace();
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
    public Bitmap obtainRemoteDataByBitmapSync(String url) {

        HttpURLConnection urlConnection = null;
        try {
            urlConnection = obtainRemoteData(url);

            if (urlConnection == null) {
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(urlConnection.getInputStream());

            if(bitmap != null) {
                Log.i(TAG, "obtainRemoteData, size:" + bitmap.getRowBytes());
                return bitmap;
            }else{
                Log.w(TAG, "obtainRemoteData, internet connection is fine, but bitmap data is empty");
            }
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return null;
    }

    @Override
    public byte[] obtainRemoteDataByByteArraySync(String url) {

        HttpURLConnection urlConnection = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        InputStream inputStream = null;
        byte buffer[] = null;


        try {
            urlConnection = obtainRemoteData(url);

            if (urlConnection == null) {
                return null;
            }

            byteArrayOutputStream = new ByteArrayOutputStream();
            inputStream = urlConnection.getInputStream();

            int c;
            while ((c = inputStream.read()) != -1) {
                byteArrayOutputStream.write(c);
            }

            buffer = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            Log.i(TAG, "obtainRemoteDataByByteArray, size:" + buffer.length);
            return buffer;
        }catch(Exception e){
            e.printStackTrace();
            return null;
        }finally {
            if (byteArrayOutputStream != null) {
                try {
                    byteArrayOutputStream.close();
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    private HttpURLConnection obtainRemoteData(String url) {

        HttpURLConnection urlConnection = null;

        try {
            URL resourceURL = new URL(url);
            urlConnection = (HttpURLConnection) resourceURL.openConnection();
            urlConnection.setConnectTimeout(TIME_OUT);
            urlConnection.setReadTimeout(TIME_OUT);
            urlConnection.setRequestMethod("GET");
            urlConnection.setDoInput(true);
            urlConnection.connect();

            //HTTP Connection no successful
            int responseCode = urlConnection.getResponseCode();

            if(HttpURLConnection.HTTP_OK == responseCode ){
                return urlConnection;
            }
            Log.w(TAG, "HTTP CONNECTION Error:" + responseCode);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
