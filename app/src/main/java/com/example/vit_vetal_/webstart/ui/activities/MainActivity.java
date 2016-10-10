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

import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.InputType;
import android.text.TextUtils;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.vit_vetal_.webstart.BackToAppReceiver;
import com.example.vit_vetal_.webstart.BuildConfig;
import com.example.vit_vetal_.webstart.R;
import com.example.vit_vetal_.webstart.utilities.Consts;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MainActivity extends Activity {
    private Window wind;
    private SharedPreferences preferences;
    private final int MY_PERMISSIONS_REQUEST = 100;
    private WebView webview;
    private float x1,x2;
    static final int MIN_DISTANCE = 250;
    private Map<String, String> headers = new HashMap<String, String>();
    private long startTime;
    MyRunnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("myLogs", "onCreate");
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        startTime = Calendar.getInstance().getTimeInMillis();

       // enableBackToAppFeature();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        webview = new WebView(this);

        setContentView(webview);
        webview.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if (deltaX > MIN_DISTANCE) {
                            showPasswordDialog();
                        }
                        break;
                }
                return false;
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            @Override public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                webview.loadUrl("file:///android_asset/error.html");
            } });
        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        preferences = getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);

        try {
            headers.put(Consts.BUILD_DATE_HEADER, getAppBuildDate());
        } catch (Exception e) {
            e.printStackTrace();
        }
        headers.put(Consts.PASSWORD_HEADER, preferences.getString(Consts.PASSWORD_TAG, Consts.DEFAULT_PASSWORD));

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
        } else {
            final Handler handler = new Handler();
            handler.removeCallbacksAndMessages(null);

            if(runnable != null)
                runnable.killRunnable();

            runnable = new MyRunnable(handler);

            handler.post(runnable);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (webview.canGoBack()) {
                        webview.goBack();
                    } else {
                        finish();
                    }
                    return true;
            }
        }
        return super.onKeyDown(keyCode, event);
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

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void sendVolleyRequestToServer() {
        String urlPattern = preferences.getString(Consts.URL_TAG, getResources().getString(R.string.default_url)) + "/?uid=%1$s&amp;uptime=%2$s";

        long timeFromStart = (Calendar.getInstance().getTimeInMillis() - startTime) / 1000;

        String url = String.format(urlPattern,
                getDeviceId(this),
                timeFromStart);

        Log.d("myLogs", "timeFromStart " + timeFromStart);

        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Log.d("myLogs", response.getString("url"));

                            String url = response.getString("url");

                            if(url != null && !TextUtils.isEmpty(url)) {
                                webview.clearView();
                                webview.loadUrl(url, headers);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("myLogs", error.getMessage());
                        webview.loadUrl("file:///android_asset/error.html");
                    }
                }
        );

        queue.add(getRequest);
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
                    final Handler handler = new Handler();
                    handler.removeCallbacksAndMessages(null);

                    if(runnable != null)
                        runnable.killRunnable();

                    runnable = new MyRunnable(handler);

                    handler.post(runnable);
                }
                return;
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();

        if(runnable != null)
            runnable.killRunnable();
    }

    private void enableBackToAppFeature() {
        Intent intent = new Intent(this, BackToAppReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + 1 * 60 * 1000, 1 * 60 * 1000, pendingIntent);
    }

    public class MyRunnable implements Runnable {
        private boolean killMe = false;
        private Handler handler;

        MyRunnable(Handler handler) {
            this.handler = handler;
        }

        public void run() {
            if(killMe)
                return;

            sendVolleyRequestToServer();

            handler.postDelayed(this, 60000);
        }

        private void killRunnable() {
            killMe = true;
            Log.d("myLogs", killMe + " killMe");
        }
    }
}