package io.alcatraz.audiohq.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.PrecisePanelBridge;
import io.alcatraz.audiohq.beans.nativebuffers.Buffers;
import io.alcatraz.audiohq.beans.nativebuffers.Processes;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.services.AHQProtector;
import io.alcatraz.audiohq.services.FloatPanelService;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.SharedPreferenceUtil;
import io.alcatraz.audiohq.utils.UpdateUtils;

public class PrefFragment extends PreferenceFragment {
    //Preference chapter background
    SwitchPreference boot;
    CheckBoxPreference float_service;

    //Preference chapter security
    CheckBoxPreference default_silent;
    PreferenceScreen default_profile;
    CheckBoxPreference protector_service;

    //Preference chapter othters
//    CheckBoxPreference weak_key;
    PreferenceScreen check_update;
    PreferenceScreen clear_profile;
    PreferenceScreen uninstall_profile;

    //Preference chapter float window
    ListPreference float_gravity;
    EditTextPreference float_background;
    CheckBoxPreference float_foreground_service;
    EditTextPreference float_dismiss_delay;
    EditTextPreference float_margin_top;
    EditTextPreference float_margin_top_landscape;
    EditTextPreference float_icon_tint;
    EditTextPreference float_toggle_size;
    EditTextPreference float_font_color;

    boolean default_silent_val;
    //    boolean weak_key_val;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_content);
        findPreferences();
        bindLinsteners();
        updateSummary();
        updateEditTextSummay();
    }

    public void findPreferences() {
        check_update = (PreferenceScreen) findPreference(Constants.PREF_CHECK_UPDATE);
        default_silent = (CheckBoxPreference) findPreference(Constants.PREF_DEFAULT_SILENT);
        boot = (SwitchPreference) findPreference(Constants.PREF_BOOT);
        float_service = (CheckBoxPreference) findPreference(Constants.PREF_FLOAT_SERVICE);
//        weak_key = (CheckBoxPreference) findPreference(Constants.PREF_WEAK_KEY_ADJUST);
        clear_profile = (PreferenceScreen) findPreference(Constants.PREF_CLEAR_PROFILES);
        uninstall_profile = (PreferenceScreen) findPreference(Constants.PREF_UNINSTALL_NATIVE);
        default_profile = (PreferenceScreen) findPreference(Constants.PREF_DEFAULT_PROFILE);
        protector_service = (CheckBoxPreference) findPreference(Constants.PREF_PROTECTOR);
        float_gravity = (ListPreference) findPreference(Constants.PREF_FLOAT_WINDOW_GRAVITY);
        float_background = (EditTextPreference) findPreference(Constants.PREF_FLOAT_WINDOW_BACKGROUND);
        float_foreground_service = (CheckBoxPreference) findPreference(Constants.PREF_FLOAT_FOREGROUND_SERVICE);
        float_dismiss_delay = (EditTextPreference) findPreference(Constants.PREF_FLOAT_WINDOW_DISMISS_DELAY);
        float_margin_top = (EditTextPreference) findPreference(Constants.PREF_FLOAT_WINDOW_MARGIN_TOP);
        float_margin_top_landscape = (EditTextPreference) findPreference(Constants.PREF_FLOAT_WINDOW_MARGIN_TOP_LANDSCAPE);
        float_icon_tint = (EditTextPreference) findPreference(Constants.PREF_FLOAT_WINDOW_ICON_TINT);
        float_toggle_size = (EditTextPreference) findPreference(Constants.PREF_FLOAT_WINDOW_TOGGLE_SIZE);
        float_font_color = (EditTextPreference) findPreference(Constants.PREF_FLOAT_WINDOW_FONT_COLOR);
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

        float_service.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                if (!Settings.canDrawOverlays(getContext())) {
                    Toast.makeText(getContext(), R.string.toast_cant_overlay, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName())));
                } else {
                    if ((boolean) o) {
                        getContext().startService(new Intent(getContext(), FloatPanelService.class));
                    }else {
                        getContext().stopService(new Intent(getContext(),FloatPanelService.class));
                    }
                }
                return true;
            }
        });

//        weak_key.setOnPreferenceChangeListener((preference, o) -> {
//            AudioHqApis.setWeakKeyAdjust(!weak_key_val);
//            weak_key.setChecked(!weak_key_val);
//            weak_key_val = !weak_key_val;
//            Toast.makeText(getContext(), R.string.pref_need_to_restart_playing_process, Toast.LENGTH_LONG).show();
//            return false;
//        });

        default_profile.setOnPreferenceClickListener(preference -> {
            ShellUtils.CommandResult result = AudioHqApis.getDefaultProfile();
            if (result.responseMsg != null) {

                Processes processes = new Processes();
                Buffers buffers = new Buffers();
                String[] prof = result.responseMsg.trim().split(",");
                buffers.setLeft(prof[0]);
                buffers.setRight(prof[1]);
                buffers.setFinalv(prof[2]);
                buffers.setControl_lr(prof[3].equals("1") ? "true" : "false");
                List<Buffers> b = new ArrayList<>();
                b.add(buffers);
                processes.setBuffers(b);

                Panels.getAdjustPanel(getActivity(), processes, true, false, new AsyncInterface<PrecisePanelBridge>() {
                    @Override
                    public boolean onAyncDone(@Nullable PrecisePanelBridge val) {
                        assert val != null;
                        val.getAlertDialog().dismiss();
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
            if (newVal) {
                getContext().startService(new Intent(getContext(), AHQProtector.class));
            } else {
                getContext().stopService(new Intent(getContext(), AHQProtector.class));
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

        float_gravity.setOnPreferenceChangeListener((preference, o) -> {
            SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
            spfu.put(getContext(), Constants.PREF_FLOAT_WINDOW_GRAVITY, (String) o);
            updateGravitySummary();
            return true;
        });

        float_background.setOnPreferenceChangeListener((preference, o) -> {
            String color_str = (String) o;
            try {
                Color.parseColor(color_str);
                SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
                spfu.put(getContext(), Constants.PREF_FLOAT_WINDOW_BACKGROUND, (String) o);
                updateEditTextSummay();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.pref_invalid_color, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });

        float_dismiss_delay.setOnPreferenceChangeListener((preference, o) -> {
            String delay = (String) o;
            try {
                Integer.parseInt(delay);
                SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
                spfu.put(getContext(), Constants.PREF_FLOAT_WINDOW_DISMISS_DELAY, (String) o);
                updateEditTextSummay();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.pref_invalid_integer, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });

        float_margin_top.setOnPreferenceChangeListener((preference, o) -> {
            String margin_top = (String) o;
            try {
                Integer.parseInt(margin_top);
                SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
                spfu.put(getContext(), Constants.PREF_FLOAT_WINDOW_MARGIN_TOP, (String) o);
                updateEditTextSummay();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.pref_invalid_integer, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });

        float_margin_top_landscape.setOnPreferenceChangeListener((preference, o) -> {
            String margin_top = (String) o;
            try {
                Integer.parseInt(margin_top);
                SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
                spfu.put(getContext(), Constants.PREF_FLOAT_WINDOW_MARGIN_TOP_LANDSCAPE, (String) o);
                updateEditTextSummay();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.pref_invalid_integer, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });

        float_icon_tint.setOnPreferenceChangeListener((preference, o) -> {
            String color_str = (String) o;
            try {
                Color.parseColor(color_str);
                SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
                spfu.put(getContext(), Constants.PREF_FLOAT_WINDOW_ICON_TINT, (String) o);
                updateEditTextSummay();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.pref_invalid_color, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });

        float_toggle_size.setOnPreferenceChangeListener((preference, o) -> {
            String delay = (String) o;
            try {
                Integer.parseInt(delay);
                SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
                spfu.put(getContext(), Constants.PREF_FLOAT_WINDOW_TOGGLE_SIZE, (String) o);
                updateEditTextSummay();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.pref_invalid_integer, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });

        float_font_color.setOnPreferenceChangeListener((preference, o) -> {
            String color_str = (String) o;
            try {
                Color.parseColor(color_str);
                SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
                spfu.put(getContext(), Constants.PREF_FLOAT_WINDOW_FONT_COLOR, (String) o);
                updateEditTextSummay();
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.pref_invalid_color, Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        });
    }

    public void updateSummary() {
        ShellUtils.CommandResult switches = AudioHqApis.getSwitches();
        if (switches.responseMsg != null) {
            String[] switches_str = switches.responseMsg.split(";");
            default_silent_val = switches_str[1].equals("true");
            default_silent.setChecked(default_silent_val);
//            weak_key_val = switches_str[2].equals("true");
//            weak_key.setChecked(weak_key_val);
        }
        updateGravitySummary();
    }

    public void updateGravitySummary() {
        SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
        String gravity = (String) spfu.get(getContext(), Constants.PREF_FLOAT_WINDOW_GRAVITY,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_GRAVITY);
        String[] entryvalue = getContext().getResources().getStringArray(R.array.entryvalue_for_float_gravity);
        String[] entry = getContext().getResources().getStringArray(R.array.entries_for_float_gravity);
        for (int i=0;i<entryvalue.length;i++){
            if(entryvalue[i].equals(gravity)){
                float_gravity.setSummary(entry[i]);
            }
        }

    }

    public void updateEditTextSummay(){
        SharedPreferenceUtil spfu = SharedPreferenceUtil.getInstance();
        String f_b_color = (String) spfu.get(getContext(),Constants.PREF_FLOAT_WINDOW_BACKGROUND,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_BACKGROUND);
        String delay = (String) spfu.get(getContext(),Constants.PREF_FLOAT_WINDOW_DISMISS_DELAY,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_DISMISS_DELAY);
        String m_top = (String) spfu.get(getContext(),Constants.PREF_FLOAT_WINDOW_MARGIN_TOP,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_MARGIN_TOP);
        String m_top_l = (String) spfu.get(getContext(),Constants.PREF_FLOAT_WINDOW_MARGIN_TOP_LANDSCAPE,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_MARGIN_TOP_LANDSCAPE);
        String ic_tint = (String) spfu.get(getContext(),Constants.PREF_FLOAT_WINDOW_ICON_TINT,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_ICON_TINT);
        String tg_size = (String) spfu.get(getContext(),Constants.PREF_FLOAT_WINDOW_TOGGLE_SIZE,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_TOGGLE_SIZE);
        String f_color = (String) spfu.get(getContext(),Constants.PREF_FLOAT_WINDOW_FONT_COLOR,
                Constants.DEFAULT_VALUE_PREF_FLOAT_WINDOW_FONT_COLOR);

        float_background.setSummary(f_b_color);
        float_dismiss_delay.setSummary(delay);
        float_margin_top.setSummary(m_top);
        float_margin_top_landscape.setSummary(m_top_l);
        float_icon_tint.setSummary(ic_tint);
        float_toggle_size.setSummary(tg_size);
        float_font_color.setSummary(f_color);
    }
}
