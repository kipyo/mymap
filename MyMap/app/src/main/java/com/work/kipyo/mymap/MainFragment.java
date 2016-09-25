package com.work.kipyo.mymap;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.icu.text.DateFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    private int mMeter = 0;
    private LocationManager mLocationManager;
    private MyLocationListener mLocationListener;
    private final static int START_MAP = 1;
    private final static int STOP_MAP = 1;
    private final static String RUNNINGMAP_STATE = "RunningMap";
    private final static String MILEAGE = "Mileage";
    private final static String METER = "Meter";

    public MainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mRunningMap = savedInstanceState.getBoolean(RUNNINGMAP_STATE, false);
            mMileage = savedInstanceState.getInt(MILEAGE, 0);
            mMeter = savedInstanceState.getInt(METER, 0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mMileageTextView = (TextView) rootView.findViewById(R.id.mileageText);
        mMileageTextView.setText(String.valueOf(mMileage));
        mKmTextView = (TextView) rootView.findViewById(R.id.kmText);
        mKmTextView.setText(MiniViewService.getMeterText(mMeter));
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

    //TODO
    /*
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
    */
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
        insertData();
    }

    //for test
    int date = 1;
    int dateCount = 0;

    private void insertData() {
        //for test block start
        /*---------- start ---------------*/
        //Date date = new Date(System.currentTimeMillis());
        //String currentDateTimeString = new SimpleDateFormat("yyyy.MM.dd").format(date);
        StringBuilder sb = new StringBuilder();
        sb.append("2016.08.");
        if (dateCount > 3) {
            dateCount = 0;
            date++;
        }
        dateCount++;
        sb.append(String.format("%2d", date));
        String currentDateTimeString = sb.toString();
        /*---------- end ---------------*/
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.DATE_FIELD, currentDateTimeString);

        Uri uri = Uri.parse("content://" + MapProvider.MAP_URI);
        ContentResolver cr = getActivity().getContentResolver();
        String[] projection = { DatabaseHelper.ID, DatabaseHelper.DATE_FIELD, DatabaseHelper.MILEAGE_FIELD, DatabaseHelper.METER_FIELD };
        String selection = DatabaseHelper.DATE_FIELD + "=?";
        Cursor cursor = cr.query(uri, projection, selection, new String[] {currentDateTimeString}, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int newMileage = mMileage + cursor.getInt(2);
                int newMeter = mMeter + cursor.getInt(3);
                values.put(DatabaseHelper.MILEAGE_FIELD, newMileage);
                values.put(DatabaseHelper.METER_FIELD, newMeter);
                cr.update(uri, values, DatabaseHelper.ID + "=" + cursor.getInt(0), null);
            } else {
                values.put(DatabaseHelper.MILEAGE_FIELD, mMileage);
                values.put(DatabaseHelper.METER_FIELD, mMeter);
                cr.insert(uri, values);
            }
            cursor.close();
        } else {
            values.put(DatabaseHelper.MILEAGE_FIELD, mMileage);
            values.put(DatabaseHelper.METER_FIELD, mMeter);
            cr.insert(uri, values);
        }
        //test
        Cursor c = cr.query(uri, projection, selection, new String[] {currentDateTimeString}, null);
        if (c != null && c.getCount() > 0) {
            c.moveToFirst();
            int mil = c.getInt(2);
            int met = c.getInt(3);
            Log.d(TAG, mil + " and " + met);
            c.close();
        }
        mMileage = 0;
        mMeter = 0;
        mMileageTextView.setText(String.valueOf(mMileage));
        mMileageTextView.invalidate();
        mKmTextView.setText(String.valueOf(mMeter));
        mKmTextView.invalidate();
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(RUNNINGMAP_STATE, mRunningMap);
        outState.putInt(MILEAGE, mMileage);
        outState.putFloat(METER, mMeter);
    }

    private BroadcastReceiver mBRReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (MiniViewService.UPDATE_ACTION.equals(intent.getAction())) {
                mMileage = intent.getIntExtra(MiniViewService.KEY_MILEAGE, 0);
                mMileageTextView.setText(String.valueOf(mMileage));
                mMileageTextView.invalidate();
                mMeter = intent.getIntExtra(MiniViewService.KEY_METER, 0);
                mKmTextView.setText(MiniViewService.getMeterText(mMeter));
                mKmTextView.invalidate();
                mFunctionButton.setText(getString(R.string.stopButton));
                mFunctionButton.setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
                mRunningMap = true;
                //TODO
//                updateLocationInfo();
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

