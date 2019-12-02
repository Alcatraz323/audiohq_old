package io.alcatraz.audiohq.extended;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.alcatraz.support.v4.appcompat.StatusBarUtil;

import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.utils.PermissionInterface;
import io.alcatraz.audiohq.utils.SharedPreferenceUtil;

@SuppressLint("Registered")
public class CompatWithPipeActivity extends AppCompatActivity {
    PermissionInterface pi;
    UpdatePreferenceReceiver updatePreferenceReceiver;

    int requestQueue = 0;

    //=========PREFERENCES==============
    public boolean modified_rc;
    public String service_type;
    public boolean default_silent;
    public boolean boot;
    public boolean pid_mode;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (pi != null && requestCode == requestQueue - 1) {
            pi.onResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadPrefernce();
        registReceivers();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionWithCallback(PermissionInterface pi, String[] permissions, int requestCode) {
        this.pi = pi;
        requestPermissions(permissions, requestCode);
    }

    public void requestPermissionWithCallback(PermissionInterface pi, String[] permissions) {
        requestPermissionWithCallback(pi, permissions, requestQueue);
        requestQueue++;
    }

    public void onReloadPreferenceDone(){}

    public void loadPrefernce() {
        SharedPreferenceUtil spf = SharedPreferenceUtil.getInstance();
        service_type = (String) spf.get(this, Constants.PREF_SERVICE_TYPE, Constants.DEFAULT_VALUE_PREF_SERVICE);
        boot = (boolean) spf.get(this, Constants.PREF_BOOT, Constants.DEFAULT_VALUE_PREF_BOOT);
        modified_rc = (boolean) spf.get(this, Constants.PREF_MODIFY_RC, Constants.DEFAULT_VALUE_PREF_MODIFY_RC);
        default_silent = (boolean) spf.get(this, Constants.PREF_DEFAULT_SILENT, Constants.DEFAULT_VALUE_PREF_DEFAULT_SILENT);
        pid_mode = (boolean) spf.get(this,Constants.PREF_PID_MODE,Constants.DEFAULT_VALUE_PREF_PID_MODE);
    }

    public void registReceivers() {
        IntentFilter ifil = new IntentFilter();
        ifil.addAction(Constants.BROADCAST_ACTION_UPDATE_PREFERENCES);
        updatePreferenceReceiver = new UpdatePreferenceReceiver();
        registerReceiver(updatePreferenceReceiver, ifil);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(updatePreferenceReceiver);
        super.onDestroy();
    }

    public void threadSleep(){
        try {
            Thread.sleep(500L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void setupStaticColorPadding(int color) {
        StatusBarUtil.setColor(this, color);
    }

    public void toast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    public void toast(int resId) {
        Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
    }

    class UpdatePreferenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            loadPrefernce();
            onReloadPreferenceDone();
        }
    }
}
