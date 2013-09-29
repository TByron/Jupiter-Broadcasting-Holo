package jupiter.broadcasting.live.holo;

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
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.GridView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jupiter.broadcasting.live.holo.parser.RssHandler;
import jupiter.broadcasting.live.holo.parser.SaxRssParser;


public class EpisodeListFragment extends Fragment {

    List<String> episodes;
    String afeed, vfeed, name;
    Hashtable<String, String[]> arssLinkTable;
    Hashtable<String, String[]> vrssLinkTable;
    GridView asyncResultView;
    SharedPreferences history;
    View v;
    String title;
    String aurls[];
    String vurls[];
    LazyAdapter lAdapter;
    boolean first;
    Fragment mFragment1;
    int opId;
    private List<String> titleList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        v = inflater.inflate(R.layout.episodelist_fragment, null);

        mFragment1 = this;
        mFragment1.setHasOptionsMenu(true);
        mFragment1.setMenuVisibility(false);

        opId = 555;
        asyncResultView = (GridView) v.findViewById(R.id.episodelist);
        asyncResultView.setOnScrollListener(new EndlessScrollListener());
        asyncResultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                aurls = arssLinkTable.get(parent.getAdapter().getItem(position));
                vurls = vrssLinkTable.get(parent.getAdapter().getItem(position));
                title = (String) parent.getAdapter().getItem(position);

                if (mFragment1.isMenuVisible() && opId == position) {
                    mFragment1.setMenuVisibility(false);
                } else {
                    mFragment1.setMenuVisibility(true);
                    opId = position;
                }
            }
        });
        Bundle b = getArguments();
        afeed = b.getString("SHOW_AUDIO");
        vfeed = b.getString("SHOW_VIDEO");
        name = b.getString("SHOW_NAME");

        first = true;
        history = getActivity().getSharedPreferences(name, 0);
        getActivity().setProgressBarIndeterminateVisibility(true);
        RSS_parse newparse = new RSS_parse();  //do networking in async task SDK>9
        newparse.execute(afeed, vfeed, "0");

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItemCompat.setShowAsAction(menu.add(R.string.audio), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menu.add(R.string.video), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        MenuItemCompat.setShowAsAction(menu.add(R.string.notes), MenuItem.SHOW_AS_ACTION_IF_ROOM);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        setMenuVisibility(false);
        //if wifi connected
        ConnectivityManager connectivity = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiInfo = connectivity
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (item.getTitle().equals(getString(R.string.notes))) {
            Fragment fragment = new ShowNotesView();
            Bundle args = new Bundle();
            String link = aurls[0];
            args.putString("Notes", link);
            fragment.setArguments(args);

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();
            ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
            ft.addToBackStack(null);
            ft.replace(R.id.episodelist, fragment).commit();

            return true;
        }
        if (item.getTitle().equals(getString(R.string.video))) {
            if (wifiInfo == null || wifiInfo.getState() != NetworkInfo.State.CONNECTED) {
                AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getActivity());
                myAlertDialog.setTitle(R.string.alert);
                myAlertDialog.setMessage(R.string.areyousure);
                myAlertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        // start video streaming if the user agrees
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
                //Intent j = new Intent(Intent.ACTION_VIEW, Uri.parse(vurls[1]));
                //j.setDataAndType(Uri.parse(vurls[1]), "video/mp4");
                //startActivity(j);
                Intent j = new Intent(getActivity(),JBPlayer.class);
                j.putExtra("Title",title);
                j.putExtra("Link", vurls[1]);
                startActivity(j);

            }
            return true;
        }
        if (item.getTitle().equals(getString(R.string.audio))) {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(aurls[1]));
            i.setDataAndType(Uri.parse(aurls[1]), "audio/mp3");
            startActivity(i);
            return true;
        }
        if (item.getItemId() == 3) {

        }
        return super.onOptionsItemSelected(item);
    }

    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public EndlessScrollListener() {
        }

        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                // load the next page of shows using a background task
                //getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
                currentPage++;
                getActivity().setProgressBarIndeterminateVisibility(true);
                RSS_parse scrollparse = new RSS_parse();
                scrollparse.execute(afeed, vfeed, String.valueOf(currentPage));
                loading = true;

            }
        }
    }

    public class RSS_parse extends AsyncTask<String, Integer, List<String>> {
        @Override
        protected List<String> doInBackground(String... link) {
            int page = Integer.parseInt(link[2]);
            SaxRssParser aparser = new SaxRssParser();
            SaxRssParser vparser = new SaxRssParser();
            RssHandler acustomhandler = new RssHandler("title", "link", page);
            RssHandler vcustomhandler = new RssHandler("title", "link", page);
            aparser.setRssHadler(acustomhandler);
            vparser.setRssHadler(vcustomhandler);

            if (first) {
                arssLinkTable = aparser.parse(link[0]);
                vrssLinkTable = vparser.parse(link[1]);

            } else {
                arssLinkTable.putAll(aparser.parse(link[0]));
                vrssLinkTable.putAll(vparser.parse(link[1]));
            }
            episodes = vparser.getTitles();

            return episodes;
        }

        @Override
        protected void onPostExecute(List<String> args) {
            titleList = args;
            try {
                if (first) {
                    lAdapter = new LazyAdapter(getActivity(), titleList, vrssLinkTable, checkNew());
                    asyncResultView.setAdapter(lAdapter);
                    first = false;

                } else {
                    lAdapter.add(titleList, vrssLinkTable);
                }
            } catch (Exception e) {
                Log.e("image catch: ", e.toString());
            }
            getActivity().setProgressBarIndeterminateVisibility(false);
        }

        private ArrayList<Boolean> checkNew() {
            ArrayList<Boolean> newList = new ArrayList<Boolean>();
            String s = name;

            String lastTitle = history.getString("X", "0");

            boolean finished = false;
            for (int i = 0; i < titleList.size(); i++) {
                if (lastTitle.equalsIgnoreCase(titleList.get(0)) || finished) {
                    //nothing new, fill the array with zeroes
                    newList.add(false);
                } else {
                    //something new, find and mark the new
                    newList.add(true);
                    if (lastTitle.equalsIgnoreCase(titleList.get(i))) {
                        newList.remove(i);
                        newList.add(false);
                        finished = true;
                    }
                }
            }
            SharedPreferences.Editor editor = history.edit();
            editor.putString("X", titleList.get(0));
            editor.commit();
            return newList;
        }
    }
}
