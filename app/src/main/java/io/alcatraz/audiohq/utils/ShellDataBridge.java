package io.alcatraz.audiohq.utils;

import android.os.Looper;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.beans.nativebuffers.PackageBuffers;
import io.alcatraz.audiohq.beans.nativebuffers.ProcessBuffers;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;

public class ShellDataBridge {
    public static void getProcessBuffers(AsyncInterface<ProcessBuffers> asyncInterface) {
        new Thread(() -> {
            Looper.prepare();
            ShellUtils.CommandResult buffers_res = AudioHqApis.getAllPlayingClients(0);
            if (buffers_res.responseMsg != null) {
                try {
                    ProcessBuffers buffers = Utils.json2Object(buffers_res.responseMsg, ProcessBuffers.class);
                    asyncInterface.onAyncDone(buffers);
                } catch (Exception e) {
                    asyncInterface.onFailure(e.getMessage());
                }
            } else {
                asyncInterface.onFailure("Null shell result!");
            }
        }).start();
    }

    public static void getPackageBuffers(AsyncInterface<PackageBuffers> asyncInterface) {
        new Thread(() -> {
            Looper.prepare();
            ShellUtils.CommandResult buffers_res = AudioHqApis.getAllPlayingClients(1);
            if (buffers_res.responseMsg != null) {
                try {
                    PackageBuffers buffers = Utils.json2Object(buffers_res.responseMsg, PackageBuffers.class);
                    asyncInterface.onAyncDone(buffers);
                } catch (Exception e) {
                    asyncInterface.onFailure(e.getMessage());
                }
            } else {
                asyncInterface.onFailure("Null shell result!");
            }
        }).start();
    }

}
