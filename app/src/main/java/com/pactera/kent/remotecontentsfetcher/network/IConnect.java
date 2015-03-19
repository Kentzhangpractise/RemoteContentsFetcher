package com.pactera.kent.remotecontentsfetcher.network;

import android.graphics.Bitmap;

/**
 * Created by Kent on 18/03/2015.
 */
public interface IConnect {

    /**
     * Very simple result, keep loose coupling with caller.
     *
     */
    public enum RESULT{
        OK,
        NG
    }

    /**
     * Main Callback IF for caller.
     * Note: NO switch to UI thread context.
     *
     */
    public interface INotify{
        public void NotifyCallerByBitmap(RESULT result, String id, Bitmap obj);
        public void NotifyCallerByByteArray(RESULT result, String id, byte[] ba);
    }

    /**
     * Get current connection state, not mandatory.
     *
     */
    public boolean isConnectionValid();

    /**
     * Down remote data by url, data will be returned user defined INotify function by Bitmap.
     *
     * @param url
     *            the url to the remote resource.
     * @param cb
     *            the callback IF used to notify caller thre result.
     *
     * Note: NO switch to UI thread context
     */
    public void obtainRemoteDataByBitmapAsync(String url, INotify cb);

    /**
     * Down remote data by url, data will be returned by user defined INotify function by ByteArray.
     *
     * @param url
     *            the url to the remote resource.
     * @param cb
     *            the callback IF used to notify caller thre result.
     *
     * Note: NO switch to UI thread context
     */
    public void obtainRemoteDataByByteArrayAsync(String url, INotify cb);

    /**
     * Down remote data by url, data will be returned by Bitmap.
     *
     * @param url
     *            the url to the remote resource.
     *@return Bitmap
     *            Remote data will be returned with in same Thread
     * Note: NO switch to UI thread context
     */
    public Bitmap obtainRemoteDataByBitmapSync(String url);

    /**
     * Down remote data by url, data will be returned by ByteArray.
     *
     * @param url
     *            the url to the remote resource.
     *@return byte[]
     *            Remote data will be returned with in same Thread
     * Note: NO switch to UI thread context
     */
    public byte[] obtainRemoteDataByByteArraySync(String url);

}
