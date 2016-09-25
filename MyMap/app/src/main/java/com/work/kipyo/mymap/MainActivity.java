package com.work.kipyo.mymap;

import android.app.*;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TabLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nhn.android.maps.NMapView;

public class MainActivity extends Activity implements View.OnClickListener {

    private Button mMainButton;
    private Button mListButton;
    private Fragment mMainFragment;
    private Fragment mListFragment;
    private boolean mIsMainShow = false;
    private final static int PERMISSION_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 23) {
            boolean canDrawOverlays = Settings.canDrawOverlays(this);
            if (!canDrawOverlays) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivityForResult(intent, PERMISSION_RESULT);
            }
        }
        initActivity();

    }

    private void initActivity() {
        setContentView(R.layout.activity_main);
        mMainButton = (Button)findViewById(R.id.mainButton);
        mMainButton.setOnClickListener(this);
        mListButton = (Button)findViewById(R.id.listButton);
        mListButton.setOnClickListener(this);
        mMainFragment = new MainFragment();
        mListFragment = new ListFragment();
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.add(R.id.fragmentLayout, mMainFragment);
        fragmentTransaction.commit();
        mIsMainShow = true;
        updateButtonStyle();
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
    }

}
