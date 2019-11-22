package io.alcatraz.audiohq.core.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.utils.Utils;

public class CheckUtils {
    public static final String SELINUX_DISABLED = "Disabled";
    public static final String SELINUX_ENFORCING = "Enforcing";
    public static final String SELINUX_PERMISSIVE = "Permissive";

    public static String getSeLinuxEnforce() {
        return ShellUtils.execCommand("getenforce", true).responseMsg;
    }

    public static String getAudioServerInfo() {
        return ShellUtils.execCommand("file /system/bin/audioserver", true).responseMsg;
    }

    public static boolean getRootStatus() {
        return ShellUtils.hasRootPermission();
    }

    public static int getSDK() {
        return Build.VERSION.SDK_INT;
    }

    public static String[] getSupportArch() {
        return Build.SUPPORTED_ABIS;
    }

    public static boolean getIfSupported() {
        int[] Apis = Constants.SUPPORT_APIS;
        String[] audiohq_abis = Constants.SUPPORT_ABIS;
        String[] device_abis = getSupportArch();
        for (int i : Apis) {
            if (i == getSDK()) {
                for (String j : audiohq_abis) {
                    for (String k : device_abis)
                        if (k.equals(j)) return true;
                }
            }
        }
        return false;
    }

    public static String getLibVersion() {
        String raw = AudioHqApis.getAudioFlingerInfo().responseMsg;

        if (Utils.isStringNotEmpty(raw)) {
            String[] process_1 = raw.split("\\[");
            String[] process_2 = process_1[1].split("]");
            return process_2[0];
        }
        return null;
    }

    public static boolean getMagiskInstalled(Context context) {
        final PackageManager packageManager = context.getPackageManager();//获取packagemanager
        List<PackageInfo> pinfo;//获取所有已安装程序的包信息
        pinfo = packageManager.getInstalledPackages(0);
        List<String> pName = new ArrayList<String>();//用于存储所有已安装程序的包名
        //从pinfo中将包名字逐一取出，压入pName list中
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                String pn = pinfo.get(i).packageName;
                pName.add(pn);
            }
        }
        return true;//pName.contains("com.topjohnwu.magisk");//判断pName中是否有目标程序的包名，有TRUE，没有FALSE
    }


}
