package io.alcatraz.audiohq.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Map;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.AppListBean;
import io.alcatraz.audiohq.beans.ServerStatus;
import io.alcatraz.audiohq.beans.TrackBean;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.utils.AnimateUtils;
import io.alcatraz.audiohq.utils.PackageCtlUtils;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.Utils;

public class PlayingExpandableAdapter extends BaseExpandableListAdapter {
    private Map<String, AppListBean> data;
    private Activity ctx;
    private LayoutInflater lf;
    private AsyncInterface throuth;

    public PlayingExpandableAdapter(Activity ctx, Map<String, AppListBean> data, AsyncInterface throuth) {
        this.data = data;
        this.ctx = ctx;
        this.throuth = throuth;
        lf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return data.keySet().size();
    }

    @Override
    public int getChildrenCount(int i) {
        return data.get(new ArrayList<>(data.keySet()).get(i)).getTracks().size();
    }

    @Override
    public Object getGroup(int i) {
        return data.get(new ArrayList<>(data.keySet()).get(i));
    }

    @Override
    public Object getChild(int i, int i1) {
        return data.get(new ArrayList<>(data.keySet()).get(i)).getTracks().get(i1);
    }

    @Override
    public long getGroupId(int i) {
        return i;
    }

    @Override
    public long getChildId(int i, int i1) {
        return i1;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = lf.inflate(R.layout.app_list_parent, null);
        }
        AppListBean element = data.get(new ArrayList<>(data.keySet()).get(i));

        TextView aplc_label = view.findViewById(R.id.app_label);
        TextView aplc_pkgname = view.findViewById(R.id.app_pkg);
        ImageView aplc_icon = view.findViewById(R.id.app_icon);
        ImageButton show_adjust = view.findViewById(R.id.app_adjust_switch);
        Switch app_allowed = view.findViewById(R.id.app_allowed);

        app_allowed.setChecked(!element.isMuted());

        app_allowed.setOnCheckedChangeListener((compoundButton, b1) -> AudioHqApis.setMute(element.getPkgName(), !b1));

        aplc_pkgname.setText(element.getPkgName().trim());

        aplc_icon.setImageDrawable(element.getIcon());

        String label = PackageCtlUtils.getLabel(ctx, element.getPkgName().contains(":") ?
                element.getPkgName().split(":")[0].trim() : element.getPkgName().trim());
        if (Utils.isStringNotEmpty(label)) {
            aplc_label.setText(label + " (" + element.getPid() + ")");
        } else {
            aplc_label.setText(element.getPkgName().trim() + " (" + element.getPid() + ")");
        }
        aplc_label.append("(" + element.activeCount() + "/" + element.getTracks().size() + " active)");

        show_adjust.setOnClickListener(view1 -> Panels.getAdjustPanel(ctx, element,throuth).show());

        setupControlPanel(view, element);

        return view;
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = lf.inflate(R.layout.app_list_track_item, null);
        }

        AppListBean grp = data.get(new ArrayList<>(data.keySet()).get(i));
        TrackBean element = grp.getTrack(i1);

        TextView aplc_track_info_brief = view.findViewById(R.id.aplc_track_info_brief);
        TextView aplc_track_info_thread = view.findViewById(R.id.aplc_track_info_thread);
        aplc_track_info_brief.setText("Track:   sessionid=" + element.getSessionId() +
                "    samplerate=" + element.getSampleRate() +
                "    framecount=" + element.getFrameSize() +
                "   " + element.getActive() +
                "   thread:");
        aplc_track_info_thread.setText(element.getThread());

        return view;
    }

    private void setupControlPanel(View root, AppListBean bean) {
        SeekBar general = root.findViewById(R.id.aplc_combined_control);
        SeekBar left = root.findViewById(R.id.aplc_left_control);
        SeekBar right = root.findViewById(R.id.aplc_right_control);
        CheckBox split_control = root.findViewById(R.id.aplc_split_control);
        LinearLayout split_control_panel = root.findViewById(R.id.aplc_split_control_panel);



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

        general.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioHqApis.setMPackageVolume(bean.getPkgName(),
                        general.getProgress() * 0.0001f,
                        left.getProgress() * 0.0001f,
                        right.getProgress() * 0.0001f,
                        split_control.isChecked());
            }
        });

        left.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioHqApis.setMPackageVolume(bean.getPkgName(),
                        general.getProgress() * 0.0001f,
                        left.getProgress() * 0.0001f,
                        right.getProgress() * 0.0001f,
                        split_control.isChecked());
            }
        });
        right.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                AudioHqApis.setMPackageVolume(bean.getPkgName(),
                        general.getProgress() * 0.0001f,
                        left.getProgress() * 0.0001f,
                        right.getProgress() * 0.0001f,
                        split_control.isChecked());
            }
        });

        if (!bean.getProfile().contains("unset")) {
            String process_1[] = bean.getProfile().replaceAll("\r|\n", "").split(",");
            split_control.setChecked(process_1[3].equals("1"));

            general.setProgress((int) (Float.parseFloat(process_1[2]) * 10000));
            left.setProgress((int) (Float.parseFloat(process_1[0]) * 10000));
            right.setProgress((int) (Float.parseFloat(process_1[1]) * 10000));
        }
    }
}
