package io.alcatraz.audiohq.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.ServerStatus;
import io.alcatraz.audiohq.core.utils.ShellUtils;

public class InstallUtils {
    public static String COPY_FILE_INTERMIDIATES_DIRECTORY;
    public static String LIB_NAME = "libaudioflinger.so";
    public static String ELF_NAME = "audiohq";
    public static String LIB64_NAME = "libaudioflinger64.so";
    public static String ELF64_NAME = "audiohq64";

    public static int install(Context context, boolean need64, boolean modifyrc) {
        String[] test_commands = {"mount -o remount,rw /system", "touch /system/test_audiohq"};

        ShellUtils.CommandResult try_rw = ShellUtils.execCommand(test_commands, true, true);
        if (try_rw.errorMsg.length() > 2) {

            return -2;
        } else {
            ShellUtils.execCommand("rm -rf /system/test_audiohq", true);
        }
        String backup_cmds[] = new String[]{"mkdir /sdcard/audiohq_backups",
                "mkdir /sdcard/audiohq_backups/lib",
                "",
                "cp /system/lib/libaudioflinger.so /sdcard/audiohq_backups/lib",
                "",
                "cp /system/etc/init/audioserver.rc /sdcard/audiohq_backups"};

        if (need64) {
            backup_cmds[2] = "mkdir /sdcard/audiohq_backups/lib64";
            backup_cmds[4] = "cp /system/lib64/libaudioflinger.so /sdcard/audiohq_backups/lib64";
        }

        ShellUtils.execCommand(backup_cmds, true, true);

        COPY_FILE_INTERMIDIATES_DIRECTORY = context.getFilesDir() + "/intermediates";
        new File(COPY_FILE_INTERMIDIATES_DIRECTORY).mkdirs();
        if (!need64) {
            Utils.copyAssetsFile(context, ELF_NAME, COPY_FILE_INTERMIDIATES_DIRECTORY);
        } else {
            Utils.copyAssetsFile(context, ELF64_NAME, COPY_FILE_INTERMIDIATES_DIRECTORY);
            Utils.copyAssetsFile(context, LIB64_NAME, COPY_FILE_INTERMIDIATES_DIRECTORY);
        }
        Utils.copyAssetsFile(context, LIB_NAME, COPY_FILE_INTERMIDIATES_DIRECTORY);

        List<String> install_cmds = new LinkedList<>();
        install_cmds.add("setenforce 0");
        install_cmds.add("mount -o remount,rw /system");
        if (!need64) {
            install_cmds.add("mv " + COPY_FILE_INTERMIDIATES_DIRECTORY + "/" + ELF_NAME + " /system/bin");
        } else {
            install_cmds.add("mv " + COPY_FILE_INTERMIDIATES_DIRECTORY + "/" + ELF64_NAME + " /system/bin/" + ELF_NAME);
            install_cmds.add("mv " + COPY_FILE_INTERMIDIATES_DIRECTORY + "/" + LIB64_NAME + " /system/lib/" + LIB_NAME);
            install_cmds.add("chmod 0644 /system/lib/" + LIB_NAME);
        }
        install_cmds.add("chmod 0755 /system/bin/" + ELF_NAME);

        install_cmds.add("mv " + COPY_FILE_INTERMIDIATES_DIRECTORY + "/" + LIB_NAME + " /system/lib");
        install_cmds.add("mount -o remount,ro /system");

        if (modifyrc) {
            modifyRCFile(true, new AsyncInterface<ShellUtils.CommandResult>() {
                @Override
                public boolean onAyncDone(@Nullable ShellUtils.CommandResult val) {
                    return true;
                }

                @Override
                public void onFailure(String reason) {

                }
            });
        }

        ShellUtils.CommandResult result = ShellUtils.execCommand(install_cmds, true, true);
        if (result.errorMsg.length() >= 2) {
            showRetryDialog(context, result.errorMsg, need64);
        } else {
            ShellUtils.execCommand("reboot", true, false);
        }
        return 0;
    }

    public static void checkAndShowInstallation(Activity activity) {
        ServerStatus.updateStatus();
        ServerStatus.requestForPending(() -> activity.runOnUiThread(() -> {
            if (!ServerStatus.isServerInstalled())
                Panels.getNotInstalledPanel(activity).show();
        }));
    }

    public static ShellUtils.CommandResult modifyRCFile(boolean readproc, AsyncInterface<ShellUtils.CommandResult> beforeReboot) {
        ShellUtils.CommandResult original = ShellUtils.execCommand("cat /system/etc/init/audioserver.rc", false);
        String modify = original.responseMsg;
//        modify = modify.replace("user audioserver","#user audioserver\n    seclabel u:r:magisk:s0");
//        modify = modify.replace("group","#group");
        if (readproc) {
            if (!modify.contains("readproc"))
                modify = modify.replace("group", "group readproc");
        } else {
            modify = modify.replace(" readproc", "");
        }
        String write_cmds[] = {"mount -o remount,rw /system",
                "echo \"" + modify + "\" > /system/etc/init/audioserver.rc",
                "mount -o remount,ro /system"};

        ShellUtils.CommandResult result = ShellUtils.execCommand(write_cmds, true, true);
        if(beforeReboot.onAyncDone(result)){
            ShellUtils.execCommand("reboot",true);
        }
        return result;
    }

    private static void showRetryDialog(Context context, String exc, boolean need64) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.install_confirm_title)
                .setMessage(exc + "\n" + context.getResources().getString(R.string.install_fail_message))
                .setNegativeButton(R.string.ad_nb, null)
                .setPositiveButton(R.string.install_retry, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        install(context, need64, true);
                    }
                }).show();
    }


}
