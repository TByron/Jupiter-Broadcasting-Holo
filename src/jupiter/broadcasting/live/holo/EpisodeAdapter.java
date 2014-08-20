package jupiter.broadcasting.live.holo;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Hashtable;
import java.util.List;


public class EpisodeAdapter extends BaseAdapter {

    private static LayoutInflater inflater = null;
    private Hashtable<String, String[]> data;
    private List<String> titles;
    private boolean[] markNew;

    public EpisodeAdapter(Activity a, List<String> t, Hashtable<String, String[]> table, boolean[] aNew) {
        data = table;
        titles = t;
        markNew = aNew;
        inflater = (LayoutInflater) a.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return titles.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void add(List<String> t, Hashtable<String, String[]> table) {
        data.putAll(table);
        titles.addAll(t);
        boolean[] g = new boolean[50];
        //markNew = g;
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        ElementHolder eHolder;
        View view = convertView;
        eHolder = new ElementHolder();
        if (convertView == null) {
            view = inflater.inflate(R.layout.episodelist_item, parent, false);

            eHolder.text = (TextView) view.findViewById(R.id.title);
            eHolder.dura = (TextView) view.findViewById(R.id.dur);
            eHolder.newtag = (ImageView) view.findViewById(R.id.newtag);

            view.setTag(eHolder);
        } else {
            eHolder = (ElementHolder) view.getTag();
        }

        String title = titles.get(position);
        String duration = null;
        try {
            duration = data.get(titles.get(position))[4];
        } catch (Exception e) {
            int z = 0;
        }

        if (markNew.length - 1 > position) {
            if (markNew[position]) {
                eHolder.newtag.setImageResource(R.drawable.newtag);
            } else {
                eHolder.newtag.setImageResource(R.drawable.nonewtag);
            }
        }
        eHolder.dura.setText(duration);
        eHolder.text.setText(title);

        return view;
    }

    static class ElementHolder {
        ImageView newtag;
        TextView text;
        TextView dura;
    }

}





