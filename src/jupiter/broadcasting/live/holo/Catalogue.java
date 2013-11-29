package jupiter.broadcasting.live.holo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
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

public class Catalogue extends ActionBarActivity {
    ListView catList;
    ActionMode mActionMode;
    CatalogueAdapter cadapter;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Vector<String> fileArray = new Vector<String>();
        final Vector<String> type = new Vector<String>();
        final Vector<String> path = new Vector<String>();

        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.episodelist_fragment);
        getSupportActionBar().setTitle(R.string.cat);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        catList = (ListView) findViewById(R.id.episodelist);
        catList.setOnItemLongClickListener(liListener);

        cadapter = new CatalogueAdapter(getApplicationContext());

        File fileList = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PODCASTS + "/JB");
        File[] filenames = fileList.listFiles();
        if (filenames != null) {
            for (File tmpf : filenames) {
                if (tmpf.toString().contains(".mp3")) {
                    path.add(tmpf.toString());
                    String curName = tmpf.toString().split("/")[tmpf.toString().split("/").length - 1];
                    fileArray.add(curName.split(".mp3")[0]);
                    type.add(getString(R.string.audio));
                    cadapter.add(curName.split(".mp3")[0],0);
                } else if (tmpf.toString().contains(".mp4")) {
                    path.add(tmpf.toString());
                    String curName = tmpf.toString().split("/")[tmpf.toString().split("/").length - 1];
                    fileArray.add(curName.split(".mp4")[0]);
                    type.add(getString(R.string.video));
                    cadapter.add(curName.split(".mp4")[0],1);
                }
            }
        }


        catList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode == null) {
                    Intent p = new Intent(getBaseContext(), JBPlayer.class);

                    p.putExtra("title", fileArray.get(position));
                    p.putExtra("offline", true);
                    p.putExtra("loc", path.get(position));
                    int t = type.get(position).equalsIgnoreCase(getString(R.string.audio)) ? 0 : 1;
                    p.putExtra("type", t);
                    startActivity(p);
                }
                else {
                    cadapter.change(position, true);
                }
            }
        });
        catList.setAdapter(cadapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private AdapterView.OnItemLongClickListener liListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (mActionMode != null) {
                return false;
            }
            catList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
            startSupportActionMode(mActionModeCallback);
            cadapter.change(position,true);

            return true; // so this action does not consume the event!!!
        }
    };

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            mActionMode = mode;
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.delete, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false; // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.deletethis:

                    mode.finish(); // Action picked, so close the CAB
                    return true;
                default:
                    return false;
            }
        }

        // Called when the user exits the action mode
        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            cadapter.change(0, false);
        }
    };
}

