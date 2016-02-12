package com.example.vit_vetal_.webstart.ui.activities;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vit_vetal_.webstart.BackToAppReceiver;
import com.example.vit_vetal_.webstart.BuildConfig;
import com.example.vit_vetal_.webstart.R;
import com.example.vit_vetal_.webstart.utilities.Consts;

public class SettingsActivity extends Activity {
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);

        preferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
    }

    public void onClickSERIAL(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.change_serial));

        final EditText input = new EditText(this);

        String url = preferences.getString(Consts.URL_TAG, getResources().getString(R.string.default_url));
        input.setText(url);
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newUrl = input.getText().toString();
                preferences.edit().putString(Consts.URL_TAG, newUrl).commit();

                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.new_url) + newUrl, Toast.LENGTH_SHORT);
                toast.show();

                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                finish();
                startActivity(i);
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

    public void onClickPassword(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.change_password));

        final EditText input = new EditText(this);

        String prePassword = preferences.getString(Consts.PASSWORD_TAG, Consts.DEFAULT_PASSWORD);
        input.setText(prePassword);
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newPassword = input.getText().toString();
                preferences.edit().putString(Consts.PASSWORD_TAG, newPassword).commit();

                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.new_password) + newPassword, Toast.LENGTH_SHORT);
                toast.show();

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

    public void onClickPersist(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.change_persist_time));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        int prePersistTime = preferences.getInt(Consts.PERSIST_TIME_TAG, Consts.DEFAULT_PERSIST_TIME);
        input.setText(String.valueOf(prePersistTime));
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int newTime = Integer.valueOf(input.getText().toString());
                preferences.edit().putInt(Consts.PERSIST_TIME_TAG, newTime).commit();

                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.new_persist_time) + newTime, Toast.LENGTH_SHORT);
                toast.show();

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

    public void onClickAutostart(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.change_autostart_time));

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        int preAutostartTime = preferences.getInt(Consts.AUTOSTART_TIME_TAG, Consts.DEFAULT_AUTOSTART_TIME);
        input.setText(String.valueOf(preAutostartTime));
        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int newTime = Integer.valueOf(input.getText().toString());
                preferences.edit().putInt(Consts.AUTOSTART_TIME_TAG, newTime).commit();

                Toast toast = Toast.makeText(getApplicationContext(),
                        getResources().getString(R.string.new_autostart_time) + newTime, Toast.LENGTH_SHORT);
                toast.show();

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

    public void onClickExit(View v) {
        Intent intent = new Intent(this, BackToAppReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        this.finishAffinity();
        finish();
        System.exit(0);
    }
}