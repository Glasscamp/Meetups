package com.glasscamp.meetup.liveposition;

import com.google.android.glass.timeline.LiveCard;
import com.google.android.glass.timeline.LiveCard.PublishMode;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.widget.RemoteViews;

import java.io.InputStream;
import java.net.URL;
import java.util.List;


public class LivePositionService extends Service {

    private static final long DELAY_MILLIS = 15000;
    private static final String LIVE_CARD_TAG = "LivePositionService";
    private boolean isStopped = false;

    RemoteViews mRemoteViews;
    LocationManager mLocationManager;
    private double mLat, mLng;
    private LiveCard mLiveCard;
    private final Handler mHandler = new Handler();
    private final Runnable updateCardRunnable = new Runnable() {

        @Override
        public void run() {
            if(isStopped) return;

            getLocation();

            if(mLat > 0 || mLng > 0) {
                new AsyncTask<String, String, Bitmap>() {

                    @Override
                    protected Bitmap doInBackground(String... params) {
                        Bitmap image = null;
                        try {
                            InputStream is = (InputStream) new URL(params[0]).getContent();
                            image = BitmapFactory.decodeStream(is);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return image;
                    }

                    @Override
                    protected void onPostExecute(Bitmap image) {
                        if(isStopped) return;

                        if(image != null && mRemoteViews != null) {
                            mRemoteViews.setImageViewBitmap(R.id.map_view,image);
                            mLiveCard.setViews(mRemoteViews);
                        }

                        mHandler.postDelayed(updateCardRunnable, DELAY_MILLIS);
                    }
                }.execute("http://maps.googleapis.com/maps/api/staticmap?center="
                        +mLat+","+mLng+
                        "&zoom=16&size=640x340&maptype=roadmap&markers=color:red%7Clabel:MyPosition%7C"
                        +mLat+","+mLng+
                        "&sensor=false");
            }
        }

    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mLiveCard == null) {
            mLiveCard = new LiveCard(this, LIVE_CARD_TAG);

            mRemoteViews = new RemoteViews(getPackageName(), R.layout.live_position);
            mLiveCard.setViews(mRemoteViews);

            // Display the options menu when the live card is tapped.
            Intent menuIntent = new Intent(this, LiveCardMenuActivity.class);
            mLiveCard.setAction(PendingIntent.getActivity(this, 0, menuIntent, 0));
            mLiveCard.publish(PublishMode.REVEAL);
        } else {
            mLiveCard.navigate();
        }

        mHandler.post(updateCardRunnable);

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDestroy() {
        if (mLiveCard != null && mLiveCard.isPublished()) {
            mLiveCard.unpublish();
            mLiveCard = null;
        }
        isStopped = true;
        mHandler.removeCallbacks(updateCardRunnable);
        super.onDestroy();
    }

    private void getLocation() {
        List<String> providers = mLocationManager.getProviders(true);
        for (String provider : providers) {
            Location location = mLocationManager.getLastKnownLocation(provider);
            if(location != null) {
                mLat = location.getLatitude();
                mLng = location.getLongitude();
                break;
            }
        }
    }
}
