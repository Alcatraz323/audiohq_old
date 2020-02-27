package io.alcatraz.audiohq.adapters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.nativebuffers.Buffers;
import io.alcatraz.audiohq.beans.nativebuffers.PackageBuffers;
import io.alcatraz.audiohq.beans.nativebuffers.Pkgs;
import io.alcatraz.audiohq.beans.nativebuffers.Processes;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.utils.AnimateUtils;
import io.alcatraz.audiohq.utils.PackageCtlUtils;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.Utils;

public class PlyPkgExpandableAdapter extends BaseExpandableListAdapter {
    private PackageBuffers data;
    private Context context;
    private LayoutInflater inflater;
    private boolean isweakkey;

    public PlyPkgExpandableAdapter(Context context, PackageBuffers initdata) {
        this.data = initdata;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setNewData(PackageBuffers buffers) {
        data = buffers;
        notifyDataSetChanged();
    }

    public void setWeakkey(boolean weakkey) {
        isweakkey = weakkey;
        notifyDataSetChanged();
    }

    @Override
    public int getGroupCount() {
        return data.getPkgs().size();
    }

    @Override
    public int getChildrenCount(int i) {
        int count = data.getPkgs().get(i).getProcesses().size();
        if(isweakkey)
            count = 0;
        return count;
    }

    @Override
    public Object getGroup(int i) {
        return data.getPkgs().get(i);
    }

    @Override
    public Object getChild(int i, int i1) {
        return data.getPkgs().get(i).getProcesses().get(i1);
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

    @SuppressLint({"InflateParams", "SetTextI18n"})
    @Override
    public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
        if (!isweakkey) {
            view = inflater.inflate(R.layout.app_list_pkg_mode_parent, null);
            Pkgs pkgs = data.getPkgs().get(i);

            ImageView icon = view.findViewById(R.id.app_list_pkg_icon);
            TextView label = view.findViewById(R.id.app_list_pkg_label);
            TextView pkg_name = view.findViewById(R.id.app_list_pkg_name);
            Drawable icon_d = PackageCtlUtils.getIcon(context, pkgs.getPkg());
            if (icon_d != null) {
                icon.setImageDrawable(icon_d);
            }
            label.setText(PackageCtlUtils.getLabel(context, pkgs.getPkg())+" ("+pkgs.getProcesses().size()+")");
            pkg_name.setText(pkgs.getPkg());
            return view;
        } else {
            view = inflater.inflate(R.layout.app_list_parent, null);

            Pkgs pkgs = data.getPkgs().get(i);
            Processes process = pkgs.getProcesses().get(0);

            TextView aplc_label = view.findViewById(R.id.app_label);
            TextView aplc_pkgname = view.findViewById(R.id.app_pkg);
            ImageView aplc_icon = view.findViewById(R.id.app_icon);
            ImageButton show_adjust = view.findViewById(R.id.app_adjust_switch);
            Switch app_allowed = view.findViewById(R.id.app_allowed);

            app_allowed.setChecked(!process.getBuffers().get(0).getMuted().equals("true"));

            app_allowed.setOnClickListener(view1 -> {
                boolean muted = process.getBuffers().get(0).getMuted().equals("true");
                if (muted) {
                    AudioHqApis.unmuteProcess(Utils.getFinalProcessName(isweakkey, process.getProcess()), isweakkey);
                    setMuted(process, false);
                } else {
                    AudioHqApis.muteProcess(Utils.getFinalProcessName(isweakkey, process.getProcess()), isweakkey);
                    setMuted(process, true);
                }
            });

            aplc_pkgname.setText(pkgs.getPkg());

            Drawable icon = PackageCtlUtils.getIcon(context, pkgs.getPkg());

            if (icon != null)
                aplc_icon.setImageDrawable(icon);

            String label = PackageCtlUtils.getLabel(context, pkgs.getPkg());

            if (Utils.isStringNotEmpty(label)) {
                aplc_label.setText(label);
            } else {
                aplc_label.setText(process.getProcess());
            }
            show_adjust.setOnClickListener(view1 ->
                    Panels.getAdjustPanel(context, process, false, false, new AsyncInterface<AlertDialog>() {
                        @Override
                        public boolean onAyncDone(@Nullable AlertDialog val) {
                            assert val != null;
                            val.dismiss();
                            notifyDataSetChanged();
                            return false;
                        }

                        @Override
                        public void onFailure(String reason) {

                        }
                    }).show());

            setupControlPanel(view, process);

            return view;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
        if (isweakkey) {
            return null;
        }

        if (view == null) {
            view = inflater.inflate(R.layout.app_list_parent, null);
        }

        Processes process = data.getPkgs().get(i).getProcesses().get(i1);

        TextView pid = view.findViewById(R.id.app_label);
        TextView process_name = view.findViewById(R.id.app_pkg);
        ImageButton show_adjust = view.findViewById(R.id.app_adjust_switch);
        Switch app_allowed = view.findViewById(R.id.app_allowed);

        app_allowed.setChecked(!process.getBuffers().get(0).getMuted().equals("true"));

        app_allowed.setOnClickListener(view1 -> {
            boolean muted = process.getBuffers().get(0).getMuted().equals("true");
            if (muted) {
                AudioHqApis.unmuteProcess(process.getProcess(), false);
                setMuted(process, false);
            } else {
                AudioHqApis.muteProcess(process.getProcess(), false);
                setMuted(process, true);
            }
        });

        pid.setText("PID : "+process.getPid());

        process_name.setText(process.getProcess());

        show_adjust.setOnClickListener(view1 ->
                Panels.getAdjustPanel(context, process, false, isweakkey, new AsyncInterface<AlertDialog>() {
                    @Override
                    public boolean onAyncDone(@Nullable AlertDialog val) {
                        assert val != null;
                        val.dismiss();
                        notifyDataSetChanged();
                        return false;
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                }).show());

        setupControlPanel(view, process);

        return view;
    }

    @Override
    public boolean isChildSelectable(int i, int i1) {
        return false;
    }

    private void setupControlPanel(View root, Processes bean) {
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
                AudioHqApis.setProfile(bean.getProcess(),
                        general.getProgress() * 0.0001f,
                        left.getProgress() * 0.0001f,
                        right.getProgress() * 0.0001f,
                        split_control.isChecked(), false);
            } else {
                left.setEnabled(false);
                right.setEnabled(false);
                general.setEnabled(true);
                AnimateUtils.playEnd(split_control_panel);
                AudioHqApis.setProfile(bean.getProcess(),
                        general.getProgress() * 0.0001f,
                        left.getProgress() * 0.0001f,
                        right.getProgress() * 0.0001f,
                        split_control.isChecked(), false);
            }
        });

        general.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    AudioHqApis.setProfile(Utils.getFinalProcessName(isweakkey, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), isweakkey);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        left.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    AudioHqApis.setProfile(Utils.getFinalProcessName(isweakkey, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), isweakkey);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }

        });
        right.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(b) {
                    AudioHqApis.setProfile(Utils.getFinalProcessName(isweakkey, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), isweakkey);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });

        Buffers buffers = bean.getBuffers().get(0);

        split_control.setChecked(buffers.getControl_lr().equals("true"));

        general.setProgress((int) (Float.parseFloat(buffers.getFinalv()) * 10000));
        left.setProgress((int) (Float.parseFloat(buffers.getLeft()) * 10000));
        right.setProgress((int) (Float.parseFloat(buffers.getRight()) * 10000));

    }

    public void setMuted(Processes processes, boolean muted) {
        List<Buffers> buffers = processes.getBuffers();
        for (Buffers buffers1 : buffers) {
            buffers1.setMuted(muted ? "true" : "false");
        }
    }
}
