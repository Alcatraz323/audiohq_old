package io.alcatraz.audiohq.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class AHQProtector extends Service {
    Process process;
    DataOutputStream os;
    BufferedReader successResult;
    boolean cir_running = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        cir_running = false;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        new Thread(() -> {
            try {
                process = Runtime.getRuntime().exec("su");
                os = new DataOutputStream(process.getOutputStream());
                successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                while (cir_running) {
                    os.write("audiohq --service".getBytes());
                    os.writeBytes("\n");
                    os.flush();
                    String s;
                    StringBuilder result = new StringBuilder();
                    while ((s = successResult.readLine()) != null) {
                        result.append(s);
                        result.append("\n");
                        break;
                    }
                    if (result.toString().contains("AudioHQ native service is running")) {
                        Log.i("AlcAhqApk", "[Protector]AudioHQ native service is running");
                    } else {
                        Log.e("AlcAhqApk", "[Protector]AudioHQ native service offline, restarting");
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }).start();

    }
}
