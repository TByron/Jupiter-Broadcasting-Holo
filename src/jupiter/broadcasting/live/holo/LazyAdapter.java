package jupiter.broadcasting.live.holo;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jupiter.broadcasting.live.holo.list.ImageLoader;


public class LazyAdapter extends BaseAdapter {

    private Activity activity;
    private Hashtable<String, String[]> data;
    private List<String> titles;
    private ArrayList<Boolean> markNew;
    private static LayoutInflater inflater = null;
    ImageLoader imageLoader;


    public LazyAdapter(Activity a, List<String> t, Hashtable<String, String[]> table, ArrayList<Boolean> aNew) {
        activity = a;
        data = table;
        titles = t;
        markNew = aNew;
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader = new ImageLoader(activity.getApplicationContext());
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
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        ElementHolder eHolder;
        if (convertView == null) {
            vi = inflater.inflate(R.layout.episodelist_item, null);
            eHolder = new ElementHolder();
            eHolder.text = (TextView) vi.findViewById(R.id.title);
            eHolder.dura = (TextView) vi.findViewById(R.id.dur);
            eHolder.image = (ImageView) vi.findViewById(R.id.thumb);
            eHolder.newtag = (ImageView) vi.findViewById(R.id.newtag);

            vi.setTag(eHolder);
        } else {
            eHolder = (ElementHolder) vi.getTag();
        }

        String duration = null;
        String url = null;
        String title = null;
        // inconsistent rss formats, so making sure...
        try {
            title = titles.get(position);
            url = data.get(titles.get(position))[2];
            duration = data.get(titles.get(position))[3];
        } catch (Exception e) {
            String err = (e.getMessage() == null) ? "Something wrong" : e.getMessage();
            Log.e("rss error: ", err);
        }

        if (null != duration) {
            eHolder.dura.setText(duration);
        } else {
            eHolder.dura.setText("11:11");
        }
        eHolder.text.setText(title);
        if (url != null) {
            imageLoader.DisplayImage(url, eHolder.image);
        } else {
            eHolder.image.setImageResource(R.drawable.logo2);
        }

        if (markNew.size() > position) {
            if (markNew.get(position)) {
                eHolder.newtag.setImageResource(R.drawable.newtag);
            }
        } else {
            eHolder.newtag.setImageResource(R.drawable.nonewtag);
        }

        return vi;
    }

    static class ElementHolder {
        ImageView image;
        ImageView newtag;
        TextView text;
        TextView dura;
    }
}





