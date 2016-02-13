package com.example.vit_vetal_.webstart.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vit_vetal_.webstart.BackToAppReceiver;
import com.example.vit_vetal_.webstart.BuildConfig;
import com.example.vit_vetal_.webstart.R;
import com.example.vit_vetal_.webstart.utilities.Consts;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class MainActivity extends Activity {
    private Window wind;
    private SharedPreferences preferences;
    private final int MY_PERMISSIONS_REQUEST = 100;

    @InjectView(R.id.etPasscode)
    EditText etPasscode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.inject(this);

     /*   try {
            Log.d("TAG", "open WiFi display settings in HTC");
            startActivity(new
                    Intent("com.htc.wifidisplay.CONFIGURE_MODE_NORMAL"));
        } catch (Exception e) {
            try {
                Log.d("TAG", "open WiFi display settings in Samsung");
                startActivity(new
                        Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"));
            } catch (Exception e2) {
                Log.d("TAG", "open WiFi display settings in stock Android");
                startActivity(new
                        Intent("android.settings.WIFI_DISPLAY_SETTINGS"));
            }
        }*/

        preferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if(getIntent().getBooleanExtra("REBOOT", false)) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(1000);
                        Log.d("myLogs", "FINISH");
                        finish();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };

            thread.start();
        } else
            enableBackToAppFeature();
        //only for Android 6
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        MY_PERMISSIONS_REQUEST);
            }
        }
    }

    public void onClickOk(View v) {
        String enteredPassword = etPasscode.getText().toString();
        String password = preferences.getString(Consts.PASSWORD_TAG, Consts.DEFAULT_PASSWORD);
        etPasscode.setText("");
        if(enteredPassword.equals(password)) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);
        } else {
            Toast toast = Toast.makeText(getApplicationContext(),
                    getResources().getString(R.string.incorrect_password), Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void onClickExit(View view) {
        finish();
    }

    private String getAppBuildDate() throws Exception {
        ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
        ZipFile zf = new ZipFile(ai.sourceDir);
        ZipEntry ze = zf.getEntry("classes.dex");
        long time = ze.getTime();
        return new SimpleDateFormat("yyyyMMdd").format(new java.util.Date(time));
    }

    @Override
    protected void onResume() {
        super.onResume();

        wind = this.getWindow();
        wind.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        wind.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        wind.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        Log.d("myLogs", "UNLOCK");
    }

    public void onClickLaunch(View v) {
        Intent intent = new Intent(this, BackToAppReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        int backTime = preferences.getInt(Consts.PERSIST_TIME_TAG, Consts.DEFAULT_PERSIST_TIME);
        Log.d("myLogs", backTime + " BACK TIME");
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), backTime * 60 * 1000, pendingIntent);
    }

    public String getDeviceId(Context context) {
        final String deviceId = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        if (deviceId != null) {
            return deviceId;
        } else {
            return android.os.Build.SERIAL;
        }
    }

    private void showPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.enter_password));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT |
                InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String enteredPassword = input.getText().toString();
                String password = preferences.getString(Consts.PASSWORD_TAG, Consts.DEFAULT_PASSWORD);

                if(enteredPassword.equals(password)) {
                    Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(intent);
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.incorrect_password), Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    //only for Android 6
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                return;
            }
        }
    }

    public void onClickTV(View v) {
        try {
            Log.d("TAG", "open WiFi display settings in HTC");
            startActivity(new
                    Intent("com.htc.wifidisplay.CONFIGURE_MODE_NORMAL"));
        } catch (Exception e) {
            try {
                Log.d("TAG", "open WiFi display settings in Samsung");
                startActivity(new
                        Intent("com.samsung.wfd.LAUNCH_WFD_PICKER_DLG"));
            } catch (Exception e2) {
                Log.d("TAG", "open WiFi display settings in stock Android");
                startActivity(new
                        Intent("android.settings.WIFI_DISPLAY_SETTINGS"));
            }
        }
    }

    private void enableBackToAppFeature() {
        Intent intent = new Intent(this, BackToAppReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        int backTime = preferences.getInt(Consts.PERSIST_TIME_TAG, Consts.DEFAULT_PERSIST_TIME);
        Log.d("myLogs", backTime + " BACK TIME");
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + backTime * 60 * 1000, backTime * 60 * 1000, pendingIntent);
    }
}