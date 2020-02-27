package io.alcatraz.audiohq.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alcatraz.support.v4.appcompat.DrawerLayoutUtil;
import com.alcatraz.support.v4.appcompat.ViewPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.adapters.PlayingExpandableAdapter;
import io.alcatraz.audiohq.adapters.PlyPkgExpandableAdapter;
import io.alcatraz.audiohq.beans.nativebuffers.PackageBuffers;
import io.alcatraz.audiohq.beans.nativebuffers.ProcessBuffers;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.CheckUtils;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.extended.CompatWithPipeActivity;
import io.alcatraz.audiohq.services.AHQProtector;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.ShellDataBridge;
import io.alcatraz.audiohq.utils.Utils;

public class MainActivity extends CompatWithPipeActivity {
    Toolbar toolbar;
    TabLayout tl_main;
    ViewPager viewPager;

    //Playing panel
    ExpandableListView playing_list;
    PlayingExpandableAdapter playingExpandableAdapter;
    ProcessBuffers processBuffers = new ProcessBuffers();

    PlyPkgExpandableAdapter plyPkgExpandableAdapter;
    PackageBuffers packageBuffers = new PackageBuffers();

    //Status panel
    ImageView daemon_status_indicator;
    TextView daemon_status;
    LinearLayout daemon_status_back;

    //Preset Panel
    CardView preset_disabled_panel;
    Button preset_apply;
    List<View> preset_widgets = new LinkedList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkAndProtectService();
        setContentView(R.layout.activity_main);
        if (!CheckUtils.getRootStatus()) {
            toast(R.string.toast_no_root);
        }
        initViews();
    }

    private void findViews() {
        toolbar = findViewById(R.id.toolbar);
        tl_main = findViewById(R.id.main_tabs);
        viewPager = findViewById(R.id.main_pager);
    }

    private void initViews() {
        findViews();
        setSupportActionBar(toolbar);
        DrawerLayoutUtil.Immersive(toolbar, true, this);

        ArrayList<View> vpd = new ArrayList<>();

        if (!CheckUtils.getMagiskInstalled(this))
            new AlertDialog.Builder(this).setTitle(R.string.install_fail_title)
                    .setMessage(R.string.install_magisk_not_installed)
                    .setNegativeButton(R.string.ad_nb, null)
                    .show();
        vpd.add(initPlayingList());
        vpd.add(initPresetPanel());
        vpd.add(initStatusPanel());

        List<String> t = Arrays.asList(getResources().getStringArray(R.array.main_tabs));

        ViewPagerAdapter vpa = new ViewPagerAdapter(vpd, t);
        viewPager.setAdapter(vpa);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 1:
                        updatePresetPanel();
                        break;
                    case 2:
                        updateStatus();
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        tl_main.setupWithViewPager(viewPager);
    }

    private View initPlayingList() {
        @SuppressLint("InflateParams") View root = getLayoutInflater().inflate(R.layout.app_list_panel, null);
        playing_list = root.findViewById(R.id.app_list_expl);
        playingExpandableAdapter = new PlayingExpandableAdapter(this, processBuffers);
        plyPkgExpandableAdapter = new PlyPkgExpandableAdapter(this, packageBuffers);
        return root;
    }

    @Override
    protected void onStart() {
        super.onStart();
        updatePlayingData();
    }

    private View initPresetPanel() {
        View view = Panels.getPresetPanel(this, preset_widgets);
        preset_disabled_panel = view.findViewById(R.id.preset_disabled_panel);
        preset_apply = view.findViewById(R.id.adjust_apply);
        return view;
    }

    private void updatePresetPanel() {
        Utils.setViewsEnabled(preset_widgets, true);
        preset_disabled_panel.setVisibility(View.GONE);
    }

    private View initStatusPanel() {
        View root = Panels.getCheckPanel(this);
        daemon_status = root.findViewById(R.id.check_daemon_status);
        daemon_status_back = root.findViewById(R.id.check_daemon_status_back);
        daemon_status_indicator = root.findViewById(R.id.check_daemon_status_indicator);
        return root;
    }

    private void updateStatus() {
        ShellUtils.CommandResult result =
                ShellUtils.execCommand("ps -A -o PID -o CMDLINE | grep -v \"PID NAME\" | grep \"audiohq --service\" | grep -v \"grep\"", true);
        if (result.responseMsg != null && result.responseMsg.contains("audiohq --service")) {
            daemon_status.setText(R.string.check_daemon_status_alive);
            daemon_status_back.setBackgroundColor(getResources().getColor(R.color.green_colorPrimary));
            daemon_status_indicator.setImageResource(R.drawable.ic_check);
        } else {
            daemon_status.setText(R.string.check_daemon_status_dead);
            daemon_status_back.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            daemon_status_indicator.setImageResource(R.drawable.ic_alert);
        }
    }

    private void updatePlayingData() {
        ArrayList<View> dialog_widgets = new ArrayList<>();
        AlertDialog alertDialog = Utils.getProcessingDialog(this, dialog_widgets, false, true);
        alertDialog.show();

        ShellUtils.CommandResult switches = AudioHqApis.getSwitches();
        if (switches.responseMsg != null) {
            String[] switches_str = switches.responseMsg.split(";");
            boolean isweakkey = switches_str[2].equals("true");
            if(fold_same_pkg || isweakkey){
                ShellDataBridge.getPackageBuffers(new AsyncInterface<PackageBuffers>() {
                    @Override
                    public boolean onAyncDone(@Nullable PackageBuffers val) {
                        runOnUiThread(() -> {
                            packageBuffers = (val == null ? new PackageBuffers() : val);
                            plyPkgExpandableAdapter.setNewData(packageBuffers);
                            plyPkgExpandableAdapter.setWeakkey(isweakkey);
                            playing_list.setAdapter(plyPkgExpandableAdapter);
                            alertDialog.dismiss();
                        });
                        return false;
                    }

                    @Override
                    public void onFailure(String reason) {
                        runOnUiThread(() -> toast(reason));
                    }
                });
            }else {
                ShellDataBridge.getProcessBuffers(new AsyncInterface<ProcessBuffers>() {
                    @Override
                    public boolean onAyncDone(@Nullable ProcessBuffers val) {
                        runOnUiThread(() -> {
                            processBuffers = (val == null ? new ProcessBuffers() : val);
                            playingExpandableAdapter.setNewData(processBuffers);
                            playing_list.setAdapter(playingExpandableAdapter);
                            alertDialog.dismiss();
                        });
                        return false;
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                });
            }
        }
    }

    @Override
    public void onReloadPreferenceDone() {
        super.onReloadPreferenceDone();
        invalidateOptionsMenu();
        updatePlayingData();
    }

    public List<PackageInfo> getAppList() {
        PackageManager pm = getPackageManager();
        // Return a List of all packages that are installed on the device.
        return pm.getInstalledPackages(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater mi = new MenuInflater(this);
        mi.inflate(R.menu.activity_main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item1:
                startActivity(new Intent(this, PreferenceActivity.class));
                break;
            case R.id.item2:
                startActivity(new Intent(this, AboutActivity.class));
                break;
            case R.id.item3:
                switch (viewPager.getCurrentItem()) {
                    case 0:
                        updatePlayingData();
                        break;
                    case 1:
                        updatePresetPanel();
                        break;
                    case 2:
                        updateStatus();
                        break;
                }
                break;
            case R.id.item4:
                Panels.getLogConsole(this).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndProtectService(){
        AudioHqApis.startNativeService();
        if(protector_service){
            startService(new Intent(this, AHQProtector.class));
        }
    }
}
