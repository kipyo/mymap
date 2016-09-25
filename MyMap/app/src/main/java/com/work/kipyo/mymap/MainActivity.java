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
    private final static int LOCATION_PERMISSION = 1;
    private final static int PERMISSION_RESULT = 2;
    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;
    private NMapView mMapView;
    private final static String clientId = "hFzyVZrHJSjZTpDJxdel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MiniView를 다른 앱 화면 위에 띄우기 위한 퍼미션 확인
        if (Build.VERSION.SDK_INT >= 23) {
            boolean canDrawOverlays = Settings.canDrawOverlays(this);
            if (!canDrawOverlays) {

                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, PERMISSION_RESULT);
            }
        }
        //현위치 정보를 가져오기 위한 퍼미션 확인
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION);
            }
        } else {
            initActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case LOCATION_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    initActivity();
                } else {
                    Toast.makeText(this, "이 앱은 위치 정보를 얻기위한 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void initActivity() {
        setContentView(R.layout.activity_main);
        mMainButton = (Button)findViewById(R.id.mainButton);
        mMainButton.setOnClickListener(this);
        mListButton = (Button)findViewById(R.id.listButton);
        mListButton.setOnClickListener(this);
        mMainFragment = new MainFragment();
        mMainFragment.setLocationInter(this);
        mListFragment = new ListFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.fragmentLayout, mMainFragment);
        fragmentTransaction.commit();
        mIsMainShow = true;
        updateButtonStyle();
        //현재 위치좌표 가져오기
        mLocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        // create map view
        mMapView = new NMapView(this);
        // set Client ID for Open MapViewer Library
        mMapView.setClientId(clientId);
        // set data provider listener
        super.setMapDataProviderListener(onDataProviderListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(MiniViewService.UPDATE_ACTION);
        registerReceiver(mBRReceiver, filter);
    }

    private void updateButtonStyle() {
        if (mIsMainShow) {
            findViewById(R.id.mainButtonBG).setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            findViewById(R.id.listButtonBG).setBackgroundColor(getResources().getColor(android.R.color.white));
        } else {
            findViewById(R.id.mainButtonBG).setBackgroundColor(getResources().getColor(android.R.color.white));
            findViewById(R.id.listButtonBG).setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Fragment newFragment = null;
        if (mIsMainShow) {
            if (view.getId() == R.id.listButton) {
                newFragment = mListFragment;
                mIsMainShow = false;
            } else {
                return;
            }
        } else {
            if (view.getId() == R.id.mainButton) {
                newFragment = mMainFragment;
                mIsMainShow = true;
            } else {
                return;
            }
        }
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentLayout, newFragment);
        fragmentTransaction.commit();
        updateButtonStyle();
        if (!mIsMainShow) {
            mLocationManager.removeUpdates(mLocationListener);
        }
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
