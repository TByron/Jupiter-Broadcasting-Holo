package jupiter.broadcasting.live.holo;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.google.sample.castcompanionlibrary.cast.BaseCastManager;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;

/*
 * Copyright (c) 2012 Shane Quigley
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 * 
 * @author Shane Quigley
 * @hacked Adam Szabo
 */
public class Home extends ActionBarActivity {

    VideoCastManager mVideoCastManager;
    boolean[] av_quality;
    static String[] audio;
    static String video;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.startscreen);

        BaseCastManager.checkGooglePlaySevices(this);
        mVideoCastManager = JBApplication.getVideoCastManager(this);
        mVideoCastManager.reconnectSessionIfPossible(this, true);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        av_quality = new boolean[2];
        //audio quality
        av_quality[0] = sharedPref.getBoolean("pref_sync_audio", false);
        //video quality
        av_quality[1] = sharedPref.getBoolean("pref_sync_video", false);

        audio = new String[2];
        audio[0] = "http://jblive.fm/";
        audio[1] = "http://jblive.am/";
        video = "http://jblive.videocdn.scaleengine.net/jb-live/play/jblive.stream/playlist.m3u8";


        final Button play = (Button) this.findViewById(R.id.button1);
        ImageView pic = (ImageView) this.findViewById(R.id.imageView1);
        pic.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.jupiterbroadcasting.com"));
                startActivity(i);
            }
        }
        );
        Button donate = (Button) this.findViewById(R.id.button3);
        donate.setOnClickListener(new OnClickListener() {

            public void onClick(View arg0) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.jupiterbroadcasting.com/support-us/"));
                startActivity(i);

            }
        });
        Button rss = (Button) this.findViewById(R.id.button2);
        rss.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), ShowActivity.class);
                myIntent.putExtra("aQ", av_quality[0]);
                myIntent.putExtra("vQ", av_quality[1]);
                startActivityForResult(myIntent, 0);
            }
        });

        Button cat = (Button) this.findViewById(R.id.button4);
        cat.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                Intent myIntent2 = new Intent(v.getContext(), Catalogue.class);
                startActivityForResult(myIntent2, 0);
            }
        });

        play.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent p = new Intent(getBaseContext(), JBPlayer.class);

                p.putExtra("title", "JB Live");
                p.putExtra("offline", true);
                p.putExtra("loc", "-1");
                p.putExtra("aLink", audio[av_quality[0] ? 0 : 1]);
                p.putExtra("vLink", video);
                startActivity(p);
            }
        });

    }

    @Override
    protected void onResume(){
        super.onResume();
        mVideoCastManager = JBApplication.getVideoCastManager(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        mVideoCastManager.addMediaRouterButton(menu,
                R.id.media_route_menu_item);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
