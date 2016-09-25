package com.work.kipyo.mymap;

import android.Manifest;
import android.app.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nhn.android.maps.NMapActivity;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.maps.nmapmodel.NMapPlacemark;

public class MainActivity extends NMapActivity implements View.OnClickListener, MainFragment.LocationInter {

    private static final String TAG = "MainActivity";
    private Button mMainButton;
    private Button mListButton;
    private MainFragment mMainFragment;
    private ListFragment mListFragment;
    private boolean mIsMainShow = false;
    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;
    private NMapView mMapView;
    private final static String clientId = "hFzyVZrHJSjZTpDJxdel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMainButton = (Button)findViewById(R.id.mainButton);
        mMainButton.setOnClickListener(this);
        mListButton = (Button)findViewById(R.id.listButton);
        mListButton.setOnClickListener(this);
        mMainFragment = new MainFragment();
        mMainFragment.setLocationInter(this);
        mListFragment = new ListFragment();
        replaceFragment(mMainFragment);
        //현재 위치좌표 가져오기
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        // create map view
        mMapView = new NMapView(this);
        // set Client ID for Open MapViewer Library
        mMapView.setClientId(clientId);
        // set data provider listener
        super.setMapDataProviderListener(onDataProviderListener);
        // 이동에 따른 이벤트를 받는 리시버 등록
        registUpdateReceiver();
    }

    private void registUpdateReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(MiniViewService.UPDATE_ACTION);
        registerReceiver(mBRReceiver, filter);
    }
    private void updateButtonStyle() {
        int activeColor = getResources().getColor(android.R.color.holo_red_light);
        int inactiveColor = getResources().getColor(android.R.color.white);
        findViewById(R.id.mainButtonBG).setBackgroundColor(mIsMainShow ? activeColor : inactiveColor);
        findViewById(R.id.listButtonBG).setBackgroundColor(!mIsMainShow ? activeColor : inactiveColor);
    }

    @Override
    public void onClick(View view) {
        Fragment newFragment = null;
        //fragmewnt가 현재와 다른 경우에만 replace하도록 함
        if (mIsMainShow && view.getId() == R.id.listButton) {
            newFragment = mListFragment;
        } else if (!mIsMainShow && view.getId() == R.id.mainButton) {
            newFragment = mMainFragment;
        } else {
            return;
        }
        replaceFragment(newFragment);
        if (!mIsMainShow) {
            mLocationManager.removeUpdates(mLocationListener);
        }
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentLayout, fragment);
        fragmentTransaction.commit();
        if (fragment instanceof MainFragment) {
            mIsMainShow = true;
        } else {
            mIsMainShow = false;
        }
        updateButtonStyle();
    }
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent();
        intent.setAction(MiniViewService.SHOW_MINIVIEW);
        intent.putExtra(MiniViewService.KEY_SHOW, false);
        sendBroadcast(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent();
        intent.setAction(MiniViewService.SHOW_MINIVIEW);
        intent.putExtra(MiniViewService.KEY_SHOW, true);
        sendBroadcast(intent);
        mLocationManager.removeUpdates(mLocationListener);
    }

    private BroadcastReceiver mBRReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mIsMainShow && MiniViewService.UPDATE_ACTION.equals(intent.getAction())) {
                mMainFragment.updateTextView(intent);
            }
        }
    };
    /* NMapDataProvider Listener */
    private final OnDataProviderListener onDataProviderListener = new OnDataProviderListener() {
        @Override
        public void onReverseGeocoderResponse(NMapPlacemark nMapPlacemark, NMapError nMapError) {
            if (nMapError != null) {
                Log.e(TAG, "Failed to findPlacemarkAtLocation: error=" + nMapError.toString());
                return;
            }
            Log.i(TAG, "onReverseGeocoderResponse: placeMark=" + nMapPlacemark.toString());
            if (mIsMainShow) {
                mMainFragment.updateLocationText(nMapPlacemark.toString());
            }
        }
    };

    @Override
    public void updateLocation() {
        //GPS_PROVIDER: GPS를 통해 위치를 알려줌
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //NETWORK_PROVIDER: WI-FI 네트워크나 통신사의 기지국 정보를 통해 위치를 알려줌
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled && isNetworkEnabled) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, mLocationListener);
            String locationProvider = LocationManager.GPS_PROVIDER;
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(locationProvider);
            if (lastKnownLocation != null) {
                double lng = lastKnownLocation.getLongitude();
                double lat = lastKnownLocation.getLatitude();
                findPlacemarkAtLocation(lng, lat);
                Log.i(TAG, "lastKnownLocation Longtitude=" + lng + ", Latitude=" + lat);
            }
        }else{
            Toast.makeText(this, "GPS 정보를 얻을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.i(TAG, "Latitude=" + location.getLatitude() + ", Longtitude=" + location.getLongitude());
            findPlacemarkAtLocation(location.getLongitude(), location.getLatitude());
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            //Do nothing
        }

        @Override
        public void onProviderEnabled(String provider) {
            //Do nothing
        }

        @Override
        public void onProviderDisabled(String provider) {
            //Do nothing
        }
    }
}
