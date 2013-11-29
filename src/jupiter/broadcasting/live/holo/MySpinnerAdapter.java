package jupiter.broadcasting.live.holo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/*
 * Copyright (c) 2013 Adam Szabo
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 *
 * @author Adam Szabo
 *
 */

public class MySpinnerAdapter extends BaseAdapter {
    LayoutInflater inflator;
    String[][] sizeArr;

    public MySpinnerAdapter(Context applicationContext, String[][] sizearray) {
        inflator = LayoutInflater.from(applicationContext);
        sizeArr = sizearray;
    }

    @Override
    public int getCount() {
        return sizeArr.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflator.inflate(R.layout.myspinner_item, null);
        TextView ty = (TextView) convertView.findViewById(R.id.type);
        ty.setText(sizeArr[0][position]);

        TextView si = (TextView) convertView.findViewById(R.id.size);
        si.setText(sizeArr[1][position]);
        return convertView;
    }
}
