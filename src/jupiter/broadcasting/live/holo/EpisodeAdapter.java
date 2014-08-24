package jupiter.broadcasting.live.holo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Hashtable;
import java.util.List;


public class EpisodeAdapter extends RecyclerView.Adapter<EpisodeAdapter.ViewHolder> {

    private static LayoutInflater inflater = null;
    private static Hashtable<String, String[]> data;
    private List<String> titles;
    private boolean[] markNew;

    public EpisodeAdapter(Activity a) {
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public ImageView newtag;
        public TextView text;
        public TextView dura;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.title);
            dura = (TextView) itemView.findViewById(R.id.dur);
            newtag = (ImageView) itemView.findViewById(R.id.newtag);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            String title;
            String aurls[];
            String vurls[];
            Toast.makeText(view.getContext(), "click", Toast.LENGTH_SHORT).show();

            aurls = data.get(text.getText().toString());
            vurls = data.get(text.getText().toString());
            title = text.getText().toString();

            Intent p = new Intent(view.getContext(), JBPlayer.class);
            p.putExtra("aLink", aurls[1]);
            p.putExtra("vLink", vurls[1]);
            if (!aurls[3].equalsIgnoreCase("X")) {
                p.putExtra("pic", aurls[3]);
            } else {
                p.putExtra("pic", vurls[3]);
            }
            p.putExtra("title", title);
            p.putExtra("sum", aurls[5]);
            view.getContext().startActivity(p);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int type) {
        View v = inflater.inflate(R.layout.episodelist_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String title = titles.get(position);
        String duration = null;
        try {
            duration = data.get(titles.get(position))[4];
        } catch (Exception e) {
            int z = 0;
        }

        if (markNew.length - 1 > position) {
            if (markNew[position]) {
                holder.newtag.setImageResource(R.drawable.newtag);
            } else {
                holder.newtag.setImageResource(R.drawable.nonewtag);
            }
        }
        holder.dura.setText(duration);
        holder.text.setText(title);
    }

    @Override
    public int getItemCount() {
        if (data != null)
            return data.size();
        else
            return 0;
    }

    public void add(List<String> t, Hashtable<String, String[]> table) {
        int position = data.size();
        data.putAll(table);
        titles.addAll(t);
        int np = data.size();

        notifyItemRangeInserted(position, np-position);
    }

    public void addNew(List<String> t, Hashtable<String, String[]> table, boolean[] aNew) {
        data = table;
        titles = t;
        markNew = aNew;
        for (int i = 0; i < data.size();i++)
            notifyItemInserted(i);
    }

    public void clear() {
        int t = data.size();
        data.clear();
        titles.clear();
        markNew = null;

        notifyItemRangeRemoved(0, t);

    }

}





