package io.alcatraz.audiohq.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.AppListBean;
import io.alcatraz.audiohq.utils.PackageCtlUtils;
import io.alcatraz.audiohq.utils.Utils;

public class AppListAdapter extends BaseAdapter {
    private List<AppListBean> data;
    private Context ctx;
    private LayoutInflater lf;

    public AppListAdapter(Context ctx, List<AppListBean> data) {
        this.data = data;
        this.ctx = ctx;
        lf = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = lf.inflate(R.layout.app_list_parent, null);
        }
        AppListBean element = data.get(i);

        TextView aplc_label = view.findViewById(R.id.app_label);
        TextView aplc_pkgname = view.findViewById(R.id.app_pkg);
        ImageView aplc_icon = view.findViewById(R.id.app_icon);

        aplc_pkgname.setText(element.getPkgName());

        aplc_icon.setImageDrawable(element.getIcon());

        String label = PackageCtlUtils.getLabel(ctx, element.getPkgName().contains(":") ? element.getPkgName().split(":")[0] : element.getPkgName());
        if (Utils.isStringNotEmpty(label)) {
            aplc_label.setText(label + " (" + element.getPid() + ")");
        } else {
            aplc_label.setText(element.getPkgName() + " (" + element.getPid() + ")");
            aplc_pkgname.setVisibility(View.GONE);
        }

        setupCtrlPanel(view, element);

        return view;
    }

    private void setupCtrlPanel(View view, AppListBean bean) {
        SeekBar aplc_general = view.findViewById(R.id.aplc_combined_control);
        SeekBar aplc_left = view.findViewById(R.id.aplc_left_control);
        SeekBar aplc_right = view.findViewById(R.id.aplc_right_control);
        CheckBox aplc_split_control = view.findViewById(R.id.aplc_split_control);
        LinearLayout aplc_split_control_panel = view.findViewById(R.id.aplc_split_control_panel);

        aplc_general.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
