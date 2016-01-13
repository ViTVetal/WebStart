package com.example.vit_vetal_.webstart.ui.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

    public void onClickExit(View v) {
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(intent);

        finish();
    }
}