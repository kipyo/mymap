package com.work.kipyo.mymap;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by q on 2016-09-24.
 */

public class MiniViewService extends Service implements View.OnTouchListener, SensorEventListener {
    private View mView;
    private TextView mMileageTextView;
    private TextView mMiniKmText;
    private WindowManager mManager;
    private WindowManager.LayoutParams mParams;
    private float mTouchX, mTouchY;
    private int mViewX, mViewY;
    private boolean isMove = false;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private PrivData mPrivData; //이전 시간의 좌표값을 저장하기 위한 변수
    private int mStep = 0;
    private int mMeter = 0;
    public final static String UPDATE_ACTION = "com.work.kipyo.mymap.UpdateMileage";
    public final static String KEY_RUNNING = "KeyRunning";
    public final static String KEY_MILEAGE = "KeyMileage";
    public final static String KEY_METER = "KeyMeter";
    public final static String SHOW_MINIVIEW = "com.work.kipyo.mymap.ShowMiniView";
    public final static String KEY_SHOW = "KeyShow";
    private SharedPreferences mPreference;
    public final static String MAP_PREFERENCE = "MapPreference";
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.mini_view, null);
        mView.setVisibility(View.INVISIBLE);
        //main 화면의 표시 여부를 확인하기 위한 BR 등록
        IntentFilter filter = new IntentFilter();
        filter.addAction(SHOW_MINIVIEW);
        registerReceiver(mBRReceiver, filter);

        mMileageTextView = (TextView) mView.findViewById(R.id.miniMileageText);
        mMiniKmText = (TextView) mView.findViewById(R.id.miniKmText);
        mView.setOnTouchListener(this);
        mParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.OPAQUE);

        mManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        mManager.addView(mView, mParams);

        //만보기 센서 작동
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mPrivData = new PrivData();
        mStep = 0;
        mMeter = 0;
        mPreference = getSharedPreferences(MAP_PREFERENCE, MODE_PRIVATE);
        mStep = mPreference.getInt(KEY_MILEAGE, 0);
        mMeter = mPreference.getInt(KEY_METER, 0);
        mMileageTextView.setText(String.valueOf(mStep));
        mMiniKmText.setText(getMeterText(mMeter));

        updatePreference();
        unregisterRestartAlarm();
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        if (mSensor != null)
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mView != null) {
            mManager.removeView(mView);
            mView = null;
        }
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }
        registerRestartAlarm();
    }

    private void registerRestartAlarm() {
        Intent intent = new Intent(this, RestartService.class);
        intent.setAction(RestartService.RESTART_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        long firstTime = SystemClock.elapsedRealtime();
        firstTime += 2*1000;
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 10*1000, sender);
    }
    private void unregisterRestartAlarm() {
        Intent intent = new Intent(this, RestartService.class);
        intent.setAction(RestartService.RESTART_ACTION);
        PendingIntent sender = PendingIntent.getBroadcast(this, 0, intent, 0);
        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
        am.cancel(sender);
    }
    private void updatePreference() {
        SharedPreferences.Editor editor = mPreference.edit();
        editor.putBoolean(KEY_RUNNING, true);
        editor.putInt(KEY_MILEAGE, mStep);
        editor.putInt(KEY_METER, mMeter);
        editor.commit();

    }
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isMove = false;
                mTouchX = event.getRawX();
                mTouchY = event.getRawY();
                mViewX = mParams.x;
                mViewY = mParams.y;
                break;

            case MotionEvent.ACTION_UP:
                break;

            case MotionEvent.ACTION_MOVE:
                isMove = true;
                int x = (int) (event.getRawX() - mTouchX);
                int y = (int) (event.getRawY() - mTouchY);
                //이동으로 인정하기 위한 최소한의 범위 지정
                final int num = 5;
                if ((x > -num && x < num) && (y > -num && y < num)) {
                    isMove = false;
                    break;
                }
                /*  x가 (+)이면 Left
                    x가 (-)이면 Right
                    y가 (+)이면 Up
                    y가 (-)이면 Down
                */
                mParams.x = mViewX + x;
                mParams.y = mViewY + y;
                mManager.updateViewLayout(mView, mParams);
                break;
        }
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            long currentTime = System.currentTimeMillis();
            long timeGap = currentTime - mPrivData.time;
            //일정 시간 간격으로 이동을 체크함
            if (timeGap > 100) {
                mPrivData.time = currentTime;

                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];
                float positionDelta = Math.abs(x + y + z - (mPrivData.x + mPrivData.y + mPrivData.z));
                float delta = positionDelta / timeGap * 10000;
                //1보로 인정 가능한 이동 거리
                if (delta > 880) {
                    mStep ++;
                    mMileageTextView.setText(String.valueOf(mStep));
                    mMileageTextView.invalidate();
                    mMeter += (int)(positionDelta / 100);
                    mMiniKmText.setText(getMeterText(mMeter));
                    mMiniKmText.invalidate();
                    updatePreference();
                    sendCurrentData();
                }
                mPrivData.x = x;
                mPrivData.y = y;
                mPrivData.z = z;
            }
        }
    }

    private void sendCurrentData() {
        Intent intent = new Intent();
        intent.setAction(UPDATE_ACTION);
        intent.putExtra(KEY_MILEAGE, mStep);
        intent.putExtra(KEY_METER, mMeter);
        sendBroadcast(intent);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do nothing
    }

    private BroadcastReceiver mBRReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (SHOW_MINIVIEW.equals(intent.getAction())) {
                boolean show = intent.getBooleanExtra(KEY_SHOW, false);
                mView.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
                mView.invalidate();
                if (!show) {
                    sendCurrentData();
                }
            }
        }
    };

    // 1000이 넘어가는 경우 KM 단위로 표시하기 위한 함수
    public static String getMeterText(int meter) {
        StringBuilder sb = new StringBuilder();
        if (meter >= 1000) {
            float flMeter = (float)meter;
            sb.append(String.format("%.2f", flMeter/1000));
            sb.append("km");
        } else {
            sb.append(String.format("%3d", meter));
            sb.append("m");
        }
        return sb.toString();
    }
}

class PrivData {
    long time;
    float x;
    float y;
    float z;
}
