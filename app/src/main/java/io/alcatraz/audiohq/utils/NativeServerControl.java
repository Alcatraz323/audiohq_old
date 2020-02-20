package io.alcatraz.audiohq.utils;

import io.alcatraz.audiohq.extended.CompatWithPipeActivity;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;
@Deprecated
public class NativeServerControl {

//    public static void startServer(CompatWithPipeActivity activity) {
//        if(activity.service_type.equals(AudioHqApis.AUDIOHQ_SERVER_NONE)) {
//            activity.toast("No Server Type");
//        }else if(activity.service_type.equals(AudioHqApis.AUDIOHQ_SERVER_TYPE_JAVA)){
//            activity.toast("Err : Attempt to start native server but type is Java");
//        }else {
//            new Thread(() -> {
//                ShellUtils.execCommand("mkdir " + activity.getFilesDir() + "/native_output", false);
//                ShellUtils.execCommand("touch " + activity.getFilesDir() + "/native_output/server.log", false);
//                AudioHqApis.startServer(activity.service_type, activity.getFilesDir() + "/native_output/server.log");
//            }).start();
//        }
//    }
//
//    public static void stopServer(CompatWithPipeActivity activity) {
//        new Thread(() -> {
//            if (!AudioHqApis.isServerRunning()) {
//                activity.runOnUiThread(() -> activity.toast(R.string.service_toast_already_killed));
//            } else {
//                AudioHqApis.killNativeServer();
//            }
//        }).start();
//
//    }
}
