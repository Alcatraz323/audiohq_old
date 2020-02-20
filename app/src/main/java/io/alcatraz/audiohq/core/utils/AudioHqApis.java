package io.alcatraz.audiohq.core.utils;

import android.content.Context;
import android.content.Intent;

import io.alcatraz.audiohq.beans.ProcessProfile;
import io.alcatraz.audiohq.services.AudiohqJavaServer;

public class AudioHqApis {
    public static ShellUtils.CommandResult setProfile(String process_name,
                                                      float prog_general,
                                                      float prog_left,
                                                      float prog_right,
                                                      boolean split_control) {
        String split = "0";
        if (split_control)
            split = "1";
        return runAudioHqCmd(AudioHqCmds.SET_PROFILE, process_name,
                prog_left + "", prog_right + "", prog_general + "", split);
    }

    public static ShellUtils.CommandResult listProfile() {
        return runAudioHqCmd(AudioHqCmds.LIST_PROFILE);
    }

    public static ShellUtils.CommandResult getAllPlayingClients() {
        return runAudioHqCmd(AudioHqCmds.GET_ALL_PLAYING_CLIENTS);
    }

    public static ShellUtils.CommandResult unsetProfile(String process_name) {
        return runAudioHqCmd(AudioHqCmds.UNSET_PROFILE, process_name);
    }

    public static ShellUtils.CommandResult getPlaybackThreadsCount() {
        return runAudioHqCmd(AudioHqCmds.GET_CURRENT_PLAYBACKTHREAD_COUNT);
    }

    public static ShellUtils.CommandResult clearAllNativeSettings() {
        return runAudioHqCmd(AudioHqCmds.CLEAR_ALL_SETTINGS);
    }

    public static ShellUtils.CommandResult getAudioHqNativeInfo() {
        return runAudioHqCmd(AudioHqCmds.GET_NATIVE_ELF_INFO);
    }

    public static ShellUtils.CommandResult getAudioFlingerInfo() {
        return runAudioHqCmd(AudioHqCmds.GET_LIB_INFO);
    }

    public static ShellUtils.CommandResult getDefaultSilentState() {
        return runAudioHqCmd(AudioHqCmds.GET_DEFAULT_SILENT_STATE);
    }

    public static ShellUtils.CommandResult setDefaultSilentState(boolean state) {
        return runAudioHqCmd(AudioHqCmds.SET_DEFAULT_SILENT_STATE, state ? "true" : "false");
    }

    public static ShellUtils.CommandResult muteProcess(String process_name) {
        return runAudioHqCmd(AudioHqCmds.MUTE_PROCESS, process_name);
    }

    public static ShellUtils.CommandResult unmuteProcess(String process_name) {
        return runAudioHqCmd(AudioHqCmds.UNMUTE_PROCESS, process_name);
    }

    public static ShellUtils.CommandResult getLog() {
        return runAudioHqCmd(AudioHqCmds.GET_LOG);
    }

    public static ShellUtils.CommandResult saveLog() {
        return runAudioHqCmd(AudioHqCmds.UNMUTE_PROCESS);
    }

    public static ShellUtils.CommandResult getDefaultProfile() {
        return runAudioHqCmd(AudioHqCmds.GET_DEFAULT_PROFILE);
    }

    public static ShellUtils.CommandResult setDefaultProfile(float prog_general,
                                                                 float prog_left,
                                                                 float prog_right,
                                                                 boolean split_control) {
        String split = "0";
        if (split_control)
            split = "1";
        return runAudioHqCmd(AudioHqCmds.SET_DEFAULT_PROFILE,
                prog_left + "", prog_right + "", prog_general + "", split);
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
        SET_PROFILE("audiohq --set-profile \"%s|%s,%s,%s,%s\"", true, true),
        LIST_PROFILE("audiohq --list-profile", false, false),
        GET_ALL_PLAYING_CLIENTS("audiohq --list-tracks -t all", false, false),
        UNSET_PROFILE("audiohq --unset-profile \"%s\"", true, true),
        GET_CURRENT_PLAYBACKTHREAD_COUNT("audiohq --count", false, false),
        CLEAR_ALL_SETTINGS("audiohq --clear", true, false),
        GET_NATIVE_ELF_INFO("audiohq --elf-info", false, false),
        GET_LIB_INFO("audiohq --lib-info", true, false),
        GET_DEFAULT_SILENT_STATE("audiohq --def-silent", false, false),
        SET_DEFAULT_SILENT_STATE("audiohq --def-silent %s", true, true),
        MUTE_PROCESS("audiohq --mute \"%s\"", true, true),
        UNMUTE_PROCESS("audiohq --unmute \"%s\"", true, true),
        GET_LOG("audiohq --get-log", false, false),
        SAVE_LOG("audiohq --save-log", true, false),
        GET_DEFAULT_PROFILE("audiohq --def-profile", false, false),
        SET_DEFAULT_PROFILE("audiohq --def-profile \"%s,%s,%s,%s\"", true, true);


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
