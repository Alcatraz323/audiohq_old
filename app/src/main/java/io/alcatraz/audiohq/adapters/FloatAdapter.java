package io.alcatraz.audiohq.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.nativebuffers.PackageBuffers;
import io.alcatraz.audiohq.beans.nativebuffers.Pkgs;
import io.alcatraz.audiohq.beans.nativebuffers.Processes;
import io.alcatraz.audiohq.core.utils.AudioHqApis;
import io.alcatraz.audiohq.utils.PackageCtlUtils;
import io.alcatraz.audiohq.utils.Utils;

public class FloatAdapter extends BaseAdapter {
    private PackageBuffers data;
    private Context context;
    private LayoutInflater inflater;
    private Handler cleaner;
    private Runnable cleanTask;
    private int font_color = 0;

    public FloatAdapter(Context context, PackageBuffers initdata, Handler cleaner, Runnable cleanTask) {
        this.data = initdata;
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.cleaner = cleaner;
        this.cleanTask = cleanTask;
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
            view = inflater.inflate(R.layout.float_list_item, null);
        }
        //Current Data Node
        Pkgs pkgs = data.getPkgs().get(i);
        Processes process = pkgs.getProcesses().get(0);

        ImageView aplc_icon = view.findViewById(R.id.float_list_item_icon);
        TextView aplc_label = view.findViewById(R.id.float_list_item_label);
        SeekBar seekBar = view.findViewById(R.id.float_list_item_general_seek);

        aplc_label.setTextColor(font_color);

        if (pkgs.isweakkeyed()){
            seekBar.setProgress((int) (Float.parseFloat(process.getBuffers().get(0).getFinalv()) * 10000));
        }else {
            seekBar.setProgress(10000);
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    AudioHqApis.setProfile(pkgs.getPkg(),
                            seekBar.getProgress() * 0.0001f,
                            Float.parseFloat(process.getBuffers().get(0).getLeft()) ,
                            Float.parseFloat(process.getBuffers().get(0).getLeft()),
                            false, true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                cleaner.removeCallbacks(cleanTask);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                cleaner.postDelayed(cleanTask,3000);
            }
        });

        Drawable icon = PackageCtlUtils.getIcon(context, pkgs.getPkg());

        if (icon != null)
            aplc_icon.setImageDrawable(icon);

        String label = PackageCtlUtils.getLabel(context, pkgs.getPkg());

        if (Utils.isStringNotEmpty(label)) {
            aplc_label.setText(label);
        } else {
            aplc_label.setText(process.getProcess());
        }

        return view;
    }

    public int getFontColor(){
        if(font_color == 0){
            font_color=context.getResources().getColor(R.color.default_colorPrimary);
        }
        return font_color;
    }

    public void setFontColor(String color){
        font_color = Color.parseColor(color);
    }
}
