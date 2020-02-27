package io.alcatraz.audiohq.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.nativebuffers.Buffers;
import io.alcatraz.audiohq.beans.nativebuffers.Processes;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.services.AHQProtector;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.UpdateUtils;

public class PrefFragment extends PreferenceFragment {
    SwitchPreference boot;

    CheckBoxPreference default_silent;
    PreferenceScreen default_profile;
    CheckBoxPreference protector_service;

    CheckBoxPreference weak_key;
    PreferenceScreen check_update;
    PreferenceScreen clear_profile;
    PreferenceScreen uninstall_profile;

    boolean default_silent_val;
    boolean weak_key_val;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_content);
        findPreferences();
        bindLinsteners();
        updateSummary();
    }

    public void findPreferences() {
        check_update = (PreferenceScreen) findPreference(Constants.PREF_CHECK_UPDATE);
        default_silent = (CheckBoxPreference) findPreference(Constants.PREF_DEFAULT_SILENT);
        boot = (SwitchPreference) findPreference(Constants.PREF_BOOT);
        weak_key = (CheckBoxPreference) findPreference(Constants.PREF_WEAK_KEY_ADJUST);
        clear_profile = (PreferenceScreen) findPreference(Constants.PREF_CLEAR_PROFILES);
        uninstall_profile = (PreferenceScreen) findPreference(Constants.PREF_UNINSTALL_NATIVE);
        default_profile = (PreferenceScreen) findPreference(Constants.PREF_DEFAULT_PROFILE);
        protector_service = (CheckBoxPreference) findPreference(Constants.PREF_PROTECTOR);
    }

    @SuppressLint("DefaultLocale")
    public void bindLinsteners() {
        check_update.setOnPreferenceClickListener(preference -> {
            Toast.makeText(getContext(), R.string.toast_implementing, Toast.LENGTH_SHORT).show();
            UpdateUtils.checkUpdate();
            return true;
        });
        default_silent.setOnPreferenceChangeListener((preference, o) -> {
            if (!default_silent_val) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.pref_default_silent_warning_title)
                        .setMessage(R.string.pref_default_silent_warning_message)
                        .setNegativeButton(R.string.ad_nb, null)
                        .setPositiveButton(R.string.adjust_confirm, (dialogInterface, i) -> {
                            AudioHqApis.setDefaultSilentState(!default_silent_val);
                            default_silent.setChecked(!default_silent_val);
                            default_silent_val = !default_silent_val;
                            Toast.makeText(getContext(), R.string.pref_need_to_restart_playing_process, Toast.LENGTH_LONG).show();
                        }).show();
                return false;
            }
            AudioHqApis.setDefaultSilentState(false);
            default_silent.setChecked(!default_silent_val);
            default_silent_val = !default_silent_val;
            Toast.makeText(getContext(), R.string.pref_need_to_restart_playing_process, Toast.LENGTH_LONG).show();
            return false;
        });
        weak_key.setOnPreferenceChangeListener((preference, o) -> {
            AudioHqApis.setWeakKeyAdjust(!weak_key_val);
            weak_key.setChecked(!weak_key_val);
            weak_key_val = !weak_key_val;
            Toast.makeText(getContext(), R.string.pref_need_to_restart_playing_process, Toast.LENGTH_LONG).show();
            return false;
        });
        default_profile.setOnPreferenceClickListener(preference -> {
            ShellUtils.CommandResult result = AudioHqApis.getDefaultProfile();
            if (result.responseMsg != null) {

                Processes processes = new Processes();
                Buffers buffers = new Buffers();
                String[] prof = result.responseMsg.trim().split(",");
                buffers.setLeft(prof[0]);
                buffers.setRight(prof[1]);
                buffers.setFinalv(prof[2]);
                buffers.setControl_lr(prof[3]);
                List<Buffers> b = new ArrayList<>();
                b.add(buffers);
                processes.setBuffers(b);

                Panels.getAdjustPanel(getActivity(), processes, true, false, new AsyncInterface<AlertDialog>() {
                    @Override
                    public boolean onAyncDone(@Nullable AlertDialog val) {
                        assert val != null;
                        val.dismiss();
                        return true;
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                }).show();
            }
            return true;
        });
        protector_service.setOnPreferenceChangeListener((preference, o) -> {
            boolean newVal = (boolean) o;
            if(newVal){
                getContext().startService(new Intent(getContext(), AHQProtector.class));
            }else {
                getContext().stopService(new Intent(getContext(),AHQProtector.class));
            }
            return true;
        });

        clear_profile.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.pref_default_silent_warning_title)
                    .setMessage(R.string.pref_clear_profile_confirm)
                    .setNegativeButton(R.string.ad_nb, null)
                    .setPositiveButton(R.string.adjust_confirm, (dialogInterface, i) -> AudioHqApis.clearAllNativeSettings()).show();
            return true;
        });
        uninstall_profile.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.pref_3_3)
                    .setMessage(R.string.uninstall_steps)
                    .setNegativeButton(R.string.ad_nb, null).show();
            return true;
        });
    }

    public void updateSummary() {
        ShellUtils.CommandResult switches = AudioHqApis.getSwitches();
        if (switches.responseMsg != null) {
            String[] switches_str = switches.responseMsg.split(";");
            default_silent_val = switches_str[1].equals("true");
            default_silent.setChecked(default_silent_val);
            weak_key_val = switches_str[2].equals("true");
            weak_key.setChecked(weak_key_val);
        }
    }
}
