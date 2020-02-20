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
import android.support.v4.widget.SwipeRefreshLayout;
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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.LogBuff;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.adapters.PlayingExpandableAdapter;
import io.alcatraz.audiohq.beans.AppListBean;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.CheckUtils;
import io.alcatraz.audiohq.core.utils.ShellUtils;
import io.alcatraz.audiohq.extended.CompatWithPipeActivity;
import io.alcatraz.audiohq.services.AudiohqJavaServer;
import io.alcatraz.audiohq.utils.InstallUtils;
import io.alcatraz.audiohq.utils.NativeServerControl;
import io.alcatraz.audiohq.utils.PackageCtlUtils;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.ShellDataBridge;
import io.alcatraz.audiohq.utils.Utils;

public class MainActivity extends CompatWithPipeActivity {
    Toolbar toolbar;
    TabLayout tl_main;
    ViewPager viewPager;

    //Playing panel
    PlayingExpandableAdapter playingExpandableAdapter;
    ExpandableListView playing_list;
    Map<String, AppListBean> playing_data = new HashMap<>();
    //SwipeRefreshLayout playing_refresh;

    //Status panel
    ImageView daemon_status_indicator;
    TextView daemon_status;
    LinearLayout daemon_status_back;

    //Preset Panel
    CardView preset_disabled_panel;
    Button preset_apply;
    List<View> preset_widgets = new LinkedList<>();

    //Console out
    TextView console;
    SwipeRefreshLayout console_refresh;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        vpd.add(initConsolePanel());
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
                        updateConsole();
                        break;
                    case 3:
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
//        playing_refresh = root.findViewById(R.id.app_list_refresh);
//        Utils.setupSRL(playing_refresh);
//        playing_refresh.setOnRefreshListener(this::updatePlayingData);
        playingExpandableAdapter = new PlayingExpandableAdapter(this, playing_data, new AsyncInterface() {
            @Override
            public boolean onAyncDone(@Nullable Object val) {
                updatePlayingData();
                return true;
            }

            @Override
            public void onFailure(String reason) {

            }
        });
        playing_list.setAdapter(playingExpandableAdapter);
//        updatePlayingData();
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

    private void updateConsole() {
        console_refresh.setRefreshing(true);
        console.setText(LogBuff.getFinalLog());
        console_refresh.setRefreshing(false);
    }

    private View initConsolePanel() {
        @SuppressLint("InflateParams") View root = getLayoutInflater().inflate(R.layout.panel_console, null);
        console = root.findViewById(R.id.console_text);
        console_refresh = root.findViewById(R.id.console_refresh);
        Utils.setupSRL(console_refresh);
        console_refresh.setOnRefreshListener(this::updateConsole);
        return root;
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
                ShellUtils.execCommand("ps -A -o PID -o CMDLINE | grep -v \"PID NAME\" | grep \"audiohq --daemon\" | grep -v \"grep\"", true);
        if (result.responseMsg != null && result.responseMsg.contains("audiohq --daemon")) {
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
        playing_data.clear();
        ArrayList<View> dialog_widgets = new ArrayList<>();
        AlertDialog alertDialog = Utils.getProcessingDialog(this, dialog_widgets, false, true);
        alertDialog.show();
        ShellDataBridge.getPlayingMap(this, new AsyncInterface<Map<String, AppListBean>>() {
            @Override
            public boolean onAyncDone(@Nullable Map<String, AppListBean> val) {
                if (val != null) {
                    playing_data.putAll(val);
                }
                runOnUiThread(() -> {
                    playingExpandableAdapter.notifyDataSetChanged();
                    alertDialog.dismiss();
//                    playing_refresh.setRefreshing(false);
                });
                return true;
            }

            @Override
            public void onFailure(String reason) {

            }
        }, dialog_widgets);

    }

    @Override
    public void onReloadPreferenceDone() {
        super.onReloadPreferenceDone();
        invalidateOptionsMenu();
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
                        updateConsole();
                        break;
                    case 3:
                        updateStatus();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
