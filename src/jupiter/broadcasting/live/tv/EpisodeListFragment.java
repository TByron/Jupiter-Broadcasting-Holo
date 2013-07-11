package jupiter.broadcasting.live.tv;

/*
 * Copyright (c) 2012 Shane Quigley
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 *
 * @author Shane Quigley
 * @hacked Adam Szabo
 */

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import java.util.Hashtable;
import java.util.List;

import jupiter.broadcasting.live.tv.parser.RssHandler;
import jupiter.broadcasting.live.tv.parser.SaxRssParser;


public class EpisodeListFragment extends SherlockFragment {

    List<String> episodes;
    String afeed, vfeed;
    Hashtable<String, String[]> arssLinkTable;
    Hashtable<String, String[]> vrssLinkTable;
    ListView asyncResultView;
    View v;
    com.actionbarsherlock.view.ActionMode mMode;
    String aurls[];
    String vurls[];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);

        v = inflater.inflate(R.layout.episodelist_fragment, null);

        Bundle b = getArguments();
        afeed = b.getString("SHOW_AUDIO");
        vfeed = b.getString("SHOW_VIDEO");
        RSS_parse newparse = new RSS_parse();  //do networking in async task SDK>9
        newparse.execute(afeed, vfeed);
        return v;
    }

    public class RSS_parse extends AsyncTask<String, Integer, List<String>> {

        @Override
        protected List<String> doInBackground(String... link) {

            SaxRssParser aparser = new SaxRssParser();
            SaxRssParser vparser = new SaxRssParser();
            RssHandler acustomhandler = new RssHandler("title", "link", 15);
            RssHandler vcustomhandler = new RssHandler("title", "link", 15);
            aparser.setRssHadler(acustomhandler);
            vparser.setRssHadler(vcustomhandler);
            arssLinkTable = aparser.parse(link[0]);
            vrssLinkTable = vparser.parse(link[1]);
            episodes = aparser.getTitles();

            return episodes;
        }

        @Override
        protected void onPostExecute(List<String> args) {

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(v.getContext(),
                    android.R.layout.simple_list_item_1, android.R.id.text1, args);

            asyncResultView = (ListView) v.findViewById(R.id.episodelist);
            asyncResultView.setAdapter(adapter);

            asyncResultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    aurls = arssLinkTable.get(parent.getAdapter().getItem(position));
                    vurls = vrssLinkTable.get(parent.getAdapter().getItem(position));
                    mMode = getSherlockActivity().startActionMode(new EpisodeActionMode());
                }
            });
            getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        }

        private final class EpisodeActionMode implements com.actionbarsherlock.view.ActionMode.Callback {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                menu.add(1, 1, 0, R.string.audio)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                menu.add(1, 2, 0, R.string.video)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                /*ßmenu.add(1, 3, 0, R.string.web)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);*/

                menu.add(1, 4, 0, R.string.notes)
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                //if wifi connected
                ConnectivityManager connectivity = (ConnectivityManager) getSherlockActivity()
                        .getSystemService(Context.CONNECTIVITY_SERVICE);

                NetworkInfo wifiInfo = connectivity
                        .getNetworkInfo(ConnectivityManager.TYPE_WIFI);


                switch (item.getItemId()) {
                    case 1: //audio
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(aurls[1]));
                        i.setDataAndType(Uri.parse(aurls[1]), "audio/mp3");
                        startActivity(i);
                        break;
                    case 2: // video
                        if (wifiInfo == null || wifiInfo.getState() != NetworkInfo.State.CONNECTED) {
                            AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getSherlockActivity());
                            myAlertDialog.setTitle(R.string.alert);
                            myAlertDialog.setMessage(R.string.areyousure);
                            myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // start videostreaming if the user agrees
                                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(vurls[1]));
                                    i.setDataAndType(Uri.parse(vurls[1]), "video/mp4");
                                    startActivity(i);
                                }
                            });

                            myAlertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                }
                            });

                            myAlertDialog.show();
                        } else {
                            Intent j = new Intent(Intent.ACTION_VIEW, Uri.parse(vurls[1]));
                            j.setDataAndType(Uri.parse(vurls[1]), "video/mp4");
                            startActivity(j);
                        }
                        break;
                    case 3: // web
                        Intent k = new Intent(Intent.ACTION_VIEW, Uri.parse(aurls[0]));
                        startActivity(k);
                        break;
                    case 4: //shownotes
                        SherlockFragment fragment = new ShowNotesView();
                        Bundle args = new Bundle();
                        String link = aurls[0];
                        args.putString("Notes",link);
                        fragment.setArguments(args);

                        FragmentManager fragmentManager = getSherlockActivity().getSupportFragmentManager();
                        fragmentManager.beginTransaction().replace(R.id.episodelist, fragment).commit();
                        break;
                }
                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        }
    }

}