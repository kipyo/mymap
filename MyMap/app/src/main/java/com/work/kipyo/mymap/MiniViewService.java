package com.work.kipyo.mymap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.support.annotation.Nullable;
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
    private float mKilometers = 0;
    public final static String UPDATE_ACTION = "com.work.kipyo.mymap.UpdateMileage";
    public final static String KEY_MILEAGE = "KeyMileage";
    public final static String KEY_KILOMETERS = "KeyKilometers";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LayoutInflater mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mView = mInflater.inflate(R.layout.mini_view, null);
        mMileageTextView = (TextView) mView.findViewById(R.id.miniMileageText);
        mMileageTextView.setText(String.valueOf(mStep));
        mMiniKmText = (TextView) mView.findViewById(R.id.miniKmText);
        mMiniKmText.setText(String.valueOf(mKilometers));
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
        mKilometers = 0;
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
                if (!isMove) {
                    Toast.makeText(this, "Touch Up", Toast.LENGTH_SHORT).show();
                }
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
                if (delta > 800) {
                    mStep ++;
                    mMileageTextView.setText(String.valueOf(mStep));
                    mMileageTextView.invalidate();
                    mKilometers += positionDelta / 10000;
                    mMiniKmText.setText(String.format("%.2f", mKilometers));
                    mMiniKmText.invalidate();
                    Intent intent = new Intent();
                    intent.setAction(UPDATE_ACTION);
                    intent.putExtra(KEY_MILEAGE, mStep);
                    intent.putExtra(KEY_KILOMETERS, mKilometers);
                    sendBroadcast(intent);
                }
                mPrivData.x = x;
                mPrivData.y = y;
                mPrivData.z = z;
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Do nothing
    }
}

class PrivData {
    long time;
    float x;
    float y;
    float z;
}
