package io.alcatraz.audiohq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.io.File;

import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.utils.SharedPreferenceUtil;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferenceUtil spf = SharedPreferenceUtil.getInstance();
        String service_type = (String) spf.get(context, Constants.PREF_SERVICE_TYPE, Constants.DEFAULT_VALUE_PREF_SERVICE);
        boolean boot = (boolean) spf.get(context, Constants.PREF_BOOT, Constants.DEFAULT_VALUE_PREF_BOOT);
        if(boot) {
            File files_dir = context.getFilesDir();
            new Thread(() -> {
                ShellUtils.execCommand("mkdir " + files_dir + "/native_output", false);
                ShellUtils.execCommand("touch " + files_dir + "/native_output/server.log", false);
                AudioHqApis.startServer(service_type, files_dir + "/native_output/server.log");
            }).start();
        }
    }
}
