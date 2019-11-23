package io.alcatraz.audiohq.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.LambdaBridge;
import io.alcatraz.audiohq.beans.SetupPage;
import io.alcatraz.audiohq.core.utils.CheckUtils;
import io.alcatraz.audiohq.core.utils.OSUtils;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.extended.SetupWizardBaseActivity;
import io.alcatraz.audiohq.utils.AnimateUtils;
import io.alcatraz.audiohq.utils.InstallUtils;
import io.alcatraz.audiohq.utils.Utils;

public class SetupActivity extends SetupWizardBaseActivity {
    @Override
    public void onSetupPageInit(List<SetupPage> pages) {
        String[] setup_titles = getResources().getStringArray(R.array.setup_page_titles);
        int[] page_layout_ids = {R.layout.setup_1, R.layout.setup_2, R.layout.setup_3,
                R.layout.setup_4, R.layout.setup_5, R.layout.setup_6};

        for (int i = 0; i < setup_titles.length; i++) {
            SetupPage page = new SetupPage(setup_titles[i], page_layout_ids[i]);
            pages.add(page);
        }

        getPager().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 1:
                        onSelectSetup2();
                        break;
                    case 2:
                        onSelectSetup3();
                        break;
                    case 3:
                        onSelectSetup4();
                        break;
                    case 4:
                        onSelectSetup5_Apply();
                        break;
                    case 5:
                        banForwardStep();
                        break;
                    default:
                        restoreState();
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    @Override
    public void onUpdate(List<SetupPage> pages) {
        SetupPage page = new SetupPage(getResources().getString(R.string.setup_current_update), R.layout.setup_7);
        pages.add(page);
    }

    @Override
    public void onFinishSetup() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public int getVersionCode() {
        return 3;
    }

    private void onSelectSetup5_Apply() {
        startPending();

        View root_view = getPageList().get(3).getRootView();
        Spinner server_type = root_view.findViewById(R.id.setup_4_server_type);

        getSpf().put(this, Constants.PREF_SERVICE_TYPE,
                server_type.getSelectedItemPosition() == 4 ? 256 + "" : server_type.getSelectedItemPosition() + "");
        sendBroadcast(new Intent().setAction(Constants.BROADCAST_ACTION_UPDATE_PREFERENCES));

        new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                endPending();
                getPager().setCurrentItem(getPager().getCurrentItem() + 1);
            });
        }).start();
    }

    private void onSelectSetup4() {
        startPending();

        View root_view = getPageList().get(3).getRootView();

        CardView modify_rc_card = root_view.findViewById(R.id.setup_4_modify_rc_card);
        TextView detected_rc = root_view.findViewById(R.id.setup_4_rc_detected);
        CheckBox modify_rc_check = root_view.findViewById(R.id.setup_4_modify_rc_check);
        Spinner server_type = root_view.findViewById(R.id.setup_4_server_type);

        final LambdaBridge<Boolean> has_modified_rc = new LambdaBridge<>();
        has_modified_rc.setTarget(CheckUtils.hasModifiedRC());

        modify_rc_check.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                server_type.setSelection(4);
                server_type.setEnabled(false);
            } else {
                server_type.setSelection(0);
                server_type.setEnabled(true);
            }
        });

        modify_rc_card.setOnClickListener(view -> new AlertDialog.Builder(SetupActivity.this)
                .setTitle(R.string.pref_default_silent_warning_title)
                .setMessage(R.string.setup_4_modify_rc_warning)
                .setNegativeButton(R.string.ad_nb, null)
                .setPositiveButton(R.string.adjust_confirm, (dialogInterface, i) -> {
                    ShellUtils.CommandResult result = InstallUtils.modifyRCFile(!has_modified_rc.getTarget());
                    if (result.result < 0 || result.errorMsg.length() > 0) {
                        new AlertDialog.Builder(SetupActivity.this)
                                .setTitle(R.string.setup_check_deny)
                                .setMessage(String.format("result = %d\nerr = %s", result.result, result.errorMsg))
                                .setNegativeButton(R.string.ad_nb, null)
                                .show();
                    } else {
                        has_modified_rc.setTarget(!has_modified_rc.getTarget());
                        modify_rc_check.setChecked(has_modified_rc.getTarget());
                    }
                })
                .show());
        //Initial state setup
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.entries_for_server_mode));
        server_type.setAdapter(adapter);

        modify_rc_check.setChecked(has_modified_rc.getTarget());

        detected_rc.setText(String.format(getResources().getString(R.string.setup_3_installed_detected),
                getResources().getString(has_modified_rc.getTarget() ?
                        R.string.setup_4_modify_rc_already : R.string.setup_4_modify_rc_not_yet)));

        endPending();
    }

    private void onSelectSetup3() {
        startPending();

        View root_view = getPageList().get(2).getRootView();

        Button btn_go_website = root_view.findViewById(R.id.setup_3_go_website);
        TextView detected_version = root_view.findViewById(R.id.setup_3_detected);
        CheckBox installed = root_view.findViewById(R.id.setup_3_installed);

        //Initial state setup
        installed.setChecked(false);

        String current_version = CheckUtils.getLibVersion();
        detected_version.setText(String.format(getResources().getString(R.string.setup_3_installed_detected), current_version));

        if (!getString(R.string.support_lib_version).equals(current_version))
            detected_version.setTextColor(getResources().getColor(android.R.color.holo_red_light, null));

        endPending();
        banNextStep();

        btn_go_website.setOnClickListener(view -> startActivity(new Intent(Intent.ACTION_VIEW,
                Uri.parse("https://alcatraz323.github.io/audiohq"))));
        installed.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                restoreState();
            } else {
                banNextStep();
            }
        });
    }

    @SuppressLint("SetTextI18n")
    private void onSelectSetup2() {
        boolean can_go_next = true;
        int color_red = getResources().getColor(android.R.color.holo_red_light, null);

        startPending();

        //Find views
        View root_view = getPageList().get(1).getRootView();

        CardView vendor_warning = root_view.findViewById(R.id.setup_2_vendor_system_warning);
        CardView root_check = root_view.findViewById(R.id.setup_2_root_check);
        CardView audioserver_check = root_view.findViewById(R.id.setup_2_audioserver_check);
        CardView api_check = root_view.findViewById(R.id.setup_2_api_check);
        CardView requirements_not_meet = root_view.findViewById(R.id.setup_2_requirements_not_meet);

        TextView vendor_warning_title = root_view.findViewById(R.id.setup_2_warning_title);
        Button vendor_unlock = root_view.findViewById(R.id.setup_2_vendor_unlock);
        Button requirement_unlock = root_view.findViewById(R.id.setup_2_requirements_not_meet_unlock);

        TextView root_check_title = root_view.findViewById(R.id.setup_2_root_check_title);
        TextView root_check_state = root_view.findViewById(R.id.setup_2_root_check_state);
        ImageView root_check_indicator = root_view.findViewById(R.id.setup_2_root_check_indicator);

        TextView audioserver_check_title = root_view.findViewById(R.id.setup_2_audioserver_check_title);
        TextView audioserver_check_state = root_view.findViewById(R.id.setup_2_audioserver_check_state);
        ImageView audioserver_check_indicator = root_view.findViewById(R.id.setup_2_audioserver_check_indicator);

        TextView api_check_title = root_view.findViewById(R.id.setup_2_api_check_title);
        TextView api_check_state = root_view.findViewById(R.id.setup_2_api_check_state);
        ImageView api_check_indicator = root_view.findViewById(R.id.setup_2_api_check_indicator);

        //Initial state setup
        vendor_warning.setVisibility(View.GONE);
        root_check.setVisibility(View.GONE);
        audioserver_check.setVisibility(View.GONE);
        api_check.setVisibility(View.GONE);
        requirements_not_meet.setVisibility(View.GONE);

        OSUtils.ROM_TYPE rom = OSUtils.getRomType();
        if (rom != OSUtils.ROM_TYPE.OTHER) {
            AnimateUtils.playstart(vendor_warning, () -> {
            });
            can_go_next = false;
        }

        if (!ShellUtils.hasRootPermission()) {
            root_check_title.setTextColor(color_red);
            root_check_state.setText(R.string.setup_check_deny);
            Utils.setImageWithTint(root_check_indicator, R.drawable.ic_close, color_red);
            can_go_next = false;
        }
        AnimateUtils.playstart(root_check, () -> {
        });

        String audioserver_info = CheckUtils.getAudioServerInfo();
        if (audioserver_info != null) {
            if (audioserver_info.split("dynamic")[0].contains("64") && Build.VERSION.SDK_INT == 28) {
                audioserver_check_title.setTextColor(color_red);
                Utils.setImageWithTint(audioserver_check_indicator, R.drawable.ic_close, color_red);
                can_go_next = false;
            }
            String audioserver_info_processed[] = audioserver_info.split(":")[1].split(",");
            audioserver_check_state.setText(audioserver_info_processed[0] + "," + audioserver_info_processed[1]);
            AnimateUtils.playstart(audioserver_check, () -> {
            });
        } else {
            audioserver_check_title.setTextColor(color_red);
            Utils.setImageWithTint(audioserver_check_indicator, R.drawable.ic_close, color_red);
            can_go_next = false;
            audioserver_check_state.setText(R.string.setup_check_deny);
            AnimateUtils.playstart(audioserver_check, () -> {
            });
        }

        if (!CheckUtils.getIfSupported()) {
            api_check_title.setTextColor(color_red);
            Utils.setImageWithTint(api_check_indicator, R.drawable.ic_close, color_red);
            can_go_next = false;
        }
        api_check_state.setText("Api:" + Build.VERSION.SDK_INT +
                "(" + Utils.extractStringArr(CheckUtils.getSupportArch()) + ")");
        AnimateUtils.playstart(api_check, () -> {
        });

        endPending();

        if (!can_go_next) {
            AnimateUtils.playstart(requirements_not_meet, () -> {
            });
            restoreState();
            banNextStep();
        }

        vendor_warning_title.setText(String.format(getResources().getString(R.string.setup_2_warning_vendor_system_title),
                OSUtils.getRomType().name()));
        vendor_unlock.setOnClickListener(view -> {
            AnimateUtils.playEnd(vendor_warning/*, () -> {}*/);
            restoreState();
        });
        requirement_unlock.setOnClickListener(view -> {
            AnimateUtils.playEnd(requirements_not_meet/*, () -> {}*/);
            restoreState();
        });
    }
}
