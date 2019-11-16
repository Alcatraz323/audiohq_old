package io.alcatraz.audiohq.beans;

import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;

public class ServerStatus {
    private static boolean serverRunning = false;
    private static volatile boolean updatePending = false;
    private static boolean serverInstalled = true;

    public static void setServerRunning(boolean running) {
        serverRunning = running;
    }

    public static boolean isServerRunning() {
        return serverRunning;
    }

    public static void setUpdatePending(boolean pending) {
        updatePending = pending;
    }

    public static synchronized void requestForPending(PendingInterface pendingInterface) {
        new Thread(() -> {
            while (updatePending) {
            }
            pendingInterface.onComplete();
        }).start();
    }

    public static void updateStatus() {
        setUpdatePending(true);
        ShellUtils.CommandResult result = AudioHqApis.getRunningServerType();
        if (result.errorMsg == null || result.errorMsg.contains("not found"))
            serverInstalled = false;

        String lib_info = AudioHqApis.getAudioFlingerInfo().responseMsg;
        if (lib_info == null || lib_info.length() < 20)
            serverInstalled = false;

        if(result.errorMsg == null){
            serverRunning =false;
            setUpdatePending(false);
            return;
        }
        serverRunning = result.errorMsg.length() < 1 && !result.responseMsg.contains("running");
        setUpdatePending(false);
    }

    public static boolean isServerInstalled() {
        return serverInstalled;
    }

    public static void setServerInstalled(boolean serverInstalled) {
        ServerStatus.serverInstalled = serverInstalled;
    }

    public interface PendingInterface {
        void onComplete();
    }
}
