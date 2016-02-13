package com.example.vit_vetal_.webstart;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Browser;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.vit_vetal_.webstart.utilities.Consts;
import com.jaredrummler.android.processes.ProcessManager;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class BackToAppReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("myLogs", "onReceive");
        ArrayList<String> runningactivities = new ArrayList<String>();

        ActivityManager activityManager = (ActivityManager)context.getSystemService (Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> services = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i1 = 0; i1 < services.size(); i1++) {
            runningactivities.add(0,services.get(i1).topActivity.toString());
        }

        Intent i = new Intent(context, BackToAppReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
        SharedPreferences preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        int backTime = preferences.getInt(Consts.PERSIST_TIME_TAG, Consts.DEFAULT_PERSIST_TIME);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + backTime * 60 * 1000, backTime * 60 * 1000, pendingIntent);

        String url = preferences.getString(Consts.URL_TAG, context.getResources().getString(R.string.default_url));
        Map<String, String> headers = new HashMap<String, String>();

        try {
            headers.put(Consts.BUILD_DATE_HEADER, getAppBuildDate(context));
        } catch (Exception e) {
            e.printStackTrace();
        }
        headers.put(Consts.PASSWORD_HEADER, preferences.getString(Consts.PASSWORD_TAG, Consts.DEFAULT_PASSWORD));

        if(!isForeground(context, "mobi.mgeek.TunnyBrowser")) {
            String urlString= url.replace("SERIAL", getDeviceId(context));
            Intent startActivityIntent =new Intent(Intent.ACTION_VIEW, Uri.parse(urlString));
            startActivityIntent.setFlags(Intent.FLAG_FROM_BACKGROUND | Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityIntent.putExtra(Browser.EXTRA_APPLICATION_ID, "mobi.mgeek.TunnyBrowser");

            startActivityIntent.setPackage("mobi.mgeek.TunnyBrowser");
            try {
                context.startActivity(startActivityIntent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(context, "Dolphin isn't found",Toast.LENGTH_SHORT).show();
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

    private String getAppBuildDate(Context context) throws Exception {
        ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        ZipFile zf = new ZipFile(ai.sourceDir);
        ZipEntry ze = zf.getEntry("classes.dex");
        long time = ze.getTime();
        return new SimpleDateFormat("yyyyMMdd").format(new java.util.Date(time));
    }

    public boolean isForeground(Context context, String myPackage) {
        ActivityManager manager = (ActivityManager) context.getSystemService(context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            final int PROCESS_STATE_TOP = 2;
            ActivityManager.RunningAppProcessInfo currentInfo = null;
            Field field = null;
            try {
                field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
            } catch (Exception ignored) {
            }
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo app : appList) {
                if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                        && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                    Integer state = null;
                    try {
                        state = field.getInt(app);
                    } catch (Exception e) {
                    }
                    if (state != null && state == PROCESS_STATE_TOP) {
                        currentInfo = app;
                        break;
                    }
                }
            }
            return currentInfo.processName.equals(myPackage);
        } else {
            List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
            ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
            return componentInfo.getPackageName().equals(myPackage);
        }
//        UsageStatsManager usm = (UsageStatsManager)context.getSystemService("usagestats");
//        long time = System.currentTimeMillis();
//        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
//        if (appList != null && appList.size() > 0) {
//            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
//            for (UsageStats usageStats : appList) {
//                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
//            }
//            if (mySortedMap != null && !mySortedMap.isEmpty()) {
//                Log.d("myLogs", mySortedMap.get(mySortedMap.lastKey()).getPackageName());
//            }
//        }
//        if(ProcessManager.getRunningProcesses().get(0).name.equals(myPackage)) {
//            return true;
//        }

      /*  final int PROCESS_STATE_TOP = 2;
        ActivityManager.RunningAppProcessInfo currentInfo = null;
        Field field = null;
        try {
            field = ActivityManager.RunningAppProcessInfo.class.getDeclaredField("processState");
        } catch (Exception ignored) {
        }
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appList = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo app : appList) {
            if (app.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
                    && app.importanceReasonCode == ActivityManager.RunningAppProcessInfo.REASON_UNKNOWN) {
                Integer state = null;
                try {
                    state = field.getInt(app);
                } catch (Exception e) {
                }
                if (state != null && state == PROCESS_STATE_TOP) {
                    currentInfo = app;
                    break;
                }
            }
        }
        Log.d("myLogs", currentInfo.processName);
        return false;*/
    }
}
