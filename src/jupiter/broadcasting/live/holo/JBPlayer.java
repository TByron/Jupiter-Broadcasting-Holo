package jupiter.broadcasting.live.holo;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ConfigurationInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import jupiter.broadcasting.live.holo.list.BitmapLruCache;
import jupiter.broadcasting.live.holo.list.FadeImageView;

/*
 * Copyright (c) 2013 Adam Szabo
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 *
 * @author Adam Szabo
 *
 */

public class JBPlayer extends ActionBarActivity implements AdapterView.OnItemSelectedListener,
        View.OnSystemUiVisibilityChangeListener {
    int av = 0;
    VideoView videoView;
    FadeImageView iView;
    Button play;
    Button down;
    TextView tw;

    String aLink;
    String vLink;
    String theLink;
    String title;
    NetworkInfo wifiInfo;
    List<String> SpinnerArray = new ArrayList<String>();
    Spinner spinner;
    boolean hasit;
    boolean offline;
    int type;
    private View mDecorView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context ct = this;

        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.mediaplayer);
        String pic = null;
        type = getIntent().getIntExtra("type", 3);
        title = getIntent().getStringExtra("title");
        hasit = HasIt(title, av);
        getSupportActionBar().setTitle(title);
        mDecorView = getWindow().getDecorView();
        mDecorView.setOnSystemUiVisibilityChangeListener(this);

        // going on different routes if coming from Catalogue
        offline = getIntent().getBooleanExtra("offline", false);
        if (!offline && !hasit) {
            pic = getIntent().getStringExtra("pic");
            aLink = getIntent().getStringExtra("aLink");
            vLink = getIntent().getStringExtra("vLink");
            theLink = aLink;
            GetSize newSize = new GetSize();
            newSize.execute();
        } else {
            theLink = getIntent().getStringExtra("loc");
        }

        iView = (FadeImageView) findViewById(R.id.thumb);
        RequestQueue mReqQue = Volley.newRequestQueue(getApplicationContext());
        ImageLoader mImageLoader = new ImageLoader(mReqQue, new BitmapLruCache());

        if ((null != pic) && !pic.equalsIgnoreCase("X")) {
            iView.setImageUrl(pic, mImageLoader);
        }
        if (offline) {
            switch (type) {
                case 0:
                    SpinnerArray.add(getString(R.string.audio));
                    break;
                case 1:
                    SpinnerArray.add(getString(R.string.video));
                    break;
            }
        } else {
            SpinnerArray.add(getString(R.string.audio));
            SpinnerArray.add(getString(R.string.video));
        }
        spinner = (Spinner) findViewById(R.id.AV);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, SpinnerArray);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        tw = (TextView) findViewById(R.id.summary);
        tw.setMovementMethod(new ScrollingMovementMethod());
        tw.setText(getIntent().getStringExtra("sum"));

        play = (Button) findViewById(R.id.player);
        play.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                StartPlay(theLink);
            }
        });


        down = (Button) findViewById(R.id.downer);
        if (offline || hasit) {
            down.setVisibility(View.INVISIBLE);
        }
        down.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final String url = theLink;
                //if wifi not connected, ask to make sure
                ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                wifiInfo = connectivity.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (wifiInfo == null || wifiInfo.getState() != NetworkInfo.State.CONNECTED) {
                    AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(ct);
                    myAlertDialog.setTitle(R.string.alert);
                    myAlertDialog.setMessage(R.string.areyousure2);
                    myAlertDialog.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            DownLoad(url);
                        }
                    });
                    myAlertDialog.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                        }
                    });
                    myAlertDialog.show();
                } else {
                    DownLoad(url);
                }
            }
        });
    }


    public boolean HasIt(String mTitle, int AV) {
        String ext = (AV == 0) ? ".mp3" : ".mp4";
        File fileList = new File(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PODCASTS + "/JB");
        File[] filenames = fileList.listFiles();
        if (filenames != null) {
            for (File tmpf : filenames) {
                if (tmpf.toString().contains(mTitle + ext)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void DownLoad(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        String ver = (av == 0) ? getString(R.string.audio) : getString(R.string.video);
        request.setDescription(getString(R.string.progress) + "(" + ver + ")...");
        request.setTitle(getIntent().getStringExtra("title"));
        // in order for this if to run, you must use the android 3.2 to compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        }
        String ext = (av == 0) ? "mp3" : "mp4";
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PODCASTS + "/JB", getIntent().getStringExtra("title") + "." + ext);

        // get download service and enqueue file
        final DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        final long enqueue = manager.enqueue(request);

        //register receiver to be notified when download finishes
        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(enqueue);
                    Cursor c = manager.query(query);
                    if (c != null) {
                        if (c.moveToFirst()) {
                            int columnIndex = c
                                    .getColumnIndex(DownloadManager.COLUMN_STATUS);
                            if (DownloadManager.STATUS_SUCCESSFUL == c
                                    .getInt(columnIndex)) {
                                Toast.makeText(getBaseContext(), "Finished", Toast.LENGTH_LONG).show();
                                hasit = true;
                            }
                        }
                    }
                }
            }
        };

        registerReceiver(receiver, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    public void Progress() {
        this.setProgressBarIndeterminateVisibility(false);
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!offline) {
            av = position;
            hasit = HasIt(title, av);
            if (position != 0) {
                theLink = vLink;
            } else theLink = aLink;
            if (!hasit)
                down.setVisibility(View.VISIBLE);
            else {
                down.setVisibility(View.INVISIBLE);
                String ext = (av == 0) ? ".mp3" : ".mp4";
                theLink = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PODCASTS + "/JB/" + title + ext;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void StartPlay(String path) {


        videoView = (VideoView) findViewById(R.id.videoView);
        videoView.setVideoPath(path);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        this.setProgressBarIndeterminateVisibility(true);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_LONG).show();
                Progress();
                iView.setVisibility(View.GONE);
                videoView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));
            }
        });
        videoView.start();
    }

    public class GetSize extends AsyncTask<String, Integer, String[]> {
        @Override
        protected String[] doInBackground(String... params) {
            String[] size = new String[]{"", ""};

            try {
                URL url = new URL(aLink);
                URLConnection urlConnection = url.openConnection();
                size[0] = urlConnection.getHeaderField("content-length");
                size[0] = String.valueOf(Integer.parseInt(size[0]) / 1024 / 1024) + " MB";
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                URL url = new URL(vLink);
                URLConnection urlConnection = url.openConnection();
                size[1] = urlConnection.getHeaderField("content-length");
                size[1] = String.valueOf(Integer.parseInt(size[1]) / 1024 / 1024) + " MB";
            } catch (IOException e) {
                e.printStackTrace();
            }

            return size;
        }

        @Override
        protected void onPostExecute(String[] args) {
            String[][] sizearray = new String[2][2];
            sizearray[0][0] = getString(R.string.audio);
            sizearray[0][1] = getString(R.string.video);
            sizearray[1][0] = args[0];
            sizearray[1][1] = args[1];

            MySpinnerAdapter adapter2 = new MySpinnerAdapter(getApplicationContext(), sizearray);
            spinner.setAdapter(adapter2);
            spinner.setSelection(av);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            spinner.setVisibility(View.VISIBLE);
            if (!offline || !hasit)
                down.setVisibility(View.VISIBLE);
            play.setVisibility(View.VISIBLE);
            tw.setVisibility(View.VISIBLE);
            showSystemUI();
            getSupportActionBar().show();
        } else {
            if (videoView.isPlaying()) {
                spinner.setVisibility(View.GONE);
                down.setVisibility(View.GONE);
                play.setVisibility(View.GONE);
                tw.setVisibility(View.GONE);
                hideSystemUI();
                getSupportActionBar().hide();
            }
        }
    }

    private void hideSystemUI() {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void showSystemUI() {
        mDecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        // Detect when we touch the screen on landscape video (which brings back the sysUI)
        // to make it go away again
        if (getResources().getConfiguration().orientation == 2 && visibility == 0){
            hideSystemUI();
        }

    }

}
