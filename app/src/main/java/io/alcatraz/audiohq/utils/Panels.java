package io.alcatraz.audiohq.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.support.design.widget.TextInputLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.AppListBean;
import io.alcatraz.audiohq.beans.ServerStatus;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.core.utils.CheckUtils;

public class Panels {

    @SuppressLint("SetTextI18n")
    public static View getCheckPanel(Activity activity) {
        LayoutInflater lf = activity.getLayoutInflater();
        @SuppressLint("InflateParams") View view = lf.inflate(R.layout.check_panel, null);

        TextView root = view.findViewById(R.id.check_root);
        TextView selinux = view.findViewById(R.id.check_selinux);
        TextView supported = view.findViewById(R.id.check_supported);
        TextView elf_info = view.findViewById(R.id.check_elf_info);
        TextView lib_info = view.findViewById(R.id.check_lib_info);
        TextView audioserver_info = view.findViewById(R.id.check_audioserver_info);
        ImageView support_indicator = view.findViewById(R.id.check_support_indicator);

        boolean support = CheckUtils.getIfSupported();

        root.setText(CheckUtils.getRootStatus() + "");
        selinux.setText(CheckUtils.getSeLinuxEnforce());
        supported.setText(CheckUtils.getIfSupported() + " (Api " + Build.VERSION.SDK_INT + " - "
                + Utils.extractStringArr(CheckUtils.getSupportArch()) + ")");

        elf_info.setText(AudioHqApis.getAudioHqNativeInfo().responseMsg);
        lib_info.setText(AudioHqApis.getAudioFlingerInfo().responseMsg);
        audioserver_info.setText(CheckUtils.getAudioServerInfo());

        if (support)
            Utils.setImageWithTint(support_indicator, R.drawable.ic_check, activity.getResources().getColor(R.color.green_colorPrimary));
        else
            Utils.setImageWithTint(support_indicator, R.drawable.ic_close, activity.getResources().getColor(android.R.color.holo_red_light));
        return view;
    }

    @SuppressLint("SetTextI18n")
    public static View getStatusPanel(Activity activity, List<TextView> textViews, List<ImageView> imgvs) {
        textViews.clear();
        imgvs.clear();
        LayoutInflater lf = activity.getLayoutInflater();
        @SuppressLint("InflateParams") View view = lf.inflate(R.layout.status_panel, null);

        TextView server = view.findViewById(R.id.status_server);
        TextView auto_remove = view.findViewById(R.id.status_auto_remove);
        TextView trackctl_position = view.findViewById(R.id.status_track_ctl_position);
        TextView trackctl_lock = view.findViewById(R.id.status_track_ctl_lock_type);
        TextView existing_threads = view.findViewById(R.id.status_exitsing_threads);
        ImageView server_indiator = view.findViewById(R.id.status_server_indicator);
        ImageView lock_type_indicator = view.findViewById(R.id.status_lock_type_indicator);

        textViews.add(server);
        textViews.add(auto_remove);
        textViews.add(trackctl_position);
        textViews.add(trackctl_lock);
        textViews.add(existing_threads);

        imgvs.add(server_indiator);
        imgvs.add(lock_type_indicator);
        return view;
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("SetTextI18n")
    public static AlertDialog getAdjustPanel(Activity ctx, AppListBean bean, AsyncInterface through) {
        LayoutInflater lf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View root = lf.inflate(R.layout.adjust_panel, null);
        TextInputLayout general = root.findViewById(R.id.adjust_general);
        TextInputLayout left = root.findViewById(R.id.adjust_left);
        TextInputLayout right = root.findViewById(R.id.adjust_right);
        CheckBox split_control = root.findViewById(R.id.aplc_split_control);
        LinearLayout adjust_apply_cover = root.findViewById(R.id.adjust_button_cover);
        LinearLayout split_control_panel = root.findViewById(R.id.adjust_split_control_panel);
        Button adjust_cancel = root.findViewById(R.id.adjust_cancel);
        Button adjust_apply = root.findViewById(R.id.adjust_apply);

        adjust_apply.setEnabled(false);
        ServerStatus.setUpdatePending(true);
        new Thread(ServerStatus::updateStatus).start();

        split_control.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                general.setEnabled(false);
                left.setEnabled(true);
                right.setEnabled(true);
                split_control_panel.setVisibility(View.VISIBLE);
            } else {
                left.setEnabled(false);
                right.setEnabled(false);
                general.setEnabled(true);
                AnimateUtils.playEnd(split_control_panel);
            }
        });

        if (!bean.getProfile().contains("unset")) {
            String process_1[] = bean.getProfile().replaceAll("\r|\n", "").split(",");
            split_control.setChecked(process_1[3].equals("1"));
            Objects.requireNonNull(general.getEditText()).setText(Float.parseFloat(process_1[2]) + "");
            Objects.requireNonNull(left.getEditText()).setText(Float.parseFloat(process_1[0]) + "");
            Objects.requireNonNull(right.getEditText()).setText(Float.parseFloat(process_1[1]) + "");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx)
                .setView(root);

        AlertDialog alertDialog = builder.create();

        adjust_cancel.setOnClickListener(view -> alertDialog.dismiss());

        ServerStatus.requestForPending(() -> ctx.runOnUiThread(() -> adjust_apply.setEnabled(true)));

        adjust_apply.setOnClickListener(view -> {
            adjust_cancel.setEnabled(false);
            alertDialog.setCancelable(false);
            adjust_apply.setEnabled(false);
            adjust_apply.setVisibility(View.INVISIBLE);
            adjust_apply_cover.setVisibility(View.VISIBLE);
            general.setErrorEnabled(false);
            left.setErrorEnabled(false);
            right.setErrorEnabled(false);
            if (Utils.checkAndSetErr(general) && Utils.checkAndSetErr(left) & Utils.checkAndSetErr(right)) {
                /*if (ServerStatus.isServerRunning()) {
                    AudioHqApis.setPkgVolume(bean.getPkgName().replaceAll("\r|\n", ""),
                            Float.parseFloat(general.getEditText().getText().toString()),
                            Float.parseFloat(left.getEditText().getText().toString()),
                            Float.parseFloat(right.getEditText().getText().toString()),
                            split_control.isChecked());
                } else {
                    AudioHqApis.setPidVolume(bean.getPid(),
                            Float.parseFloat(general.getEditText().getText().toString()),
                            Float.parseFloat(left.getEditText().getText().toString()),
                            Float.parseFloat(right.getEditText().getText().toString()),
                            split_control.isChecked());
                }*/
                AudioHqApis.setMPackageVolume(bean.getPkgName().replaceAll("\r|\n", ""),
                        Float.parseFloat(general.getEditText().getText().toString()),
                        Float.parseFloat(left.getEditText().getText().toString()),
                        Float.parseFloat(right.getEditText().getText().toString()),
                        split_control.isChecked());
                alertDialog.dismiss();
                through.onAyncDone(null);
            } else {
                adjust_apply_cover.setVisibility(View.GONE);
                adjust_cancel.setEnabled(true);
                adjust_apply.setEnabled(true);
                adjust_apply.setVisibility(View.VISIBLE);
                alertDialog.setCancelable(true);
            }
        });


        return alertDialog;
    }

    public static AlertDialog getInstallPanel(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(R.string.install_title)
                .setMessage(R.string.install_warning)
                .setNegativeButton(R.string.ad_nb, null)
                .setPositiveButton(R.string.adjust_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean magisk_installed =  CheckUtils.getMagiskInstalled(context);
                        String audioserver_info = CheckUtils.getAudioServerInfo();
                        String[] process_1 = audioserver_info.split(",");
                        String confirm_msg = "<font color='#ff5722'>"
                                + context.getResources().getString(R.string.install_confirm_detect) + "Api:" + Build.VERSION.SDK_INT+", Magisk installed:["+magisk_installed+"]" + "audioserver: %s</font>";

                        boolean show_install = false;

                        if (process_1[1].contains("32-bit LSB arm") && CheckUtils.getIfSupported() && magisk_installed) {
                            confirm_msg = "<font color='#4caf50'>"
                                    + context.getResources().getString(R.string.install_confirm_detect) + "Api:" + Build.VERSION.SDK_INT +", Magisk installed:["+magisk_installed+"]"+ "audioserver: %s</font>";
                            show_install = true;
                        }

                        if (!magisk_installed)
                            new AlertDialog.Builder(context).setTitle(R.string.install_fail_title).setMessage(R.string.install_magisk_not_installed).setNegativeButton(R.string.ad_nb,null).show();

                        String final_confirm_message = context.getResources().getString(R.string.install_confirm_support)
                                + "</br>" + String.format(confirm_msg, process_1[1]);

                        AlertDialog.Builder builder1 = new AlertDialog.Builder(context)
                                .setTitle(R.string.install_confirm_title)
                                .setMessage(Html.fromHtml(final_confirm_message))
                                .setNegativeButton(R.string.ad_nb, null);
                        if (show_install) {
                            builder1.setPositiveButton(R.string.adjust_confirm, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (InstallUtils.install(context, false, true) == -2) {
                                        new AlertDialog.Builder(context)
                                                .setTitle(R.string.install_fail_title)
                                                .setMessage(R.string.install_cant_mount_rw)
                                                .setNegativeButton(R.string.ad_pb, null).show();
                                    }
                                }
                            });

                        }
                        builder1.show();
                    }
                });
        return builder.create();
    }
}
