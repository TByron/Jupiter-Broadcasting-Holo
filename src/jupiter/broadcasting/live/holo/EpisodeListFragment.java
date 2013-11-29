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

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import jupiter.broadcasting.live.holo.parser.RssHandler;
import jupiter.broadcasting.live.holo.parser.SaxRssParser;

public class EpisodeListFragment extends Fragment {

    List<String> episodes;
    String afeed, vfeed, name;
    Hashtable<String, String[]> arssLinkTable;
    Hashtable<String, String[]> vrssLinkTable;
    ListView asyncResultView;
    SharedPreferences history;
    View v;
    String title;
    String aurls[];
    String vurls[];
    EpisodeAdapter lAdapter;
    boolean first;
    private List<String> titleList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.episodelist_fragment, null);

        asyncResultView = (ListView) v.findViewById(R.id.episodelist);
        asyncResultView.setOnScrollListener(new EndlessScrollListener());
        asyncResultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                aurls = arssLinkTable.get(parent.getAdapter().getItem(position));
                vurls = vrssLinkTable.get(parent.getAdapter().getItem(position));
                title = (String) parent.getAdapter().getItem(position);

                Intent p = new Intent(getActivity(), JBPlayer.class);
                p.putExtra("aLink", aurls[1]);
                p.putExtra("vLink", vurls[1]);
                if (!aurls[3].equalsIgnoreCase("X")) {
                    p.putExtra("pic", aurls[3]);
                } else {
                    p.putExtra("pic", vurls[3]);
                }
                p.putExtra("title", title);
                p.putExtra("sum", aurls[5]);
                startActivity(p);
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

    /*
        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

            setMenuVisibility(false);


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
                            // start video streaming if the user agrees
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(vurls[1]));
                            i.setDataAndType(Uri.parse(vurls[1]), "video/mp4");
                            startActivity(i);

                } else {
                    Intent j = new Intent(getActivity(), JBPlayer.class);
                    j.putExtra("Title", title);
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

            return super.onOptionsItemSelected(item);
        }
    */
    public class EndlessScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 4;
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
            aparser.setRssHandler(acustomhandler);
            vparser.setRssHandler(vcustomhandler);

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
                    lAdapter = new EpisodeAdapter(getActivity(), titleList, vrssLinkTable, checkNew());
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

        private boolean[] checkNew() throws ParseException {

            boolean[] newCount = new boolean[titleList.size()];
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
            try {
                String lastTitle = history.getString("Y", formatter.format(new Date(0)));
                //testing
                //lastTitle = formatter.format(new Date(0));

                Date olddate = formatter.parse(lastTitle);


                for (int i = 0; i < titleList.size(); i++) {
                    Date newdate = formatter.parse(vrssLinkTable.get(titleList.get(i))[2]);
                    if (newdate.after(olddate)) {
                        //something new
                        newCount[i] = true;
                    } else {
                        //found the newest we saw last time
                        break;
                    }
                }
            } catch (Exception ignored) {

            }

            SharedPreferences.Editor editor = history.edit();
            //saving the latest episode as publication date
            editor.putString("Y", vrssLinkTable.get(titleList.get(0))[2]);
            editor.commit();
            return newCount;
        }


    }
}
