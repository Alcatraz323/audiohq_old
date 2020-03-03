package io.alcatraz.audiohq.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.LambdaBridge;
import io.alcatraz.audiohq.beans.PrecisePanelBridge;
import io.alcatraz.audiohq.beans.nativebuffers.Buffers;
import io.alcatraz.audiohq.beans.nativebuffers.PackageBuffers;
import io.alcatraz.audiohq.beans.nativebuffers.Pkgs;
import io.alcatraz.audiohq.beans.nativebuffers.Processes;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.extended.NoScrollListView;
import io.alcatraz.audiohq.utils.AnimateUtils;
import io.alcatraz.audiohq.utils.PackageCtlUtils;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.Utils;

public class PlayingAdapter extends BaseAdapter {
    private PackageBuffers data;
    private Context context;
    private LayoutInflater inflater;

    public PlayingAdapter(Context context, PackageBuffers initdata) {
        this.data = initdata;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setNewData(PackageBuffers buffers) {
        data = buffers;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.getPkgs().size();
    }

    @Override
    public Object getItem(int i) {
        return data.getPkgs().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.app_list_pkg_mode_parent, null);
        }
        //Current Data Node
        Pkgs pkgs = data.getPkgs().get(i);
        Processes process = pkgs.getProcesses().get(0);
        boolean weakkeyed = process.getBuffers().get(0).getIsweakkey().equals("yes");
        final LambdaBridge<View> bridge = new LambdaBridge<>();
        bridge.setTarget(view);

        //Weakkeyed controller widget
        TextView aplc_label = view.findViewById(R.id.app_label);
        TextView aplc_pkgname = view.findViewById(R.id.app_pkg);
        ImageView aplc_icon = view.findViewById(R.id.app_icon);
        ImageButton show_adjust = view.findViewById(R.id.app_adjust_switch);
        Switch app_allowed = view.findViewById(R.id.app_allowed);
        SeekBar general = view.findViewById(R.id.aplc_combined_control);
        SeekBar left = view.findViewById(R.id.aplc_left_control);
        SeekBar right = view.findViewById(R.id.aplc_right_control);
        CheckBox split_control = view.findViewById(R.id.aplc_split_control);

        //Process control
        LinearLayout process_control_switch = view.findViewById(R.id.app_list_use_process_control);
        NoScrollListView process_list = view.findViewById(R.id.app_list_process_list);
        Switch process_control_indicator = view.findViewById(R.id.app_list_use_process_control_indicator);

        if (weakkeyed) {
            process_list.setVisibility(View.GONE);
            togglePanelEnabled(view, true);
            process_control_indicator.setChecked(false);
        } else {
            process_list.setVisibility(View.VISIBLE);
            togglePanelEnabled(view, false);
            process_control_indicator.setChecked(true);
        }

        process_control_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean weakkeyed = process.getBuffers().get(0).getIsweakkey().equals("yes");
                if (weakkeyed) {
                    process_list.setVisibility(View.VISIBLE);
                    process_control_indicator.setChecked(true);
                    togglePanelEnabled(bridge.getTarget(), false);
                    AudioHqApis.unsetProfile(pkgs.getPkg(), true);
                    setWeakkey(process,false);
                } else {
                    process_list.setVisibility(View.GONE);
                    togglePanelEnabled(bridge.getTarget(), true);
                    process_control_indicator.setChecked(false);
                    AudioHqApis.setProfile(pkgs.getPkg(),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), true);
                    setWeakkey(process,true);
                }
            }
        });

        ProcessAdapter processAdapter = new ProcessAdapter(context, pkgs);
        process_list.setAdapter(processAdapter);

        app_allowed.setChecked(!process.getBuffers().get(0).getMuted().equals("true"));

        app_allowed.setOnClickListener(view1 -> {
            boolean muted = process.getBuffers().get(0).getMuted().equals("true");
            if (muted) {
                AudioHqApis.unmuteProcess(Utils.getFinalProcessName(true, process.getProcess()), true);
                setMuted(process, false);
            } else {
                AudioHqApis.muteProcess(Utils.getFinalProcessName(true, process.getProcess()), true);
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
                Panels.getAdjustPanel(context, process, false, true, new AsyncInterface<PrecisePanelBridge>() {
                    @SuppressWarnings("ConstantConditions")
                    @Override
                    public boolean onAyncDone(@Nullable PrecisePanelBridge val) {
                        general.setProgress((int) (val.getGeneral() * 10000));
                        left.setProgress((int) (val.getLeft() * 10000));
                        right.setProgress((int) (val.getRight() * 10000));
                        split_control.setChecked(val.isControl_lr());
                        val.getAlertDialog().dismiss();
                        return false;
                    }

                    @Override
                    public void onFailure(String reason) {

                    }
                }).show());

        setupControlPanel(view, process);

        return view;

    }

    private void togglePanelEnabled(View root, boolean enabled) {
        ImageButton show_adjust = root.findViewById(R.id.app_adjust_switch);
        Switch app_allowed = root.findViewById(R.id.app_allowed);
        SeekBar general = root.findViewById(R.id.aplc_combined_control);
        SeekBar left = root.findViewById(R.id.aplc_left_control);
        SeekBar right = root.findViewById(R.id.aplc_right_control);
        CheckBox split_control = root.findViewById(R.id.aplc_split_control);

        show_adjust.setEnabled(enabled);
        app_allowed.setEnabled(enabled);
        general.setEnabled(enabled);
        left.setEnabled(enabled);
        right.setEnabled(enabled);
        split_control.setEnabled(enabled);
    }

    private void setupControlPanel(View root, Processes bean) {
        SeekBar general = root.findViewById(R.id.aplc_combined_control);
        SeekBar left = root.findViewById(R.id.aplc_left_control);
        SeekBar right = root.findViewById(R.id.aplc_right_control);
        CheckBox split_control = root.findViewById(R.id.aplc_split_control);
        LinearLayout split_control_panel = root.findViewById(R.id.aplc_split_control_panel);

        Buffers buffers = bean.getBuffers().get(0);

        //Load set data before bind listeners
        split_control.setChecked(buffers.getControl_lr().equals("true"));
        if(buffers.getIsweakkey().equals("yes")) {
            if (split_control.isChecked()) {
                general.setEnabled(false);
                left.setEnabled(true);
                right.setEnabled(true);
                split_control_panel.setVisibility(View.VISIBLE);
            } else {
                left.setEnabled(false);
                right.setEnabled(false);
                general.setEnabled(true);
                split_control_panel.setVisibility(View.GONE);
            }
        }

        general.setProgress((int) (Float.parseFloat(buffers.getFinalv()) * 10000));
        left.setProgress((int) (Float.parseFloat(buffers.getLeft()) * 10000));
        right.setProgress((int) (Float.parseFloat(buffers.getRight()) * 10000));

        split_control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    general.setEnabled(false);
                    left.setEnabled(true);
                    right.setEnabled(true);
                    split_control_panel.setVisibility(View.VISIBLE);
                    AudioHqApis.setProfile(Utils.getFinalProcessName(true, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), true);
                } else {
                    left.setEnabled(false);
                    right.setEnabled(false);
                    general.setEnabled(true);
                    AnimateUtils.playEnd(split_control_panel);
                    AudioHqApis.setProfile(Utils.getFinalProcessName(true, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), true);
                }
            }
        });

        general.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    AudioHqApis.setProfile(Utils.getFinalProcessName(true, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), true);
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
                if (b) {
                    AudioHqApis.setProfile(Utils.getFinalProcessName(true, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), true);
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
                if (b) {
                    AudioHqApis.setProfile(Utils.getFinalProcessName(true, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


            }
        });
    }

    public void setMuted(Processes processes, boolean muted) {
        List<Buffers> buffers = processes.getBuffers();
        for (Buffers buffers1 : buffers) {
            buffers1.setMuted(muted ? "true" : "false");
        }
    }

    public void setWeakkey(Processes processes, boolean weakkey) {
        List<Buffers> buffers = processes.getBuffers();
        for (Buffers buffers1 : buffers) {
            buffers1.setIsweakkey(weakkey ? "yes" : "no");
        }
    }
}
