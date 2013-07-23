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
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.decode.BaseImageDecoder;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import jupiter.broadcasting.live.holo.parser.RssHandler;
import jupiter.broadcasting.live.holo.parser.SaxRssParser;


public class EpisodeListFragment extends SherlockFragment {

    List<String> episodes;
    String afeed, vfeed, name;
    Hashtable<String, String[]> arssLinkTable;
    Hashtable<String, String[]> vrssLinkTable;
    ListView asyncResultView;
    SharedPreferences history;
    View v;
    com.actionbarsherlock.view.ActionMode mMode;
    String aurls[];
    String vurls[];
    LazyAdapter lAdapter;
    boolean first;
    ImageLoader imageLoader;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
        v = inflater.inflate(R.layout.episodelist_fragment, null);

        Context c = getSherlockActivity().getApplicationContext();
        Point size = new Point();
        WindowManager w = getSherlockActivity().getWindowManager();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2){
            w.getDefaultDisplay().getSize(size);
        }else{
            Display d = w.getDefaultDisplay();
            size.x = d.getWidth();
            size.y = d.getHeight();
        }

        File cacheDir = StorageUtils.getCacheDirectory(c);
        ImageLoaderConfiguration iConfig = new ImageLoaderConfiguration.Builder(c)
                .memoryCacheExtraOptions(size.x/3, size.y/5)
                .discCacheExtraOptions(size.x / 3, size.y / 5, Bitmap.CompressFormat.JPEG, 75, null)
                .threadPoolSize(3) // default
                .threadPriority(Thread.NORM_PRIORITY - 1) // default
                .tasksProcessingOrder(QueueProcessingType.FIFO) // default
                .denyCacheImageMultipleSizesInMemory()
                .memoryCache(new LruMemoryCache(2 * 1024 * 1024))
                .memoryCacheSizePercentage(13) // default
                .discCache(new UnlimitedDiscCache(cacheDir)) // default
                .discCacheFileNameGenerator(new HashCodeFileNameGenerator()) // default
                .imageDownloader(new BaseImageDownloader(c)) // default
                .imageDecoder(new BaseImageDecoder(false)) // default
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple()) // default
                .writeDebugLogs()
                .build();
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(iConfig);

        asyncResultView = (ListView) v.findViewById(R.id.episodelist);
        asyncResultView.setOnScrollListener(new EndlessScrollListener());
        asyncResultView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                aurls = arssLinkTable.get(parent.getAdapter().getItem(position));
                vurls = vrssLinkTable.get(parent.getAdapter().getItem(position));
                mMode = getSherlockActivity().startActionMode(new EpisodeActionMode());
            }
        });
        View footerView = inflater.inflate(R.layout.loadingline, null, false);
        asyncResultView.addFooterView(footerView);
        Bundle b = getArguments();
        afeed = b.getString("SHOW_AUDIO");
        vfeed = b.getString("SHOW_VIDEO");
        name = b.getString("SHOW_NAME");
        first = true;
        history = getActivity().getSharedPreferences(name, 0);
        RSS_parse newparse = new RSS_parse();  //do networking in async task SDK>9
        newparse.execute(afeed, vfeed, "0");

        return v;
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
                RSS_parse scrollparse = new RSS_parse();
                scrollparse.execute(afeed, vfeed, String.valueOf(currentPage));
                loading = true;

            }
        }
    }

    private List<String> titleList;

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
            if (page > 0) {
                first = false;
            }
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
                    //adapter = new ArrayAdapter<String>(v.getContext(), android.R.layout.simple_list_item_1, android.R.id.text1, titleList);
                    lAdapter = new LazyAdapter(getSherlockActivity(), imageLoader, titleList, vrssLinkTable, checkNew());
                    asyncResultView.setAdapter(lAdapter);
                    getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
                } else {
                    lAdapter.add(titleList, vrssLinkTable);
                }
            } catch (Exception e) {
                Log.e("image catch: ", e.toString());
            }
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

    private final class EpisodeActionMode implements com.actionbarsherlock.view.ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {

            menu.add(1, 1, 0, R.string.audio)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

            menu.add(1, 2, 0, R.string.video)
                    .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);

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
                    String kep = vurls[2];
                    Intent k = new Intent(Intent.ACTION_VIEW, Uri.parse(kep));
                    startActivity(k);
                    break;
                case 4: //shownotes
                    SherlockFragment fragment = new ShowNotesView();
                    Bundle args = new Bundle();
                    String link = aurls[0];
                    args.putString("Notes", link);
                    fragment.setArguments(args);

                    FragmentManager fragmentManager = getSherlockActivity().getSupportFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
                    ft.replace(R.id.episodelist, fragment).commit();
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
