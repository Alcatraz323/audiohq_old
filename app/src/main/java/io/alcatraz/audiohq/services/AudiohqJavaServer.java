package io.alcatraz.audiohq.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.LogBuff;
import io.alcatraz.audiohq.beans.AdjustProfiles;
import io.alcatraz.audiohq.beans.ProcessProfile;
import io.alcatraz.audiohq.beans.RunningProcess;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;

public class AudiohqJavaServer extends Service {
    private static final String SERVER_ACTION_PREFIX = "audiohq_server_action_";
    public static final String SERVER_ACTION_ADJUST_PROCESS = SERVER_ACTION_PREFIX + "add_process";
    public static final String SERVER_ACTION_ADJUST_PID = SERVER_ACTION_PREFIX + "adjust_pid";

    ServerListener mServerListner;
    AdjustProfiles general_profile;
    Map<String, RunningProcess> running_processes = new HashMap<>();
    Map<String, String> adjusted_processes = new HashMap<>();
    Map<String, String> adjusted_processes_mkey_pid = new HashMap<>();
    Thread process_monitor_thread;
    ProcessMonitor processMonitor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initData();
        startProcessMonitor();
        registReceivers();
        LogBuff.I("Audiohq Java Server Initialized");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogBuff.I("Stopping server");
        unregisterReceiver(mServerListner);
        LogBuff.I("Unregistered receiver");
        processMonitor.kill();
        process_monitor_thread.interrupt();
        LogBuff.I("Killed monitor thread");

        LogBuff.I("Server has been stopped");
    }

    public synchronized void adjustProcess(ProcessProfile profile) {
        if (running_processes.containsKey(profile.getProcessName())) {
            RunningProcess process = running_processes.get(profile.getProcessName());
            AudioHqApis.setPidVolume(process.getPid(),
                    profile.getGeneral(),
                    profile.getLeft(),
                    profile.getRight(),
                    profile.getControl_lr());
            adjusted_processes.put(profile.getProcessName(), process.getPid());
            adjusted_processes_mkey_pid.put(process.getPid(), process.getProcessName());
        }
        modifyGeneralProfiles(profile, false);
    }

    private int modifyGeneralProfiles(ProcessProfile target, boolean remove) {
        List<ProcessProfile> processProfiles = general_profile.getProcessProfile();
        for (int i = 0; i < processProfiles.size(); i++) {
            if (target.getProcessName().equals(processProfiles.get(i).getProcessName())) {
                general_profile.getProcessProfile().remove(i);
                general_profile.getProcessProfile().add(target);
                return 1;
            }
        }
        general_profile.getProcessProfile().add(target);
        return 2;
    }

    private void registReceivers() {
        LogBuff.I("Registering Controller");
        IntentFilter ifil = new IntentFilter();
        ifil.addAction(Constants.BROADCAST_ACTION_UPDATE_PREFERENCES);
        ifil.addAction(SERVER_ACTION_ADJUST_PROCESS);
        mServerListner = new ServerListener();
        registerReceiver(mServerListner, ifil);
        LogBuff.I("Controller registered");

    }

    private void initData() {
        LogBuff.I("Loading profile data");
        general_profile = AdjustProfiles.getProfiles(this);
    }

    private void startProcessMonitor() {
        LogBuff.I("Starting process monitor thread");
        if (process_monitor_thread != null) {
            LogBuff.I("Thread existed,interrupting");
            process_monitor_thread.interrupt();
        }
        processMonitor = new ProcessMonitor();
        process_monitor_thread = new Thread(processMonitor);
        process_monitor_thread.start();
        LogBuff.I("Process monitor started");
    }

    class ServerListener extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogBuff.I("Received broadcast from ["+intent.getPackage()+"]");
            switch (Objects.requireNonNull(intent.getAction())) {
                case SERVER_ACTION_ADJUST_PROCESS:
                    ProcessProfile profile = intent.getParcelableExtra(SERVER_ACTION_ADJUST_PROCESS);
                    LogBuff.I("Received process adjust intent for process:[" + profile.getProcessName() + "]");
                    processMonitor.runOnNextCirculation(new AsyncInterface() {
                        @Override
                        public boolean onAyncDone(@Nullable Object val) {
                            adjustProcess(profile);
                            general_profile.saveToLocal(getFilesDir() + AdjustProfiles.DEFAULT_PROFILE_POSITION_AFFIX);
                            return false;
                        }

                        @Override
                        public void onFailure(String reason) {

                        }
                    });
                    break;
            }
        }
    }

    class ProcessMonitor implements Runnable {
        private boolean require_pause = false;
        private boolean live = true;
        private AsyncInterface asyncInterface;
        private int fail_count = 0;

        public void runOnNextCirculation(AsyncInterface asyncInterface) {
            this.asyncInterface = asyncInterface;
            require_pause = true;
        }

        void kill() {
            live = false;
        }

        @Override
        public void run() {
            while (live) {
                //TODO : Optimize sequence
                if (require_pause) {
                    LogBuff.I("Running injected");
                    asyncInterface.onAyncDone(null);
                    require_pause = false;
                }
                running_processes.clear();
                ShellUtils.CommandResult current_raw
                        = ShellUtils.execCommand("ps -A -o PID -o NAME -o CMDLINE", true, true, false);

                if(current_raw.responseMsg == null ) {
                    fail_count++;
                    if(fail_count >= 5){
                        kill();
                        LogBuff.E("Failed to read shell too many times, server killed");
                    }
                    continue;
                }
                //Update running process info
                String[] process_0 = current_raw.responseMsg.split("\n");
                for (int i = 1; i < process_0.length; i++) {
                    String[] process_1 = process_0[i].trim().split(" +");
                    RunningProcess process = new RunningProcess();
                    process.setPid(process_1[0]);
                    process.setProcessName(process_1[1]);
                    process.setCmdline(process_1[2]);
                    running_processes.put(process_1[1], process);
                    if (adjusted_processes_mkey_pid.containsKey(process.getPid())) {
                        if (!adjusted_processes_mkey_pid.get(process.getPid()).equals(process.getProcessName())) {
                            LogBuff.I("Detected pid set but processed killed condition:["+process.getPid()+"], restoring");
                            AudioHqApis.unsetForPid(process.getPid());
                            adjusted_processes_mkey_pid.remove(process.getPid());
                            adjusted_processes.remove(process.getProcessName());
                        }
                    }
                }

                //Dynamically adjustment
                List<ProcessProfile> processProfiles = general_profile.getProcessProfile();
                for (ProcessProfile i : processProfiles) {
                    if (running_processes.containsKey(i.getProcessName()) && !adjusted_processes.containsKey(i.getProcessName())) {
                        LogBuff.I("Profile found for :["+i.getProcessName()+"], doing adjust");
                        RunningProcess process = running_processes.get(i.getProcessName());
                        AudioHqApis.setPidVolume(process.getPid(),
                                i.getGeneral(),
                                i.getLeft(),
                                i.getRight(),
                                i.getControl_lr()
                        );
                        LogBuff.I("Native adjust done, recording adjusted");
                        adjusted_processes.put(process.getProcessName(), process.getPid());
                        adjusted_processes_mkey_pid.put(process.getPid(), process.getProcessName());
                    }
                }
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
