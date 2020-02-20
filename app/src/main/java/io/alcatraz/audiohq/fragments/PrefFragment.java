package io.alcatraz.audiohq.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Arrays;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.AppListBean;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.CheckUtils;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.utils.InstallUtils;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.SharedPreferenceUtil;
import io.alcatraz.audiohq.utils.UpdateUtils;

public class PrefFragment extends PreferenceFragment{
    SwitchPreference boot;

    CheckBoxPreference default_silent;
    PreferenceScreen default_profile;

    PreferenceScreen check_update;
    PreferenceScreen clear_profile;
    PreferenceScreen uninstall_profile;

    boolean default_silent_val;

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
        clear_profile = (PreferenceScreen) findPreference(Constants.PREF_CLEAR_PROFILES);
        uninstall_profile = (PreferenceScreen) findPreference(Constants.PREF_UNINSTALL_NATIVE);
        default_profile = (PreferenceScreen) findPreference(Constants.PREF_DEFAULT_PROFILE);
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
                        }).show();
                return false;
            }
            AudioHqApis.setDefaultSilentState(false);
            default_silent.setChecked(!default_silent_val);
            default_silent_val = !default_silent_val;
            return false;
        });
        default_profile.setOnPreferenceClickListener(preference -> {
            ShellUtils.CommandResult result = AudioHqApis.getDefaultProfile();
            if(result.responseMsg!=null) {
                AppListBean bean = new AppListBean();
                bean.setProfile(result.responseMsg);
                Panels.getAdjustPanel(getActivity(), bean, new AsyncInterface() {
                    @Override
                    public boolean onAyncDone(@Nullable Object val) {
                        return false;
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                },true).show();
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
        default_silent_val = AudioHqApis.getDefaultSilentState().responseMsg.contains("true");
        default_silent.setChecked(default_silent_val);
    }
}
