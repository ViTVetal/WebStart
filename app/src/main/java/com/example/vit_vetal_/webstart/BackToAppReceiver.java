package com.example.vit_vetal_.webstart;

import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Browser;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.example.vit_vetal_.webstart.utilities.Consts;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import io.fabric.sdk.android.Fabric;

public class BackToAppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Fabric.with(context, new Crashlytics());

        ArrayList<String> runningactivities = new ArrayList<String>();

        ActivityManager activityManager = (ActivityManager)context.getSystemService (Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i1 = 0; i1 < services.size(); i1++) {
            runningactivities.add(0,services.get(i1).topActivity.toString());
        }

        SharedPreferences preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        String url = preferences.getString(Consts.URL_TAG, context.getResources().getString(R.string.default_url));
        if(!isForeground(context, "org.mozilla.firefox")) {
            String urlString= url.replace("SERIAL", getDeviceId(context));
            Intent startActivityIntent =new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            startActivityIntent.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityIntent.putExtra(Browser.EXTRA_APPLICATION_ID, "org.mozilla.firefox");

            startActivityIntent.setPackage("org.mozilla.firefox");
            try {
                context.startActivity(startActivityIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Firefox is not found",Toast.LENGTH_SHORT).show();
            }
        }
    }

    public String getDeviceId(Context context) {
        final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (deviceId != null) {
            return deviceId;
        } else {
            return android.os.Build.SERIAL;
        }
    }

    public boolean isForeground(Context context, String myPackage) {
            ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= 21) {
                String topPackageName = "";
                    UsageStatsManager mUsageStatsManager = (UsageStatsManager)context.getSystemService(Context.USAGE_STATS_SERVICE);
                    long time = System.currentTimeMillis();
                    // We get usage stats for the last 10 seconds
                    List<UsageStats> stats = mUsageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, time);
                    // Sort the stats by the last time used
                    if(stats != null) {
                        SortedMap<Long,UsageStats> mySortedMap = new TreeMap<Long,UsageStats>();
                        for (UsageStats usageStats : stats) {
                            mySortedMap.put(usageStats.getLastTimeUsed(),usageStats);
                        }
                        if(mySortedMap != null && !mySortedMap.isEmpty()) {
                            topPackageName =  mySortedMap.get(mySortedMap.lastKey()).getPackageName();
                        }
                    }
                return topPackageName.equals(myPackage);
            } else {
                List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
                ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
                return componentInfo.getPackageName().equals(myPackage);
            }
    }
}