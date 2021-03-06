package com.work.kipyo.mymap;

import android.Manifest;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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

    interface LocationInter {
        public abstract void updateLocation();
    }
    private static final String TAG = "MainFragment";
    private TextView mMileageTextView;
    private TextView mKmTextView;
    private TextView mLocationTextView;
    private Button mFunctionButton;
    private boolean mRunningMap = false;
    private int mMileage = 0;
    private int mMeter = 0;
    private LocationInter mLocationInter;
    private final static int START_MAP = 1;
    private final static int STOP_MAP = 1;
    private final static String RUNNINGMAP_STATE = "RunningMap";
    private final static String MILEAGE = "Mileage";
    private final static String METER = "Meter";
    private SharedPreferences mPreference;
    private final static int PERMISSION_RESULT = 2;

    public MainFragment() {
    }

    public void setLocationInter(LocationInter inter) {
        mLocationInter = inter;
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
        mKmTextView = (TextView) rootView.findViewById(R.id.kmText);
        mLocationTextView = (TextView) rootView.findViewById(R.id.locationText);
        mFunctionButton = (Button) rootView.findViewById(R.id.functionButton);
        mFunctionButton.setOnClickListener(this);
        mPreference = getActivity().getSharedPreferences(MiniViewService.MAP_PREFERENCE, Context.MODE_PRIVATE);
        mMileage = mPreference.getInt(MiniViewService.KEY_MILEAGE, 0);
        mMeter = mPreference.getInt(MiniViewService.KEY_METER, 0);
        mRunningMap = mPreference.getBoolean(MiniViewService.KEY_RUNNING, false);
        if (mRunningMap) {
            startMap();
        }
        mMileageTextView.setText(String.valueOf(mMileage));
        mKmTextView.setText(MiniViewService.getMeterText(mMeter));
        return rootView;
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
        insertData();
    }

    private void insertData() {
        Date date = new Date(System.currentTimeMillis());
        String currentDateTimeString = new SimpleDateFormat("yyyy.MM.dd").format(date);
        ContentValues values = new ContentValues();
        values.put(DBConstants.DATE, currentDateTimeString);

        Uri uri = DBConstants.CONTENT_URI;
        ContentResolver cr = getActivity().getContentResolver();
        String[] projection = { DBConstants.ID, DBConstants.DATE, DBConstants.MILEAGE, DBConstants.METER };
        String selection = DBConstants.DATE + "=?";
        Cursor cursor = cr.query(uri, projection, selection, new String[] {currentDateTimeString}, null);
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                cursor.moveToFirst();
                int newMileage = mMileage + cursor.getInt(2);
                int newMeter = mMeter + cursor.getInt(3);
                values.put(DBConstants.MILEAGE, newMileage);
                values.put(DBConstants.METER, newMeter);
                cr.update(uri, values, DBConstants.ID + "=" + cursor.getInt(0), null);
            } else {
                values.put(DBConstants.MILEAGE, mMileage);
                values.put(DBConstants.METER, mMeter);
                cr.insert(uri, values);
            }
            cursor.close();
        } else {
            values.put(DBConstants.MILEAGE, mMileage);
            values.put(DBConstants.METER, mMeter);
            cr.insert(uri, values);
        }
        mMileage = 0;
        mMeter = 0;
        mMileageTextView.setText(String.valueOf(mMileage));
        mMileageTextView.invalidate();
        mKmTextView.setText(MiniViewService.getMeterText(mMeter));
        mKmTextView.invalidate();
        clearPreference();
    }

    private void clearPreference() {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.clear();
        editor.commit();
    }
    private boolean permissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean canDrawOverlays = Settings.canDrawOverlays(getActivity());
            if (!canDrawOverlays) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getActivity().getPackageName()));
                startActivityForResult(intent, PERMISSION_RESULT);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        hiddenMiniView();
        mLocationInter.updateLocation();
    }
    private void hiddenMiniView() {
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
        outState.putInt(METER, mMeter);
    }

    public void updateTextView(Intent intent) {
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
            mLocationInter.updateLocation();
        }
    };

    public void updateLocationText(String location) {
        mLocationTextView.setText(location);
        mLocationTextView.invalidate();
    }
}

