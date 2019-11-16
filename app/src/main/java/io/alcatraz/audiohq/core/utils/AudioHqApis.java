package io.alcatraz.audiohq.core.utils;

public class AudioHqApis {
    public static final String AUDIOHQ_THREAD_ALL_USE = "all";
    public static final String AUDIOHQ_THREAD_ENUM_CURRENT = "enum_current";

    public static final String AUDIOHQ_SERVER_TYPE_FIFO = "1";
    public static final String AUDIOHQ_SERVER_TYPE_MEMORY = "2";
    public static final String AUDIOHQ_SERVER_TYPE_LOG = "3";
    public static final String AUDIOHQ_SERVER_TYPE_NO_AUTOMATION = "3";
    public static final String AUDIOHQ_SERVER_NONE = "256";


    public static ShellUtils.CommandResult setPkgVolume(String pkgname,
                                                        float prog_general,
                                                        float prog_left,
                                                        float prog_right,
                                                        boolean split_control,
                                                        String thread) {
        String split = "0";
        if (split_control)
            split = "1";
        return runAudioHqCmd(AudioHqCmds.SET_PKG_VOLUME, pkgname,
                prog_left + "", prog_right + "", prog_general + "", split, thread);
    }

    public static ShellUtils.CommandResult setPidVolume(String pid,
                                                        float prog_general,
                                                        float prog_left,
                                                        float prog_right,
                                                        boolean split_control,
                                                        String thread) {
        String split = "0";
        if (split_control)
            split = "1";
        return runAudioHqCmd(AudioHqCmds.SET_PID_VOLUME, pid,
                prog_left + "", prog_right + "", prog_general + "", split, thread);
    }

    public static ShellUtils.CommandResult setPidVolume(String pid,
                                                        float prog_general,
                                                        float prog_left,
                                                        float prog_right,
                                                        boolean split_control) {
        return setPidVolume(pid, prog_left, prog_right, prog_general, split_control, AUDIOHQ_THREAD_ALL_USE);
    }

    public static ShellUtils.CommandResult setPkgVolume(String pkgname,
                                                        float prog_general,
                                                        float prog_left,
                                                        float prog_right,
                                                        boolean split_control) {
        return setPkgVolume(pkgname, prog_general, prog_left, prog_right, split_control, AUDIOHQ_THREAD_ALL_USE);
    }

    public static ShellUtils.CommandResult getSetPkgs(String thread) {
        return runAudioHqCmd(AudioHqCmds.GET_SET_PKGS, thread);
    }

    public static ShellUtils.CommandResult getSetPkgs() {
        return getSetPkgs(AUDIOHQ_THREAD_ALL_USE);
    }

    public static ShellUtils.CommandResult getAllPlayingClients() {
        return runAudioHqCmd(AudioHqCmds.GET_ALL_PLAYING_CLIENTS);
    }

    public static ShellUtils.CommandResult unsetForPkg(String pkg, String thread) {
        return runAudioHqCmd(AudioHqCmds.UNSET_PKG_VOLUME, pkg, thread);
    }

    public static ShellUtils.CommandResult unsetForPkg(String pkg) {
        return unsetForPkg(pkg, AUDIOHQ_THREAD_ALL_USE);
    }

    public static ShellUtils.CommandResult getRunningServerType() {
        return runAudioHqCmd(AudioHqCmds.GET_RUNNING_SERVER_TYPE);
    }

    public static boolean isServerRunning() {
        ShellUtils.CommandResult res = getRunningServerType();
        return res.errorMsg.length() < 1 && !res.responseMsg.contains("running");
    }

    public static ShellUtils.CommandResult killNativeServer() {
        return runAudioHqCmd(AudioHqCmds.KILL_SERVER);
    }

    public static ShellUtils.CommandResult getTrackCtrlPosition() {
        return runAudioHqCmd(AudioHqCmds.GET_TRACK_CTL_POSITION);
    }

    public static ShellUtils.CommandResult setTrackCtrlPosition(String position) {
        return runAudioHqCmd(AudioHqCmds.SET_TRACK_CTL_POSITION, position);
    }

    public static ShellUtils.CommandResult getAutoRemoveFlag() {
        return runAudioHqCmd(AudioHqCmds.GET_AUTO_REMOVE_FLAG);
    }

    public static ShellUtils.CommandResult setAutoRemoveFlag(boolean auto_remove, String thread) {
        return runAudioHqCmd(AudioHqCmds.SET_AUTO_REMOVE_FLAG, auto_remove + "", thread);
    }

    public static ShellUtils.CommandResult setAutoRemoveFlag(boolean auto_remove) {
        return setAutoRemoveFlag(auto_remove, AUDIOHQ_THREAD_ALL_USE);
    }

    public static ShellUtils.CommandResult getPlaybackThreadsCount() {
        return runAudioHqCmd(AudioHqCmds.GET_CURRENT_PLAYBACKTHREAD_COUNT);
    }

    public static ShellUtils.CommandResult clearAllNativeSettings() {
        return runAudioHqCmd(AudioHqCmds.CLEAR_ALL_SETTINGS);
    }

    public static ShellUtils.CommandResult getTrackCtrlLockState() {
        return runAudioHqCmd(AudioHqCmds.GET_TRACK_CTL_LOCK_STATE);
    }

    public static ShellUtils.CommandResult setTrackCtrlLockState(String state) {
        return runAudioHqCmd(AudioHqCmds.SET_TRACK_CTL_LOCK_STATE, state);
    }

    public static ShellUtils.CommandResult getAllSetPids(String thread) {
        return runAudioHqCmd(AudioHqCmds.GET_ALL_SET_PIDS, thread);
    }

    public static ShellUtils.CommandResult getAllSetPids() {
        return getAllSetPids(AUDIOHQ_THREAD_ALL_USE);
    }

    public static void startServer(String type, String output_file_dir) {
        runAudioHqCmd(AudioHqCmds.START_NATIVE_SERVER, type, output_file_dir);
    }

    public static ShellUtils.CommandResult getAudioHqNativeInfo() {
        return runAudioHqCmd(AudioHqCmds.GET_NATIVE_ELF_INFO);
    }

    public static ShellUtils.CommandResult getAudioFlingerInfo() {
        return runAudioHqCmd(AudioHqCmds.GET_LIB_INFO);
    }

    public static ShellUtils.CommandResult getMSetPackage() {
        return runAudioHqCmd(AudioHqCmds.M_GET_SET_PKGS);
    }

    public static ShellUtils.CommandResult setMPackageVolume(String pkgname,
                                                             float prog_general,
                                                             float prog_left,
                                                             float prog_right,
                                                             boolean split_control) {
        String split = "0";
        if (split_control)
            split = "1";
        return runAudioHqCmd(AudioHqCmds.M_SET_PKG_VOLUME, pkgname,
                prog_left + "", prog_right + "", prog_general + "", split);
    }

    public static ShellUtils.CommandResult mUnsetForPkg(String pkg) {
        return runAudioHqCmd(AudioHqCmds.M_UNSET_PKG, pkg);
    }

    public static ShellUtils.CommandResult mClear() {
        return runAudioHqCmd(AudioHqCmds.M_CLEAR);
    }

    public static ShellUtils.CommandResult getDefaultSilentState() {
        return runAudioHqCmd(AudioHqCmds.GET_DEFAULT_SILENT_STATE);
    }

    public static ShellUtils.CommandResult setDefaultSilentState(boolean state) {
        return runAudioHqCmd(AudioHqCmds.SET_DEFAULT_SILENT_STATE, state ? "true" : "false");
    }

    public static ShellUtils.CommandResult setMute(String pkg, boolean mute) {
        return runAudioHqCmd(AudioHqCmds.M_SET_PKG_MUTE, pkg, mute ? "true" : "false");
    }

    public static ShellUtils.CommandResult getReadProcState() {
        return runAudioHqCmd(AudioHqCmds.GET_READPROC_STATE);
    }

    public static ShellUtils.CommandResult setReadProcState(boolean use_readproc) {
        return runAudioHqCmd(AudioHqCmds.SET_READPROC_STATE, use_readproc ? "true" : "false");
    }

    public static ShellUtils.CommandResult runAudioHqCmd(AudioHqCmds audioHqCmds, String... params) {
        String cmd;
        if (audioHqCmds.hasParams())
            cmd = audioHqCmds.createCmd(params);
        else
            cmd = audioHqCmds.getCmd_raw();
        return ShellUtils.execCommand(cmd, audioHqCmds.requiresRoot());
    }

    enum AudioHqCmds {
        SET_PKG_VOLUME("audiohq -k\"%s;%s,%s,%s,%s\" -i %s", true, true),
        GET_SET_PKGS("audiohq -k -i %s", true, true),
        GET_ALL_PLAYING_CLIENTS("audiohq -P", false, false),
        UNSET_PKG_VOLUME("audiohq -U \"%s\" -i %s", true, true),
        GET_RUNNING_SERVER_TYPE("audiohq -y", true, false),
        KILL_SERVER("audiohq -R", true, false),
        GET_TRACK_CTL_POSITION("audiohq -o", false, false),
        SET_TRACK_CTL_POSITION("audiohq -o%s", true, true),
        GET_AUTO_REMOVE_FLAG("audiohq -r", false, false),
        SET_AUTO_REMOVE_FLAG("audiohq -r%s -i %s", true, true),
        GET_CURRENT_PLAYBACKTHREAD_COUNT("audiohq -c", false, false),
        CLEAR_ALL_SETTINGS("audiohq -C", true, false),
        GET_TRACK_CTL_LOCK_STATE("audiohq -l", true, false),
        SET_TRACK_CTL_LOCK_STATE("audiohq -l%s", true, true),
        GET_ALL_SET_PIDS("audiohq -p -i %s", false, true),
        SET_PID_VOLUME("audiohq -a \"%s,%s,%s,%s,%s -i %s\"", true, true),
        START_NATIVE_SERVER("nohup audiohq -d %s > %s &", true, true),
        GET_NATIVE_ELF_INFO("audiohq -v", false, false),
        GET_LIB_INFO("audiohq -V", true, true),
        GET_DEFAULT_SILENT_STATE("audiohq -n", false, false),
        SET_DEFAULT_SILENT_STATE("audiohq -n%s", true, true),
        GET_READPROC_STATE("audiohq -S", false, false),
        SET_READPROC_STATE("audiohq -S%s", true, true),

        M_SET_PKG_VOLUME("audiohq -z\"%s|%s,%s,%s,%s\"", true, true),
        M_GET_SET_PKGS("audiohq -z", false, false),
        M_SET_PKG_MUTE("audiohq -M\"%s|%s\"", true, true),
        M_GET_PKG_MUTE("audiohq -M", false, false),
        M_UNSET_PKG("audiohq -x \"%s\"", true, true),
        M_CLEAR("audiohq -b", true, false);


        private String cmd_raw;
        private boolean require_root;
        private boolean has_params;

        AudioHqCmds(String cmd_raw, boolean require_root, boolean has_params) {
            this.cmd_raw = cmd_raw;
            this.require_root = require_root;
            this.has_params = has_params;
        }

        public boolean requiresRoot() {
            return require_root;
        }

        public boolean hasParams() {
            return has_params;
        }

        public String getCmd_raw() {
            return cmd_raw;
        }

        @SuppressWarnings("ConfusingArgumentToVarargsMethod")
        public String createCmd(String... params) {
            return String.format(cmd_raw, params);
        }
    }
}
