package jupiter.broadcasting.live.holo;

import android.app.Activity;
import android.content.Context;
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
    private Hashtable<String,String[]> data;
    private List<String> titles;
    private ArrayList<Boolean> markNew;
    private static LayoutInflater inflater=null;
    public ImageLoader imageLoader;

    public LazyAdapter(Activity a, List<String> t, Hashtable<String, String[]> table, ArrayList<Boolean> aNew) {
        activity = a;
        data = table;
        titles = t;
        markNew = aNew;
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
    public void add(List<String> t, Hashtable<String, String[]> table){
        data.putAll(table);
        titles.addAll(t);

        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi=convertView;

        if(convertView==null){
            vi = inflater.inflate(R.layout.episodelist_item, null);
        }
        TextView text=(TextView)vi.findViewById(R.id.title);
        ImageView image=(ImageView)vi.findViewById(R.id.thumb);
        TextView dura=(TextView)vi.findViewById(R.id.dur);
        ImageView newtag = (ImageView)vi.findViewById(R.id.newtag);

        text.setText(titles.get(position));
        // inconsistent rss formats, so making sure...
        if (data.get(titles.get(position)).length == 4 ){
            dura.setText(data.get(titles.get(position))[3]);
        }
        if (data.get(titles.get(position))[2] != null){
        imageLoader.DisplayImage(data.get(titles.get(position))[2], image);
        }
        if (markNew.get(position) && (markNew.size()>0)){
            newtag.setImageResource(R.drawable.newtag);
        }else{
            newtag.setImageResource(R.drawable.nonewtag);
        }

        return vi;
    }
}





