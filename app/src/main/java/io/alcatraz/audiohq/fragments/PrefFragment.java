package io.alcatraz.audiohq.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.Arrays;

import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.utils.SharedPreferenceUtil;
import io.alcatraz.audiohq.utils.UpdateUtils;

public class PrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    ListPreference service;
    PreferenceScreen check_update;
    CheckBoxPreference default_silent;
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
        service = (ListPreference) findPreference(Constants.PREF_SERVICE_TYPE);
        check_update = (PreferenceScreen) findPreference(Constants.PREF_CHECK_UPDATE);
        default_silent = (CheckBoxPreference) findPreference(Constants.PREF_DEFAULT_SILENT);
        clear_profile = (PreferenceScreen) findPreference(Constants.PREF_CLEAR_PROFILES);
        uninstall_profile = (PreferenceScreen) findPreference(Constants.PREF_UNINSTALL_NATIVE);
    }

    public void bindLinsteners() {
        service.setOnPreferenceChangeListener(this);
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
        clear_profile.setOnPreferenceClickListener(preference -> {
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.pref_default_silent_warning_title)
                    .setMessage(R.string.pref_clear_profile_confirm)
                    .setNegativeButton(R.string.ad_nb, null)
                    .setPositiveButton(R.string.adjust_confirm, (dialogInterface, i) -> AudioHqApis.mClear()).show();
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
        SharedPreferenceUtil spf = SharedPreferenceUtil.getInstance();
        String service_type = (String) spf.get(getActivity(), Constants.PREF_SERVICE_TYPE, Constants.DEFAULT_VALUE_PREF_SERVICE);
        String[] service_types = getResources().getStringArray(R.array.entryvalues_for_server_mode);
        int index_of_service_type = Arrays.asList(service_types).indexOf(service_type);
        String[] location_options = getResources().getStringArray(R.array.entries_for_server_mode);
        service.setSummary(location_options[index_of_service_type]);

        default_silent_val = AudioHqApis.getDefaultSilentState().responseMsg.contains("true");
        default_silent.setChecked(default_silent_val);
    }

    public void updateServiceSummary(String value) {
        String[] service_types = getResources().getStringArray(R.array.entryvalues_for_server_mode);
        int index_of_service_type = Arrays.asList(service_types).indexOf(value);
        String[] location_options = getResources().getStringArray(R.array.entries_for_server_mode);
        service.setSummary(location_options[index_of_service_type]);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        switch (preference.getKey()) {
            case Constants.PREF_SERVICE_TYPE:
                updateServiceSummary(o.toString());
                break;
        }
        return true;
    }
}
