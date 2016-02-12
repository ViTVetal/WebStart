package com.example.vit_vetal_.webstart;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.net.Uri;
import android.preference.Preference;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.example.vit_vetal_.webstart.ui.activities.MainActivity;
import com.example.vit_vetal_.webstart.utilities.Consts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
            startActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager.getRunningTasks(1);
        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        return componentInfo.getPackageName().equals(myPackage);
    }
}
