package com.example.vit_vetal_.webstart;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.vit_vetal_.webstart.ui.activities.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class BackToAppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<String> runningactivities = new ArrayList<String>();

        ActivityManager activityManager = (ActivityManager)context.getSystemService (Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i1 = 0; i1 < services.size(); i1++) {
            runningactivities.add(0,services.get(i1).topActivity.toString());
        }

        if(!isForeground(context, "com.example.vit_vetal_.webstart")) {
            Log.d("myLogs", "START");
            Intent startActivityIntent = new Intent(context, MainActivity.class);
            startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(startActivityIntent);
        } else {
            Log.d("myLogs", "NO START");
        }
    }

    public boolean isForeground(Context context, String myPackage) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}
