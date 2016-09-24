package com.work.kipyo.mymap;

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
    public MainFragment() {
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
        return rootView;
    }

    @Override
    public void onClick(View view) {
        if (mFunctionButton.getId() == view.getId()) {
            if (mRunningMap == false) {
                mFunctionButton.setText(getString(R.string.stopButton));
                mFunctionButton.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                //TODO start
                mRunningMap = true;
                Toast.makeText(getContext(), "start", Toast.LENGTH_SHORT).show();
            } else {
                mFunctionButton.setText(getString(R.string.startButton));
                mFunctionButton.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                //TODO stop
                mRunningMap = false;
                Toast.makeText(getContext(), "stop", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "Not support view button: id = " + view.getId());
        }
    }
}
