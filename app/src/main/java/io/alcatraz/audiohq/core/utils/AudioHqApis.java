package io.alcatraz.audiohq.core.utils;

public class AudioHqApis {
    public static ShellUtils.CommandResult setProfile(String process_name,
                                                      float prog_general,
                                                      float prog_left,
                                                      float prog_right,
                                                      boolean split_control,
                                                      boolean isweakkey) {
        return runAudioHqCmd(AudioHqCmds.SET_PROFILE, process_name,
                prog_left + "", prog_right + "", prog_general + "",
                split_control ? "true" : "false", isweakkey ? "true" : "false");
    }

    public static ShellUtils.CommandResult getSwitches() {
        ShellUtils.CommandResult command = runAudioHqCmd(AudioHqCmds.GET_SWITCHES);
        if(command.responseMsg!=null){
            command.responseMsg = command.responseMsg.replaceAll("[\n]","");
        }
        return command;
    }

    public static ShellUtils.CommandResult getAllPlayingClients(int mode) {
        return runAudioHqCmd(AudioHqCmds.LIST_ALL_BUFFER, mode + "");
    }

    public static ShellUtils.CommandResult clearAllNativeSettings() {
        return runAudioHqCmd(AudioHqCmds.CLEAR_ALL_SETTINGS);
    }

    public static ShellUtils.CommandResult getAudioHqNativeInfo() {
        return runAudioHqCmd(AudioHqCmds.GET_NATIVE_ELF_INFO);
    }

    public static ShellUtils.CommandResult setDefaultSilentState(boolean state) {
        return runAudioHqCmd(AudioHqCmds.SET_DEFAULT_SILENT_STATE, state ? "true" : "false");
    }

    public static ShellUtils.CommandResult setWeakKeyAdjust(boolean state) {
        return runAudioHqCmd(AudioHqCmds.SET_WEAK_KEY_ADJUST, state ? "true" : "false");
    }

    public static ShellUtils.CommandResult muteProcess(String process_name, boolean isweakkey) {
        return runAudioHqCmd(AudioHqCmds.MUTE_PROCESS, process_name, isweakkey ? "true" : "false");
    }

    public static ShellUtils.CommandResult unmuteProcess(String process_name, boolean isweakkey) {
        return runAudioHqCmd(AudioHqCmds.UNMUTE_PROCESS, process_name, isweakkey ? "true" : "false");
    }

    public static void startNativeService(){
        runAudioHqCmd(AudioHqCmds.START_NATIVE_SERVICE);
    }

    public static ShellUtils.CommandResult getDefaultProfile() {
        ShellUtils.CommandResult command = runAudioHqCmd(AudioHqCmds.GET_DEFAULT_PROFILE);
        if(command.responseMsg!=null){
            command.responseMsg = command.responseMsg.replaceAll("[\n\t]","");
        }
        return command;
    }

    public static ShellUtils.CommandResult setDefaultProfile(float prog_general,
                                                             float prog_left,
                                                             float prog_right,
                                                             boolean split_control) {
        return runAudioHqCmd(AudioHqCmds.SET_DEFAULT_PROFILE,
                prog_left + "", prog_right + "", prog_general + "", split_control ? "true" : "false");
    }

    public static ShellUtils.CommandResult runAudioHqCmd(AudioHqCmds audioHqCmds, String... params) {
        String cmd;
        if (audioHqCmds.hasParams())
            cmd = audioHqCmds.createCmd(params);
        else
            cmd = audioHqCmds.getCmd_raw();
        ShellUtils.CommandResult result = ShellUtils.execCommand(cmd, audioHqCmds.requiresRoot());
        if(result.responseMsg!=null&&result.responseMsg.length()!=0){
            result.responseMsg = result.responseMsg.substring(0,result.responseMsg.length()-1);
        }
        return result;
    }

    enum AudioHqCmds {
        SET_PROFILE("audiohq --set-profile \"%s\" %s %s %s %s %s", false, true),
        GET_SWITCHES("audiohq --switches", false, false),
        SET_DEFAULT_SILENT_STATE("audiohq --def-silent %s", false, true),
        GET_DEFAULT_PROFILE("audiohq --def-profile", false, false),
        SET_DEFAULT_PROFILE("audiohq --def-profile %s %s %s %s", false, true),
        MUTE_PROCESS("audiohq --mute \"%s\" %s", false, true),
        UNMUTE_PROCESS("audiohq --unmute \"%s\" %s", false, true),
        CLEAR_ALL_SETTINGS("audiohq --clear", false, false),
        LIST_ALL_BUFFER("audiohq --list-buffers %s", false, true),
        GET_NATIVE_ELF_INFO("audiohq --elf-info", false, false),
        SET_WEAK_KEY_ADJUST("audiohq --weak-key %s",false,true),
        START_NATIVE_SERVICE("audiohq --service",true,false);


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
