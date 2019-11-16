package io.alcatraz.audiohq.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import io.alcatraz.audiohq.R;
import io.alcatraz.audiohq.beans.QueryElement;

public class QueryElementAdapter extends RecyclerView.Adapter<QueryElementContainer> {
    List<QueryElement> data;
    Context ctx;
    LayoutInflater lf;

    public QueryElementAdapter(Context c, List<QueryElement> data) {
        ctx = c;
        this.data = data;
        lf = (LayoutInflater) ctx.getSystemService(ctx.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public QueryElementContainer onCreateViewHolder(ViewGroup p1, int p2) {
        View v = lf.inflate(R.layout.item_opensource_holder, p1, false);
        QueryElementContainer mvh = new QueryElementContainer(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(QueryElementContainer p1, int p2) {
        View root = p1.getView();
        final QueryElement cur = data.get(p2);
        CardView cv = root.findViewById(R.id.listcardCardView1);
        if (cur.getUrl() != null) {
            cv.setOnClickListener(p11 -> {
                Intent localIntent = new Intent("android.intent.action.VIEW");
                localIntent.setData(Uri.parse(cur.getUrl()));
                ctx.startActivity(localIntent);
            });
        }
        TextView name = root.findViewById(R.id.opensourceholderTextView1);
        TextView author = root.findViewById(R.id.opensourceholderTextView2);
        TextView intro = root.findViewById(R.id.opensourceholderTextView3);
        TextView lic = root.findViewById(R.id.opensourceholderTextView4);
        name.setText(cur.getName());
        author.setText(cur.getAuthor());
        intro.setText(cur.getIntro());
        lic.setText(cur.getLicense());
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}

class QueryElementContainer extends RecyclerView.ViewHolder {
    private View hold;

    QueryElementContainer(View v) {
        super(v);
        hold = v;
    }

    public View getView() {
        return hold;
    }
}