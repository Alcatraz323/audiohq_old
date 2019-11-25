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
import io.alcatraz.audiohq.beans.ServerStatus;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.CheckUtils;
import io.alcatraz.audiohq.extended.CompatWithPipeActivity;
import io.alcatraz.audiohq.utils.InstallUtils;
import io.alcatraz.audiohq.utils.NativeServerControl;
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
    List<TextView> status_txvs = new LinkedList<>();
    List<ImageView> status_imgvs = new LinkedList<>();
    SwipeRefreshLayout status_refresh;

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
        vpd.add(initStatusPanel());
        vpd.add(initConsolePanel());
        vpd.add(Panels.getCheckPanel(this));

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
                    case 3:
                        updateConsole();
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

    private View initStatusPanel() {
        View view = Panels.getStatusPanel(this, status_txvs, status_imgvs);
        status_refresh = view.findViewById(R.id.status_refresh);
        Utils.setupSRL(status_refresh);
        status_refresh.setOnRefreshListener(this::updateStatus);
        return view;
    }

    private View initPresetPanel() {
        View view = Panels.getPresetPanel(this, preset_widgets);
        preset_disabled_panel = view.findViewById(R.id.preset_disabled_panel);
        preset_apply = view.findViewById(R.id.adjust_apply);
        return view;
    }

    private void updatePresetPanel() {
        Utils.setViewsEnabled(preset_widgets, false);
        preset_disabled_panel.setVisibility(View.VISIBLE);
        ServerStatus.setUpdatePending(true);
        new Thread(ServerStatus::updateStatus).start();

        ServerStatus.requestForPending(() -> runOnUiThread(() -> {
            if (ServerStatus.isServerRunning() || CheckUtils.hasModifiedRC()) {
                preset_apply.setEnabled(true);
                preset_disabled_panel.setVisibility(View.GONE);
                Utils.setViewsEnabled(preset_widgets, true);
            } else {
                preset_disabled_panel.setVisibility(View.VISIBLE);
            }
        }));
    }

    private void updateStatus() {
        status_refresh.setRefreshing(true);
        AlertDialog alertDialog = Utils.getProcessingDialog(this, new ArrayList<>(), false, false);
        alertDialog.show();
        new Thread(() -> {
            String server = AudioHqApis.getRunningServerType().responseMsg;
            String autoremove = AudioHqApis.getAutoRemoveFlag().responseMsg;
            String track_ctl_position = AudioHqApis.getTrackCtrlPosition().responseMsg;
            String track_ctl_lock = AudioHqApis.getTrackCtrlLockState().responseMsg;
            String threads = AudioHqApis.getPlaybackThreadsCount().responseMsg;

            InstallUtils.checkAndShowInstallation(this);

            runOnUiThread(() -> {
                status_txvs.get(0).setText(server);
                status_txvs.get(1).setText(autoremove);
                status_txvs.get(2).setText(track_ctl_position);
                status_txvs.get(3).setText(track_ctl_lock);
                status_txvs.get(4).setText(threads);

                int server_indicator_image, server_indicator_color;
                int lock_image, lock_color;
                if (server.contains("running")) {
                    server_indicator_image = R.drawable.ic_alert;
                    server_indicator_color = getResources().getColor(R.color.orange_colorPrimary);
                } else if (!server.equals(track_ctl_lock)) {
                    server_indicator_image = R.drawable.ic_close;
                    server_indicator_color = getResources().getColor(android.R.color.holo_red_light);
                    lock_image = R.drawable.ic_close;
                    lock_color = getResources().getColor(android.R.color.holo_red_light);
                } else {
                    server_indicator_image = R.drawable.ic_check;
                    server_indicator_color = getResources().getColor(R.color.green_colorPrimary);
                }

                if (track_ctl_lock.contains("0")) {
                    lock_image = R.drawable.ic_alert;
                    lock_color = getResources().getColor(R.color.orange_colorPrimary);
                } else {
                    lock_image = R.drawable.ic_check;
                    lock_color = getResources().getColor(R.color.green_colorPrimary);
                }

                if (service_type.equals(AudioHqApis.AUDIOHQ_SERVER_NONE)) {
                    lock_image = R.drawable.ic_check;
                    lock_color = getResources().getColor(R.color.green_colorPrimary);
                    server_indicator_image = R.drawable.ic_check;
                    server_indicator_color = getResources().getColor(R.color.green_colorPrimary);
                }
                Utils.setImageWithTint(status_imgvs.get(0), server_indicator_image, server_indicator_color);
                Utils.setImageWithTint(status_imgvs.get(1), lock_image, lock_color);
                status_refresh.setRefreshing(false);
                alertDialog.dismiss();
            });
        }).start();
    }

    private void updateConsole() {
        console_refresh.setRefreshing(true);
        console.setText(LogBuff.getLog());
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

    private void updatePlayingData() {
        playing_data.clear();
        AlertDialog alertDialog = Utils.getProcessingDialog(this, new ArrayList<>(), false, false);
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
        });

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
                if (!CheckUtils.hasModifiedRC())
                    NativeServerControl.startServer(this);
                else
                    toast(R.string.rc_modified_server_noneed);
                break;
            case R.id.item4:
                if (!CheckUtils.hasModifiedRC())
                    NativeServerControl.stopServer(this);
                else
                    toast(R.string.rc_modified_server_noneed);
                break;
            case R.id.item5:
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
                    case 3:
                        updateConsole();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
