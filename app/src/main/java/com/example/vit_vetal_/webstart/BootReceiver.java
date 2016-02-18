package com.example.vit_vetal_.webstart;

import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.crashlytics.android.Crashlytics;
import com.example.vit_vetal_.webstart.utilities.Consts;

import java.util.Calendar;

import io.fabric.sdk.android.Fabric;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        Fabric.with(context, new Crashlytics());

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            try {
                context.startActivity(new
                        Intent("com.htc.wifidisplay.CONFIGURE_MODE_NORMAL").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            } catch (Exception e) {
                try {
                    context.startActivity(new
                            Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                } catch (Exception e2) {
                    context.startActivity(new
                            Intent("android.settings.WIFI_DISPLAY_SETTINGS").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }
            }

            SharedPreferences preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

            if (Build.VERSION.SDK_INT >= 21) {
                while(true) {
                    if(checkPermission(context)) {
                        preferences.edit().putBoolean("afterBoot", true).commit();

                        Intent i = new Intent(context, BackToAppReceiver.class);
                        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                        int backTime = preferences.getInt(Consts.PERSIST_TIME_TAG, Consts.DEFAULT_PERSIST_TIME);
                        int autostartTime = preferences.getInt(Consts.AUTOSTART_TIME_TAG, Consts.DEFAULT_AUTOSTART_TIME);
                        AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                        alarmManager.cancel(pendingIntent);
                        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 1000 * autostartTime, backTime * 60 * 1000, pendingIntent);
                        break;
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                Intent i = new Intent(context, BackToAppReceiver.class);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);
                int backTime = preferences.getInt(Consts.PERSIST_TIME_TAG, Consts.DEFAULT_PERSIST_TIME);
                int autostartTime = preferences.getInt(Consts.AUTOSTART_TIME_TAG, Consts.DEFAULT_AUTOSTART_TIME);
                AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
                alarmManager.cancel(pendingIntent);
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 1000 * autostartTime, backTime * 60 * 1000, pendingIntent);

            }
        }
    }

    private boolean checkPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats",
                android.os.Process.myUid(), context.getPackageName());

        return mode == AppOpsManager.MODE_ALLOWED;
    }
}