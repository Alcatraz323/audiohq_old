package io.alcatraz.audiohq.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.AppListBean;
import io.alcatraz.audiohq.beans.ServerStatus;
import io.alcatraz.audiohq.beans.TrackBean;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;

public class ShellDataBridge {
    public static void getPlayingMap(Activity context, AsyncInterface<Map<String, AppListBean>> asyncInterface) {
        new Thread(() -> {
            Looper.prepare();
            ShellUtils.CommandResult raw = AudioHqApis.getAllPlayingClients();
            InstallUtils.checkAndShowInstallation(context);
            HashMap<String, AppListBean> out = new HashMap<>();
            List<String> saved_pids = new ArrayList<>();

            if (Utils.isStringNotEmpty(raw.errorMsg)) {
                asyncInterface.onFailure(raw.errorMsg);
                asyncInterface.onAyncDone(out);
            } else {
                String[] process_1 = raw.responseMsg.split("\n");

                String current_thread = "";
                for (String i : process_1) {
                    if (!Utils.isStringNotEmpty(i))
                        continue;

                    if (i.contains("Operating Thread")) {
                        String[] thread = i.split(" -> ");
                        if (thread.length >= 2)
                            current_thread = thread[1];

                        continue;
                    }

                    String[] process_2 = i.split(";");

                    if (process_2.length <= 6) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                Toast.makeText(context,i,Toast.LENGTH_LONG).show();

                            }
                        });
                        Toast.makeText(context,i,Toast.LENGTH_LONG).show();
                        continue;
                    }

                    TrackBean current = new TrackBean();
                    current.setPid(process_2[0]);
                    current.setThread(current_thread);
                    current.setSessionId(process_2[1]);
                    current.setSampleRate(process_2[2]);
                    current.setFrameSize(process_2[3]);
                    current.setActive(process_2[4]);
                    if (saved_pids.contains(current.getPid())) {
                        out.get(current.getPid()).addTrack(current);
                        continue;
                    }

                    saved_pids.add(current.getPid());
                    AppListBean new_app = new AppListBean();

                    //new_app.setPkgName(PackageCtlUtils.getProcessName(current.getPid()));
                    new_app.setPkgName(process_2[5]);
                    new_app.setProfile(process_2[6]);

                    Drawable icon = PackageCtlUtils.getIcon(context, new_app.getPkgName().contains(":") ? new_app.getPkgName().split(":")[0] : new_app.getPkgName());
                    if (icon != null) {
                        new_app.setIcon(icon);
                    } else {
                        new_app.setIcon(context.getDrawable(R.mipmap.ic_launcher_round));
                    }

                    new_app.setPid(current.getPid());
                    new_app.addTrack(current);
                    out.put(current.getPid(), new_app);
                }
                asyncInterface.onAyncDone(out);
            }
        }).start();
    }


}
