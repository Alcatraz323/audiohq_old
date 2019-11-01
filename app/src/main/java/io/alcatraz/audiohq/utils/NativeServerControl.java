package io.alcatraz.audiohq.utils;

import io.alcatraz.audiohq.CompatWithPipeActivity;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.ServerStatus;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;

public class NativeServerControl {

    public static void startServer(CompatWithPipeActivity activity) {
        new Thread(() -> {
            ShellUtils.execCommand("mkdir " + activity.getFilesDir() + "/native_output", false);
            ShellUtils.execCommand("touch " + activity.getFilesDir() + "/native_output/server.log", false);
            AudioHqApis.startServer(activity.service_type, activity.getFilesDir() + "/native_output/server.log");
        }).start();

    }

    public static void stopServer(CompatWithPipeActivity activity) {
        new Thread(() -> {
            if (!AudioHqApis.isServerRunning()) {
                activity.runOnUiThread(() -> activity.toast(R.string.service_toast_already_killed));
            } else {
                AudioHqApis.killNativeServer();
            }
        }).start();

    }
}
