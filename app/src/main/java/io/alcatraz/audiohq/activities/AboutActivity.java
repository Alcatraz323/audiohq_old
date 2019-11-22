package io.alcatraz.audiohq.activities;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.alcatraz.support.v4.appcompat.AlertDialogUtil;
import com.alcatraz.support.v4.appcompat.DrawerLayoutUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.alcatraz.audiohq.extended.CompatWithPipeActivity;
import io.alcatraz.audiohq.Constants;
import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.adapters.AuthorAdapter;
import io.alcatraz.audiohq.adapters.QueryElementAdapter;
import io.alcatraz.audiohq.beans.QueryElement;
import io.alcatraz.audiohq.extended.DividerItemDecoration;
import io.alcatraz.audiohq.utils.Utils;

public class AboutActivity extends CompatWithPipeActivity {
    List<Integer> imgs = new ArrayList<Integer>();
    Map<Integer, List<String>> data = new HashMap<>();
    ListView lv;
    android.support.v7.widget.Toolbar tb;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        initData();
        initViews();
    }

    public void showDetailDev() {
        android.support.v7.app.AlertDialog g = new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(R.string.au_l_2)
                .setMessage("主代码:Alcatraz\n" +
                        "主要测试人员:Mr_Dennis(Coolapk)")
                .setPositiveButton(R.string.ad_pb, null)
                .create();
        new AlertDialogUtil().setSupportDialogColor(g, Color.parseColor("#3f51b5"));
        g.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void initViews() {
        tb = findViewById(R.id.about_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DrawerLayoutUtil.Immersive(tb,true,this);
        lv = findViewById(R.id.authorcontentListView1);
        AuthorAdapter aa = new AuthorAdapter(this, data, imgs);
        lv.setAdapter(aa);
        lv.setOnItemClickListener((p1, p2, p3, p4) -> {
            if (p1.getItemAtPosition(p3).toString().equals(getString(R.string.au_l_3))) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Alcatraz323/audiohq")));
            } else if (p1.getItemAtPosition(p3).toString().equals(getString(R.string.au_l_4))) {
                showOSPDialog();
            } else if (p1.getItemAtPosition(p3).toString().equals(getString(R.string.au_l_2))) {
                showDetailDev();
            }
        });
    }

    public void showOSPDialog(){
        View v=getLayoutInflater().inflate(R.layout.dialog_ops,null);
        new android.support.v7.app.AlertDialog.Builder(this)
                .setTitle(R.string.au_osp)
                .setView(v)
                .setNegativeButton(R.string.ad_nb3,null).show();
        RecyclerView rv= v.findViewById(R.id.opRc1);
        List<QueryElement> dat= Constants.getOpenSourceProjects();
        QueryElementAdapter mra=new QueryElementAdapter(this,dat);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.setAdapter(mra);
        rv.addItemDecoration(new DividerItemDecoration(this, LinearLayout.HORIZONTAL, Utils.Dp2Px(this,8),Color.parseColor("#eeeeee")));
    }



    public void initData() {
        imgs.add(R.drawable.ic_information_outline);
        imgs.add(R.drawable.ic_account);
        imgs.add(R.drawable.ic_github);
        imgs.add(R.drawable.ic_open_in_new);
        List<String> l1 = new ArrayList<>();
        l1.add(getString(R.string.au_l_1));
        l1.add("---");
        List<String> l2 = new ArrayList<>();
        l2.add(getString(R.string.au_l_2));
        l2.add(getString(R.string.au_l_2_1));
        List<String> l3 = new ArrayList<>();
        l3.add(getString(R.string.au_l_3));
        l3.add("");
        List<String> l4 = new ArrayList<>();
        l4.add(getString(R.string.au_l_4));
        l4.add(getString(R.string.au_l_4_1));
        data.put(0, l1);
        data.put(1, l2);
        data.put(2, l3);
        data.put(3, l4);

    }
}