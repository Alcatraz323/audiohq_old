package io.alcatraz.audiohq.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

import io.alcatraz.audiohq.R;

public class AuthorAdapter extends BaseAdapter
{
    Map<Integer,List<String>> data;
    List<Integer> img;
    Context c;
    public AuthorAdapter(Context c,Map<Integer,List<String>> data,List<Integer> img){
        this.data=data;
        this.c=c;
        this.img=img;
    }
    @Override
    public int getCount()
    {
        return data.size();
    }

    @Override
    public Object getItem(int p1)
    {
        return data.get(p1).get(0);
    }

    @Override
    public long getItemId(int p1)
    {
        return p1;
    }

    @Override
    public View getView(int p1, View p2, ViewGroup p3)
    {
        if(p2==null){
            LayoutInflater lf=(LayoutInflater) c.getSystemService(c.LAYOUT_INFLATER_SERVICE);
            p2=lf.inflate(R.layout.item_author_main_list,null);
        }
        ImageView iv= p2.findViewById(R.id.authoritemImageView1);
        TextView txv1= p2.findViewById(R.id.authoritemTextView1);
        TextView txv2= p2.findViewById(R.id.authoritemTextView2);
        iv.setImageResource(img.get(p1));
        txv1.setText(data.get(p1).get(0));
        txv2.setText(data.get(p1).get(1));
        if(txv1.getText().toString().equals(c.getString(R.string.au_l_3))){
            txv2.setVisibility(View.GONE);
        }
        return p2;
    }

}
