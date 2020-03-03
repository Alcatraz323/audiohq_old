package io.alcatraz.audiohq.adapters;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.alcatraz.audiohq.AsyncInterface;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.LambdaBridge;
import io.alcatraz.audiohq.beans.PrecisePanelBridge;
import io.alcatraz.audiohq.beans.nativebuffers.Buffers;
import io.alcatraz.audiohq.beans.nativebuffers.Processes;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.utils.AnimateUtils;
import io.alcatraz.audiohq.utils.PackageCtlUtils;
import io.alcatraz.audiohq.utils.Panels;

public class PresetAdapter extends BaseAdapter implements Filterable {
    private List<PackageInfo> data;
    private Context context;
    private LayoutInflater inflater;
    private PackageFilter filter;

    public PresetAdapter (Context context,List<PackageInfo> data){
        this.data =data;
        this.context =context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = inflater.inflate(R.layout.app_list_parent, null);
        }
        //Current Data Node
        PackageInfo info = data.get(i);
        Processes process = new Processes();
        Buffers buffers = new Buffers();
        buffers.setLeft("1.0");
        buffers.setRight("1.0");
        buffers.setFinalv("1.0");
        buffers.setControl_lr("false");
        List<Buffers> b = new ArrayList<>();
        b.add(buffers);
        process.setBuffers(b);

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

        app_allowed.setOnClickListener(view1 -> {
            boolean muted = app_allowed.isChecked();
            if (muted) {
                AudioHqApis.unmuteProcess(info.packageName, true);
            } else {
                AudioHqApis.muteProcess(info.packageName, true);
            }
        });

        aplc_pkgname.setText(info.packageName);

        aplc_icon.setImageDrawable(PackageCtlUtils.getIcon(context,info.packageName));

        aplc_label.setText(PackageCtlUtils.getLabel(context,info.packageName));

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

        setupControlPanel(view, info.packageName);

        return view;

    }

    private void setupControlPanel(View root, String pkg) {
        SeekBar general = root.findViewById(R.id.aplc_combined_control);
        SeekBar left = root.findViewById(R.id.aplc_left_control);
        SeekBar right = root.findViewById(R.id.aplc_right_control);
        CheckBox split_control = root.findViewById(R.id.aplc_split_control);
        LinearLayout split_control_panel = root.findViewById(R.id.aplc_split_control_panel);

        split_control.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    general.setEnabled(false);
                    left.setEnabled(true);
                    right.setEnabled(true);
                    split_control_panel.setVisibility(View.VISIBLE);
                    AudioHqApis.setProfile(pkg,
                            general.getProgress() * 0.0001f,
                            left.getProgress() * 0.0001f,
                            right.getProgress() * 0.0001f,
                            split_control.isChecked(), true);
                } else {
                    left.setEnabled(false);
                    right.setEnabled(false);
                    general.setEnabled(true);
                    AnimateUtils.playEnd(split_control_panel);
                    AudioHqApis.setProfile(pkg,
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
                    AudioHqApis.setProfile(pkg,
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
                    AudioHqApis.setProfile(pkg,
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
                    AudioHqApis.setProfile(pkg,
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

    @Override
    public Filter getFilter() {
        if (filter == null) {
            filter = new PackageFilter(data);
        }
        return filter;
    }

    public void onTextChanged(String newtext){
        getFilter().filter(newtext);
    }

    private class PackageFilter extends Filter{

        List<PackageInfo> original;

        public PackageFilter(List<PackageInfo> tofilter){
            original = tofilter;
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults results = new FilterResults();
            if (charSequence == null || charSequence.length() == 0) {
                results.values = original;
                results.count = original.size();
            } else {
                List<PackageInfo> mList = new ArrayList<>();
                for(PackageInfo info : original){
                    String label = PackageCtlUtils.getLabel(context,info.packageName);
                    if(label.toLowerCase().contains(charSequence.toString().toLowerCase())
                            ||info.packageName.toLowerCase().contains(charSequence.toString().toLowerCase())){
                        mList.add(info);
                    }
                }
                results.values = mList;
                results.count = mList.size();
            }
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            data = (List<PackageInfo>) filterResults.values;
            notifyDataSetChanged();
        }
    }
}
