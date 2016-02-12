package com.example.vit_vetal_.webstart;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.vit_vetal_.webstart.ui.activities.MainActivity;
import com.example.vit_vetal_.webstart.utilities.Consts;

import java.util.Calendar;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent in = new Intent(context, MainActivity.class);
            in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            in.putExtra("REBOOT", true);
            context.startActivity(in);

            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(2000);
                        Log.d("myLogs", "AFTER2000");
                        try {
                            Log.d("myLogs", "open WiFi display settings in HTC");
                            context.startActivity(new
                                    Intent("com.htc.wifidisplay.CONFIGURE_MODE_NORMAL").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        } catch (Exception e) {
                            try {
                                Log.d("myLogs", "open WiFi display settings in Samsung");
                                context.startActivity(new
                                        Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            } catch (Exception e2) {
                                Log.d("myLogs", "open WiFi display settings in stock Android");
                                context.startActivity(new
                                        Intent("android.settings.WIFI_DISPLAY_SETTINGS").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();


            Intent i = new Intent(context, BackToAppReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, i, 0);
            SharedPreferences preferences = context.getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
            int backTime = preferences.getInt(Consts.PERSIST_TIME_TAG, Consts.DEFAULT_PERSIST_TIME);
            int autostartTime = preferences.getInt(Consts.AUTOSTART_TIME_TAG, Consts.DEFAULT_AUTOSTART_TIME);
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(context.ALARM_SERVICE);
            alarmManager.cancel(pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 1000 * autostartTime, backTime * 60 * 1000, pendingIntent);
        }
    }
}