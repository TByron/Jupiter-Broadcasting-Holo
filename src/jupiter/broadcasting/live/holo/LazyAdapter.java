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
    public ImageLoader imageLoader;

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

        if (convertView == null) {
            vi = inflater.inflate(R.layout.episodelist_item, null);
        }
        TextView text = (TextView) vi.findViewById(R.id.title);
        ImageView image = (ImageView) vi.findViewById(R.id.thumb);
        TextView dura = (TextView) vi.findViewById(R.id.dur);
        ImageView newtag = (ImageView) vi.findViewById(R.id.newtag);

        text.setText(titles.get(position));
        // inconsistent rss formats, so making sure...
        try {
            if (data.get(titles.get(position))[3] != null) {
                dura.setText(data.get(titles.get(position))[3]);
            }


        } catch (Exception e) {
            String err = (e.getMessage()==null)?"Something wrong":e.getMessage();
            Log.e("duration catch: ",err);
        }
        try {
            if (data.get(titles.get(position))[2] != null) {
                imageLoader.DisplayImage(data.get(titles.get(position))[2], image);
            }
        }catch (Exception e) {
            String err = (e.getMessage()==null)?"Something wrong":e.getMessage();
            Log.e("image catch: ",err);
        }


        if (markNew.size() > position) {
            if (markNew.get(position)) {
                newtag.setImageResource(R.drawable.newtag);
            }
        } else {
            newtag.setImageResource(R.drawable.nonewtag);
        }

        return vi;
    }
}





