package com.meetup.glasscamp.bluetoothledetector;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;
import com.google.android.glass.widget.Slider;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private final int SCAN_PERIOD = 10000;

    private CardScrollView mCardScroller;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayList<View> mDevicesViews;
    private boolean isAnyDeviceNearby = false;
    private boolean isScanning = false;

    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        mHandler = new Handler(getMainLooper());

        mDevicesViews = new ArrayList<View>();
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                if(!isAnyDeviceNearby) {
                    isAnyDeviceNearby = true;
                    mDevicesViews.remove(0);
                }
                mDevicesViews.add(buildView(device.getName()));
                mCardScroller.getAdapter().notifyDataSetChanged();
            }
        };

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return mDevicesViews.size();
            }

            @Override
            public Object getItem(int position) {
                return mDevicesViews.get(position);
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mDevicesViews.get(position);
            }

            @Override
            public int getPosition(Object item) {
                return mDevicesViews.indexOf(item);
            }
        });
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
            }
        });
        setContentView(mCardScroller);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
        getBluetoothLEDevices();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.scan_now:
                getBluetoothLEDevices();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View buildView(String text) {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);

        card.setText(text);
        return card.getView();
    }

    private void getBluetoothLEDevices() {

        if(isScanning) {
            return;
        }

        mDevicesViews.clear();
        isAnyDeviceNearby = false;
        isScanning = true;

        View loadingView = buildView(getString(R.string.searching));
        Slider.from(loadingView).startIndeterminate();
        mDevicesViews.add(0, loadingView);

        mCardScroller.getAdapter().notifyDataSetChanged();

        mBluetoothAdapter.startLeScan(mLeScanCallback);

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
                isScanning = false;
                if(!isAnyDeviceNearby) {
                    mDevicesViews.clear();
                    mDevicesViews.add(buildView(getString(R.string.no_nearby_devices)));
                    mCardScroller.getAdapter().notifyDataSetChanged();
                }
            }
        }, SCAN_PERIOD);

    }

}
