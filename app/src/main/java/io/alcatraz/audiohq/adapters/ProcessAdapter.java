package io.alcatraz.audiohq.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.List;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.PrecisePanelBridge;
import io.alcatraz.audiohq.beans.nativebuffers.Buffers;
import io.alcatraz.audiohq.beans.nativebuffers.Pkgs;
import io.alcatraz.audiohq.beans.nativebuffers.Processes;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.utils.AnimateUtils;
import io.alcatraz.audiohq.utils.Panels;
import io.alcatraz.audiohq.utils.Utils;

public class ProcessAdapter extends BaseAdapter {
    private Pkgs data;
    private Context context;
    private LayoutInflater inflater;

    public ProcessAdapter(Context context, Pkgs initdata) {
        this.data = initdata;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setNewData(Pkgs buffers) {
        data = buffers;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.getProcesses().size();
    }

    @Override
    public Object getItem(int i) {
        return data.getProcesses().get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.app_list_parent, null);
        }
        //Current Data Node
        Processes process = data.getProcesses().get(i);

        //Weakkeyed controller widget
        TextView aplc_label = view.findViewById(R.id.app_label);
        TextView aplc_pkgname = view.findViewById(R.id.app_pkg);
        ImageButton show_adjust = view.findViewById(R.id.app_adjust_switch);
        Switch app_allowed = view.findViewById(R.id.app_allowed);
        SeekBar general = view.findViewById(R.id.aplc_combined_control);
        SeekBar left = view.findViewById(R.id.aplc_left_control);
        SeekBar right = view.findViewById(R.id.aplc_right_control);
        CheckBox split_control = view.findViewById(R.id.aplc_split_control);

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

        aplc_pkgname.setText(process.getProcess());

        aplc_label.setText("Pid : " + process.getPid());

        show_adjust.setOnClickListener(view1 ->
                Panels.getAdjustPanel(context, process, false, false, new AsyncInterface<PrecisePanelBridge>() {
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

    private void setupControlPanel(View root, Processes bean) {
        SeekBar general = root.findViewById(R.id.aplc_combined_control);
        SeekBar left = root.findViewById(R.id.aplc_left_control);
        SeekBar right = root.findViewById(R.id.aplc_right_control);
        CheckBox split_control = root.findViewById(R.id.aplc_split_control);
        LinearLayout split_control_panel = root.findViewById(R.id.aplc_split_control_panel);

        Buffers buffers = bean.getBuffers().get(0);

        split_control.setChecked(buffers.getControl_lr().equals("true"));
        if(split_control.isChecked()){
            general.setEnabled(false);
            left.setEnabled(true);
            right.setEnabled(true);
            split_control_panel.setVisibility(View.VISIBLE);
        }else {
            left.setEnabled(false);
            right.setEnabled(false);
            general.setEnabled(true);
            split_control_panel.setVisibility(View.GONE);
        }

        general.setProgress((int) (Float.parseFloat(buffers.getFinalv()) * 10000));
        left.setProgress((int) (Float.parseFloat(buffers.getLeft()) * 10000));
        right.setProgress((int) (Float.parseFloat(buffers.getRight()) * 10000));

        split_control.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                general.setEnabled(false);
                left.setEnabled(true);
                right.setEnabled(true);
                split_control_panel.setVisibility(View.VISIBLE);
                AudioHqApis.setProfile(Utils.getFinalProcessName(false, bean.getProcess()),
                        general.getProgress() * 0.0001f,
                        left.getProgress() * 0.0001f,
                        right.getProgress() * 0.0001f,
                        split_control.isChecked(), false);
            } else {
                left.setEnabled(false);
                right.setEnabled(false);
                general.setEnabled(true);
                AnimateUtils.playEnd(split_control_panel);
                AudioHqApis.setProfile(Utils.getFinalProcessName(false, bean.getProcess()),
                        general.getProgress() * 0.0001f,
                        left.getProgress() * 0.0001f,
                        right.getProgress() * 0.0001f,
                        split_control.isChecked(), false);
            }
        });

        general.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    AudioHqApis.setProfile(Utils.getFinalProcessName(false, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), false);
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
                    AudioHqApis.setProfile(Utils.getFinalProcessName(false, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), false);
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
                    AudioHqApis.setProfile(Utils.getFinalProcessName(false, bean.getProcess()),
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), false);
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
}
