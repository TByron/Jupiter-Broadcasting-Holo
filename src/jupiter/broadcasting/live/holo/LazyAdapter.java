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

import jupiter.broadcasting.live.holo.list.ImageLoader;

public class LazyAdapter extends BaseAdapter {

    private Activity activity;
    private Hashtable<String,String[]> data;
    private List<String> titles;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;

    public LazyAdapter(Activity a, List<String> t, Hashtable<String, String[]> table) {
        activity = a;
        data = table;
        titles = t;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        imageLoader=new ImageLoader(activity.getApplicationContext());
    }

    public int getCount() {
        return data.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    public void add(List<String> t,Hashtable<String, String[]> table){
        data.putAll(table);
        titles.addAll(t);
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;
        if(convertView==null)
            vi = inflater.inflate(R.layout.episodelist_item, null);

        TextView text=(TextView)vi.findViewById(R.id.title);
        ImageView image=(ImageView)vi.findViewById(R.id.thumb);
        text.setText(titles.get(position));
        imageLoader.DisplayImage(data.get(titles.get(position))[2], image);
        return vi;
    }
}





