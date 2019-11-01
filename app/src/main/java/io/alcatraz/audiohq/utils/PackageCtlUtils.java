package io.alcatraz.audiohq.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import io.alcatraz.audiohq.core.utils.ShellUtils;

public class PackageCtlUtils {
    public static Drawable getIcon(Context ctx, String pkg) {
        PackageManager pm = ctx.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA);
            return pm.getApplicationIcon(ai);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static String getLabel(Context ctx, String pkg) {
        PackageManager pm = ctx.getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pkg, PackageManager.GET_META_DATA);
            return pm.getApplicationLabel(ai).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    public static String getProcessName(String pid) {
        return ShellUtils.execCommand("ps -p " + pid + " -o NAME | grep -v NAME", true).responseMsg;
    }
}
