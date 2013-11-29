package jupiter.broadcasting.live.holo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Vector;

/*
 * Copyright (c) 2013 Adam Szabo
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 *
 * @author Adam Szabo
 *
 */

public class CatalogueAdapter extends BaseAdapter {
    LayoutInflater inflater;
    Vector<String> list;
    Vector<Integer> type;
    Vector<Boolean> check;
    boolean boxvisible;

    public CatalogueAdapter(Context applicationContext) {
        list = new Vector<String>();
        type = new Vector<Integer>();
        check = new Vector<Boolean>();
        boxvisible = false;
        inflater = (LayoutInflater) applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(String file, int t) {
        list.addElement(file);
        type.addElement(t);
        check.addElement(false);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.elementAt(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder viewHolder;

        if (convertView == null) {

            view = inflater.inflate(R.layout.catalogue_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.checkbox = (CheckBox) view.findViewById(R.id.cbox);
            viewHolder.tview = (TextView) view.findViewById(R.id.ctitle);
            viewHolder.imview = (ImageView) view.findViewById(R.id.ctype);

            viewHolder.tview.setText(list.get(position));
            //audio
            if (type.get(position) == 0) {
                viewHolder.imview.setImageResource(R.drawable.ic_action_mic);
            }
            //video
            else if (type.get(position) == 1) {
                viewHolder.imview.setImageResource(R.drawable.ic_action_video);
            }
            viewHolder.checkbox.setVisibility(View.GONE);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.checkbox.setChecked(check.elementAt(position));
        if (boxvisible) {

            viewHolder.checkbox.setVisibility(View.VISIBLE);
        } else
            viewHolder.checkbox.setVisibility(View.GONE);

        return view;
    }

    static class ViewHolder {
        protected TextView tview;
        protected CheckBox checkbox;
        protected ImageView imview;
    }

    public void change(int pos, boolean visibility) {
        //toggle the checkbox visibility and the checked state
        //if invisible, zero out the checked states
        boxvisible = visibility;
        if (visibility) {
            if (check.elementAt(pos))
                check.setElementAt(false, pos);
            else
                check.setElementAt(true, pos);
        } else
            for (int i = 0; i < check.size(); i++)
                check.setElementAt(false, i);
        notifyDataSetChanged();
    }
}
