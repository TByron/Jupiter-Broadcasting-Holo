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

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;

import it.gmariotti.recyclerview.itemanimator.SlideInOutRightItemAnimator;
import jupiter.broadcasting.live.holo.parser.RssHandler;
import jupiter.broadcasting.live.holo.parser.SaxRssParser;


public class ShowActivity extends Activity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    public Hashtable<String, String> audioFeedTable;
    public Hashtable<String, String> videoFeedTable;
    public String[] shows;

    //list parameters
    List<String> episodes;
    String afeed, vfeed, name;
    Hashtable<String, String[]> arssLinkTable;
    Hashtable<String, String[]> vrssLinkTable;
    RecyclerView asyncResultView;
    SharedPreferences history;
    View v;
    String title;
    String aurls[];
    String vurls[];
    EpisodeAdapter lAdapter;
    boolean first;
    private List<String> titleList;
    final Activity ctx = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);


        sharedPref.getBoolean("pref_sync_audio", false);
        shows = new String[]{getString(R.string.allshows),
                "BSD Now",
                "Coder Radio",
                "Faux Show",
                "Linux Action Show",
                "LINUX Unplugged",
                "Plan B",
                "SciByte",
                "Tech Talk Today",
                "Techsnap",
                "Unfilter"};

        audioFeedTable = new Hashtable<String, String>();
        audioFeedTable.put(getString(R.string.allshows), "http://feeds.feedburner.com/JupiterBroadcasting");
        audioFeedTable.put("Coder Radio", "http://feeds.feedburner.com/coderradiomp3");
        audioFeedTable.put("Faux Show", "http://www.jupiterbroadcasting.com/feeds/FauxShowMP3.xml");
        audioFeedTable.put("Linux Action Show", "http://feeds.feedburner.com/TheLinuxActionShow");
        audioFeedTable.put("LINUX Unplugged", "http://feeds.feedburner.com/linuxunplugged");
        audioFeedTable.put("SciByte", "http://feeds.feedburner.com/scibyteaudio");
        audioFeedTable.put("Techsnap", "http://feeds.feedburner.com/techsnapmp3");
        audioFeedTable.put("Unfilter", "http://www.jupiterbroadcasting.com/feeds/unfilterMP3.xml");
        audioFeedTable.put("Plan B", "http://feeds.feedburner.com/planbmp3");
        audioFeedTable.put("BSD Now", "http://feeds.feedburner.com/BsdNowMp3");
        audioFeedTable.put("Tech Talk Today", "http://feedpress.me/t3mp3");

        videoFeedTable = new Hashtable<String, String>();
        videoFeedTable.put(getString(R.string.allshows), "http://feeds2.feedburner.com/AllJupiterVideos");
        videoFeedTable.put("Coder Radio", "http://feeds.feedburner.com/coderradiovideo");
        videoFeedTable.put("LINUX Unplugged", "http://feeds.feedburner.com/linuxunvid");
        videoFeedTable.put("SciByte", "http://feeds.feedburner.com/scibytelarge");
        videoFeedTable.put("Plan B", "http://feeds.feedburner.com/PlanBVideo");
        videoFeedTable.put("Tech Talk Today", "http://feedpress.me/t3mob");


        if (sharedPref.getBoolean("pref_sync_video", false)) {
            //load low quality video feeds
            videoFeedTable.put("Techsnap", "http://feeds.feedburner.com/techsnapmobile");
            videoFeedTable.put("Linux Action Show", "http://feeds.feedburner.com/linuxactionshowipodvid");
            videoFeedTable.put("Faux Show", "http://www.jupiterbroadcasting.com/feeds/FauxShowMobile.xml");
            videoFeedTable.put("BSD Now", "http://feeds.feedburner.com/BsdNowMobile");
            videoFeedTable.put("Unfilter", "http://www.jupiterbroadcasting.com/feeds/unfilterMob.xml");

        } else {
            //if set, load high quality videos
            videoFeedTable.put("Techsnap", "http://feeds.feedburner.com/techsnaphd");
            videoFeedTable.put("Linux Action Show", "http://feeds.feedburner.com/linuxashd");
            videoFeedTable.put("Faux Show", "http://www.jupiterbroadcasting.com/feeds/FauxShowHD.xml");
            videoFeedTable.put("BSD Now", "http://feeds.feedburner.com/BsdNowHd");
            videoFeedTable.put("Unfilter", "http://www.jupiterbroadcasting.com/feeds/unfilterHD.xml");
        }

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.main);

        mTitle = mDrawerTitle = getTitle();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.showlist);

        asyncResultView = (RecyclerView) findViewById(R.id.episodelist);
        asyncResultView.setItemAnimator(new SlideInOutRightItemAnimator(asyncResultView));
        asyncResultView.setHasFixedSize(true);
        asyncResultView.setAdapter(lAdapter = new EpisodeAdapter(this));
        LinearLayoutManager layoutManager = new LinearLayoutManager(ctx);
        asyncResultView.setLayoutManager(layoutManager);

        // set a custom shadow that overlays the main content when the drawer opens
        //mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with shows
        mDrawerList.setAdapter(new ArrayAdapter<String>(this,
                R.layout.drawer_list_item, shows));

        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        // enable ActionBar app icon to behave as action to toggle nav drawer (Not working for some reason)
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(mTitle);

            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(mDrawerTitle);

            }
        };
        //set the drawer icon to be clickable
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        //first start opens the drawer
        if (savedInstanceState == null) {
            mDrawerLayout.openDrawer(mDrawerList);
        }

        /*asyncResultView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            private int visibleThreshold = 4;
            private int currentPage = 0;
            private int previousTotal = 0;
            private boolean loading = false;
            @Override
            public void onScrollStateChanged(int i) {
                int k =0;

            }

            @Override
            public void onScrolled(int visibleItemCount, int firstVisibleItem) {
                if (loading && (visibleItemCount + 10 < firstVisibleItem)) {
                    loading = false;
                }

                if (!loading && (visibleItemCount + 10 > firstVisibleItem) ) {
                    // load the next page of shows using a background task
                    currentPage++;
                    //Progress(true);
                    RSS_parse scrollparse = new RSS_parse();
                    //scrollparse.execute(afeed, vfeed, String.valueOf(currentPage));
                    loading = true;
                }
            }
        });*/

    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position); //start fragment to download items for the selected show
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (item.getItemId() == android.R.id.home) {

            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        // update the main content by replacing fragments

//        android.app.Fragment fragment = new EpisodeListFragment();
//        Bundle args = new Bundle();
        afeed = audioFeedTable.get(shows[position]);
        vfeed = videoFeedTable.get(shows[position]);
        name = shows[position];
//        args.putInt("SHOW_ID", position);
//        args.putString("SHOW_AUDIO", afeed);
//        args.putString("SHOW_VIDEO", vfeed);
//        args.putString("SHOW_NAME", shows[position]);
//        fragment.setArguments(args);
//
//        android.app.FragmentManager fragmentManager = getFragmentManager();
//        android.app.FragmentTransaction ft = fragmentManager.beginTransaction();
//        //ft.setCustomAnimations(android.R.anim.fade_in,android.R.anim.fade_out);
//        ft.replace(R.id.episodelist, fragment).commit();

        first = true;
        history = getSharedPreferences(name, 0);
        //Progress(true);
        RSS_parse newparse = new RSS_parse();  //do networking in async task SDK>9
        newparse.execute(afeed, vfeed, "0");

        // update selected item and title, then close the drawer
        mDrawerList.setItemChecked(position, true);
        setTitle(shows[position]);
        mDrawerLayout.closeDrawer(mDrawerList);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
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
                    if (lAdapter.getItemCount() > 0)
                        lAdapter.clear();

                    lAdapter.addNew(titleList, vrssLinkTable, checkNew());
                    first = false;
                } else {
                    lAdapter.add(titleList, vrssLinkTable);
                }
            } catch (Exception e) {
                Log.e("image catch: ", e.toString());
            }
            //Progress(false);
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
            //return newCount;
            return new boolean[titleList.size()];
        }
    }

    /*public void Progress(boolean set) {
        ctx.setProgressBarIndeterminateVisibility(set);
    }*/
}
