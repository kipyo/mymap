package com.work.kipyo.mymap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by q on 2016-09-25.
 */

public class RestartService extends BroadcastReceiver {
    private static final String TAG = "RestartService";
    public static final String RESTART_ACTION = "com.work.kipyo.mymap.RESTART_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        /* 서비스 죽일때 알람으로 다시 서비스 등록 */
        if(RESTART_ACTION.equals(intent.getAction())){
            Log.d(TAG, "ACTION_RESTART_PERSISTENTSERVICE");
            Intent i = new Intent(context, MiniViewService.class);
            context.startService(i);
        }
    }
}
