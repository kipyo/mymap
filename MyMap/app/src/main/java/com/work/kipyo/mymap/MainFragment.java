package com.work.kipyo.mymap;

import android.Manifest;
import android.app.Fragment;
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
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by kipyo on 2016-09-23.
 */

public class MainFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "MainFragment";
    private TextView mMileageTextView;
    private TextView mKmTextView;
    private TextView mLocationTextView;
    private Button mFunctionButton;
    private boolean mRunningMap = false;
    private int mMileage = 0;
    private float mKilometers = 0;
    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;
    private final static int START_MAP = 1;
    private final static int STOP_MAP = 1;
    private final static String RUNNINGMAP_STATE = "RunningMap";
    private final static String MILEAGE = "Mileage";
    private final static String KILOMETERS = "Kilometers";

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mRunningMap = savedInstanceState.getBoolean(RUNNINGMAP_STATE, false);
            mMileage = savedInstanceState.getInt(MILEAGE, 0);
            mKilometers = savedInstanceState.getFloat(KILOMETERS, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mMileageTextView = (TextView) rootView.findViewById(R.id.mileageText);
        mMileageTextView.setText(String.valueOf(mMileage));
        mKmTextView = (TextView) rootView.findViewById(R.id.kmText);
        mKmTextView.setText(String.format("%.2f", mKilometers));
        mLocationTextView = (TextView) rootView.findViewById(R.id.locationText);
        mFunctionButton = (Button) rootView.findViewById(R.id.functionButton);
        mFunctionButton.setOnClickListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MiniViewService.UPDATE_ACTION);
        getActivity().registerReceiver(mBRReceiver, filter);
        //현재 위치좌표 가져오기
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mLocationListener = new MyLocationListener();
        return rootView;
    }

    private void updateLocationInfo() {
        //GPS_PROVIDER: GPS를 통해 위치를 알려줌
        boolean isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        //NETWORK_PROVIDER: WI-FI 네트워크나 통신사의 기지국 정보를 통해 위치를 알려줌
        boolean isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isGPSEnabled && isNetworkEnabled) {
            //선택된 프로바이더를 사용해 위치정보를 업데이트
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, mLocationListener);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 10, mLocationListener);
            String locationProvider = LocationManager.GPS_PROVIDER;
            Location lastKnownLocation = mLocationManager.getLastKnownLocation(locationProvider);
                if (lastKnownLocation != null) {
                double lng = lastKnownLocation.getLongitude();
                double lat = lastKnownLocation.getLatitude();
                Log.d(TAG, "longtitude=" + lng + ", latitude=" + lat);
            }
        }else{
            Toast.makeText(getActivity(), "GPS 정보를 얻을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onClick(View view) {
        if (mFunctionButton.getId() == view.getId()) {
            if (mRunningMap == false) {
                if (permissionCheck()) {
                    startMap();
                }
            } else {
                if(permissionCheck()) {
                    stopMap();
                }
            }
        } else {
            Log.e(TAG, "Not support view button: id = " + view.getId());
        }
    }

    private void startMap() {
        getActivity().startService(new Intent(getActivity(), MiniViewService.class));
        mRunningMap = true;
        mFunctionButton.setText(getString(R.string.stopButton));
        mFunctionButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
    }
    private void stopMap() {
        getActivity().stopService(new Intent(getActivity(), MiniViewService.class));
        mFunctionButton.setText(getString(R.string.startButton));
        mFunctionButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        mRunningMap = false;
    }

    private boolean permissionCheck() {
        if (Build.VERSION.SDK_INT >= 23) {
            boolean canDrawOverlays = Settings.canDrawOverlays(getActivity());
            if (!canDrawOverlays) {
                Toast.makeText(getActivity(), "이 앱은 다른 화면 위에 그리기 위한 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent();
        intent.setAction(MiniViewService.SHOW_MINIVIEW);
        intent.putExtra(MiniViewService.KEY_SHOW, false);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent();
        intent.setAction(MiniViewService.SHOW_MINIVIEW);
        intent.putExtra(MiniViewService.KEY_SHOW, true);
        getActivity().sendBroadcast(intent);
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RUNNINGMAP_STATE, mRunningMap);
        outState.putInt(MILEAGE, mMileage);
        outState.putFloat(KILOMETERS, mKilometers);
    }

    private BroadcastReceiver mBRReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MiniViewService.UPDATE_ACTION.equals(intent.getAction())) {
                mMileage = intent.getIntExtra(MiniViewService.KEY_MILEAGE, 0);
                mMileageTextView.setText(String.valueOf(mMileage));
                mMileageTextView.invalidate();
                mKilometers = intent.getFloatExtra(MiniViewService.KEY_KILOMETERS, 0);
                mKmTextView.setText(String.format("%.2f", mKilometers));
                mKmTextView.invalidate();
                mFunctionButton.setText(getString(R.string.stopButton));
                mFunctionButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                updateLocationInfo();
            }
        }
    };

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Latitude=" + location.getLatitude() + ", Longtitude=" + location.getLongitude());
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

