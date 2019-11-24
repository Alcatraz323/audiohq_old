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
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.CheckUtils;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.utils.InstallUtils;
import io.alcatraz.audiohq.utils.SharedPreferenceUtil;
import io.alcatraz.audiohq.utils.UpdateUtils;

public class PrefFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    ListPreference service;
    PreferenceScreen check_update;
    CheckBoxPreference default_silent;
    SwitchPreference boot;
    SwitchPreference modified_rc;
    PreferenceScreen clear_profile;
    PreferenceScreen uninstall_profile;

    boolean default_silent_val;
    boolean modified_rc_val;

    SharedPreferenceUtil spf;

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
        boot = (SwitchPreference) findPreference(Constants.PREF_BOOT);
        clear_profile = (PreferenceScreen) findPreference(Constants.PREF_CLEAR_PROFILES);
        uninstall_profile = (PreferenceScreen) findPreference(Constants.PREF_UNINSTALL_NATIVE);
        modified_rc = (SwitchPreference) findPreference(Constants.PREF_MODIFY_RC);
    }

    @SuppressLint("DefaultLocale")
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
        modified_rc.setOnPreferenceChangeListener((preference, o) -> {
            if (!modified_rc_val) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.pref_default_silent_warning_title)
                        .setMessage(R.string.setup_4_modify_rc_warning)
                        .setNegativeButton(R.string.ad_nb, null)
                        .setPositiveButton(R.string.adjust_confirm, (dialogInterface, i) ->
                                InstallUtils.modifyRCFile(!modified_rc_val, new AsyncInterface<ShellUtils.CommandResult>() {
                                    @Override
                                    public boolean onAyncDone(@Nullable ShellUtils.CommandResult val) {
                                        assert val != null;
                                        if (val.result < 0 || val.errorMsg.length() > 0) {
                                            new android.support.v7.app.AlertDialog.Builder(getContext())
                                                    .setTitle(R.string.setup_check_deny)
                                                    .setMessage(String.format("result = %d\nerr = %s", val.result, val.errorMsg))
                                                    .setNegativeButton(R.string.ad_nb, null)
                                                    .show();
                                            return false;   //Prevent reboot
                                        } else {
                                            modified_rc.setChecked(!modified_rc_val);
                                            modified_rc_val = !modified_rc_val;
                                            spf.put(getContext(), Constants.PREF_BOOT, false);
                                            spf.put(getContext(), Constants.PREF_SERVICE_TYPE, AudioHqApis.AUDIOHQ_SERVER_NONE);
                                            AudioHqApis.setReadProcState(true);
                                            updateSummary();
                                        }
                                        return true;
                                    }

                                    @Override
                                    public void onFailure(String reason) {

                                    }
                                })).show();
                return false;
            }

            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.pref_default_silent_warning_title)
                    .setMessage(R.string.setup_4_modify_rc_warning)
                    .setNegativeButton(R.string.ad_nb, null)
                    .setPositiveButton(R.string.adjust_confirm, (dialogInterface, i) ->
                            InstallUtils.modifyRCFile(false, new AsyncInterface<ShellUtils.CommandResult>() {
                                @Override
                                public boolean onAyncDone(@Nullable ShellUtils.CommandResult val) {
                                    assert val != null;
                                    if (val.result < 0 || val.errorMsg.length() > 0) {
                                        new android.support.v7.app.AlertDialog.Builder(getContext())
                                                .setTitle(R.string.setup_check_deny)
                                                .setMessage(String.format("result = %d\nerr = %s", val.result, val.errorMsg))
                                                .setNegativeButton(R.string.ad_nb, null)
                                                .show();
                                        return false;   //Prevent reboot
                                    } else {
                                        modified_rc.setChecked(!modified_rc_val);
                                        modified_rc_val = !modified_rc_val;
                                        AudioHqApis.setReadProcState(false);
                                        updateSummary();
                                    }
                                    return true;
                                }

                                @Override
                                public void onFailure(String reason) {

                                }
                            })).show();
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
        default_silent_val = AudioHqApis.getDefaultSilentState().responseMsg.contains("true");
        default_silent.setChecked(default_silent_val);
        modified_rc_val = CheckUtils.hasModifiedRC();
        modified_rc.setChecked(modified_rc_val);

        spf = SharedPreferenceUtil.getInstance();
        String service_type = (String) spf.get(getActivity(), Constants.PREF_SERVICE_TYPE, Constants.DEFAULT_VALUE_PREF_SERVICE);
        String[] service_types = getResources().getStringArray(R.array.entryvalues_for_server_mode);
        int index_of_service_type = Arrays.asList(service_types).indexOf(service_type);
        String[] location_options = getResources().getStringArray(R.array.entries_for_server_mode);
        service.setSummary(location_options[index_of_service_type]);
        service.setEnabled(!modified_rc_val);
        boot.setEnabled(!modified_rc_val && !AudioHqApis.AUDIOHQ_SERVER_NONE.equals(service_type));
        if (AudioHqApis.AUDIOHQ_SERVER_NONE.equals(service_type)) {
            boot.setChecked(false);
        }
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
                updateSummary();
                break;
        }
        return true;
    }
}
