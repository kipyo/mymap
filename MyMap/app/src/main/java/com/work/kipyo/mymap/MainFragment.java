package com.work.kipyo.mymap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
    public MainFragment() {
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
        getContext().registerReceiver(mBRReceiver, filter);
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (mFunctionButton.getId() == view.getId()) {
            if (mRunningMap == false) {
                mFunctionButton.setText(getString(R.string.stopButton));
                mFunctionButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                getContext().startService(new Intent(getContext(), MiniViewService.class));
                mRunningMap = true;
            } else {
                mFunctionButton.setText(getString(R.string.startButton));
                mFunctionButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                getContext().stopService(new Intent(getContext(), MiniViewService.class));
                mRunningMap = false;
            }
        } else {
            Log.e(TAG, "Not support view button: id = " + view.getId());
        }
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
            }
        }
    };
}

