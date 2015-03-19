package com.pactera.kent.remotecontentsfetcher.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Vibrator;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pactera.kent.remotecontentsfetcher.R;
import com.pactera.kent.remotecontentsfetcher.cache.CACHE;
import com.pactera.kent.remotecontentsfetcher.cache.ICache;
import com.pactera.kent.remotecontentsfetcher.json.JsonContentsDataStructure;
import com.pactera.kent.remotecontentsfetcher.json.JsonUtil;
import com.pactera.kent.remotecontentsfetcher.network.CONNECTION;
import com.pactera.kent.remotecontentsfetcher.network.IConnect;

import static android.R.color;

public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener{
    public static final String TAG = MainActivity.class.getSimpleName();

    //UX Controls
    private SwipeRefreshLayout mSwipeLayout = null;
    private ListView mListContainer = null;
    private SimpleListAdapter mListAdapter = null;

    //Contents Data
    private JsonContentsDataStructure mContentsCache = null;
    private ICache mFastCache = null, mSlowCache = null;

    //Local Event Handler
    private Handler mHandler = new Handler();

    //Local Variable
    private long mDoubleClickInterval = 0;
    private ActionBar actionBar;
    private Vibrator mVibrator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialize Control
        //SwipeLayout -> pull to refresh
        //ListView -> Main contents container
        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.SWIPELAYOUT);
        mSwipeLayout.setOnRefreshListener(this);
        mSwipeLayout.setColorSchemeResources(
                color.holo_blue_bright,
                color.holo_green_light,
                color.holo_orange_light,
                color.holo_red_light);

        mListContainer = (ListView) findViewById(R.id.LISTCONTAINER);
        mListAdapter = new SimpleListAdapter();

        //Init Local Cache
        mFastCache = CACHE.getInstance(this, ICache.CACHE_TYPE.FAST_CACHE);
        mSlowCache = CACHE.getInstance(this, ICache.CACHE_TYPE.SLOW_CACHE);

        //ActionBar initialization
        actionBar = getSupportActionBar();

        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

    }

    @Override
    public void onResume(){
        super.onResume();
        onRefresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch(item.getItemId()){
            case R.id.action_refresh:
                mSwipeLayout.setRefreshing(true);
                onRefresh();
                return true;

            case R.id.action_clear:
                //Indicate user all cache will be cleaned
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                builder.setMessage("About to delete all local cache data, Continue?");
                builder.setTitle("Attention");

                builder.setNegativeButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        clearAllCache();
                        mSwipeLayout.setRefreshing(true);
                        onRefresh();
                    }
                });

                builder.setPositiveButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                builder.create().show();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if ((System.currentTimeMillis() - mDoubleClickInterval) > 2000) {
                Toast.makeText(this, "Click again to exit", Toast.LENGTH_LONG).show();
                mDoubleClickInterval = System.currentTimeMillis();
            } else {
                finish();
            }
        }
        return true;
    }
    @Override
    public void onRefresh() {
        final Activity activity = this;

        //Renew Remote Json Contents
        final IConnect connectIF = CONNECTION.getInstance(this);

        //Indicate Remote Loading Action, Note action happens with SwipeView.
        //No Lazy Loading bitmap action need update app title.
        setTitle(getString(R.string.title_loading));
        new Thread(new Runnable() {
            @Override
            public void run() {
                //Start fetching remote contents
                byte[] buffer = getJsonByUniqueID(getString(R.string.contents_url));

                if(buffer != null){
                    mContentsCache = JsonUtil.Parse(new String(buffer));

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Reset app name if NO remote contents available.
                            activity.setTitle(mContentsCache.title);
                        }
                    });
                }else{
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            //Reset app name if NO remote contents available.
                            activity.setTitle(activity.getString(R.string.app_name));
                            stopSwipeRefreshing();

                            mVibrator.vibrate(1000);
                            //Indicate when neither remote contents nor local cache is available
                            Toast.makeText(activity, "Failed to get Remote Json Content", Toast.LENGTH_LONG).show();
                        }
                    });
                    return;
                }

                //update ListView on UI Thread
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //make sure data is ready for list view when show at 1st time
                        if(mListContainer.getAdapter() == null) {
                            mListContainer.setAdapter(mListAdapter);
                        }

                        mListContainer.invalidate();
                        stopSwipeRefreshing();

                    }
                });
                }
        }).start();
    }

    private void stopSwipeRefreshing(){
        if( mSwipeLayout.isRefreshing()){
            mSwipeLayout.setRefreshing(false);
            mVibrator.vibrate(100);
        }
    }

    //Start downloading if no json data cached locally
    //Fix: blank screen shows at startup if no network connection detected
    private byte[] getJsonByUniqueID(String url){
        byte[] ret = null;
        try {
            if (mSlowCache.isExistedInCache(url)) {
                ret = mSlowCache.obtainByteArrayInCache(url);
            } else {
                //Cache not hit, start downloading procedure
                ret =  CONNECTION.getInstance(this).obtainRemoteDataByByteArraySync(url);
            }
        }catch (Exception e){
            Log.e(TAG, "getBitmapByUniqueID(),"
                    + "Possibility: Cache fetching Error, KEY:"
                    + url);
            return null;
        }
        return ret;
    }

    //Start downloading if no bitmap data cached locally
    private Bitmap getBitmapByUniqueID(final String url){

        Bitmap ret = null;

        try {
            //In case of empty or unavailable URL
            if(url.length() == 0) {
                return null;
            }

            if (mFastCache.isExistedInCache(url)) {
                ret = mFastCache.obtainBitmapDataInCache(url);
            } else if (mSlowCache.isExistedInCache(url)) {
                ret = mSlowCache.obtainBitmapDataInCache(url);
            } else {
                //Cache not hit, start downloading procedure
                final IConnect connectIF = CONNECTION.getInstance(this);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        connectIF.obtainRemoteDataByBitmapAsync(url, new Notifier() );
                    }
                }).start();

            }
        }catch (Exception e){
            Log.e(TAG, "getBitmapByUniqueID(),"
                    + "Possibility: Cache fetching Error, KEY:"
                    + url);
        }

        return ret;
    }

    private void clearAllCache(){
        try {
            mSlowCache.clear();
            mFastCache.clear();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public class Notifier implements IConnect.INotify{

        @Override
        public void NotifyCallerByBitmap(IConnect.RESULT result, final String id, final Bitmap obj) {
            Log.i(TAG, "NotifyCallerByBitmap()," + "RESULT:" + result);

            switch(result){
                case OK:
                    //update Bitmap data into local cache
                    try {
                        mSlowCache.put(id, obj);
                        mFastCache.put(id, obj);
                    }catch(Exception e){
                        Log.e(TAG, "NotifyCallerByBitmap()," + "Cache update Error" );
                        e.printStackTrace();
                        return;
                    }

                    //update ImageView on UI Thread
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                ImageView _ivThumbnail = (ImageView) mListContainer.findViewWithTag(id);

                                //ImageView not recycled yet, update
                                if(_ivThumbnail != null){
                                    _ivThumbnail.setVisibility(View.VISIBLE);
                                    _ivThumbnail.setImageBitmap(obj);
                                }
                            } catch (Exception e) {
                               e.printStackTrace();
                            }
                        }
                    });
                    break;
                case NG:
                default:
                    Log.w(TAG, "NotifyCallerByBitmap()," + "No Bitmap data is available" );
                    break;
            }
        }

        @Override
        public void NotifyCallerByByteArray(IConnect.RESULT result, String id, byte[] ba) {
            Log.i(TAG, "NotifyCallerByByteArray()," + "RESULT:" + result + " ID:" + id);
        }
    }

    public class SimpleListAdapter extends BaseAdapter {

        @Override
        public int getCount() {

            try {
                return mContentsCache.rows.size();
            } catch (Exception e) {
                Log.w(TAG, "SimpleListAdapter,getCount()," + "possibility: Remote Json parse error ");
                e.printStackTrace();
                return 0;
            }
        }

        @Override
        public Object getItem(int position) {

            try {
                return mContentsCache.rows.get(position);
            } catch (Exception e) {
                Log.w(TAG, "SimpleListAdapter,getItem()," + "possibility: Remote Json parse error ");
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        //Classical design pattern for listview recycling
        class ViewHolder {
            public TextView _tvTITLE;
            public TextView _tvSUBTITLE;
            public ImageView _ivThumbnail;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder vh = null;
            JsonContentsDataStructure.rows row = null;
            try {
               row = mContentsCache.rows.get(position);
            }catch(Exception e){
                Log.e(TAG, "Data fetch failure in mContentsCache, position:" + position);
            }

            if (convertView == null) {
                vh = new ViewHolder();

                convertView = getLayoutInflater().inflate(R.layout.listview, null);

                vh._tvTITLE = (TextView) convertView.findViewById(R.id.TITLE);
                vh._tvSUBTITLE = (TextView) convertView.findViewById(R.id.SUBTITLE);
                vh._ivThumbnail = (ImageView) convertView.findViewById(R.id.IMAGE);

                convertView.setTag(vh);

            } else {
                vh = (ViewHolder) convertView.getTag();
            }

            //update current View holder.
            //Note: tag in ImageView used for telling current view is recycled or not.
            vh._tvTITLE.setText(row.title);
            vh._tvSUBTITLE.setText(row.description);
            vh._ivThumbnail.setVisibility(View.GONE);
            vh._ivThumbnail.setTag(row.imageHref);

            Bitmap thumbnail = getBitmapByUniqueID(row.imageHref);

            //Found bitmap in local catch, update imageview
            if(thumbnail != null){
                vh._ivThumbnail.setImageBitmap(thumbnail);
                vh._ivThumbnail.setVisibility(View.VISIBLE);
            }

            return convertView;
        }


    }
}
