package jupiter.broadcasting.live.holo;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

/*
 * Copyright (c) 2012 Shane Quigley
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 * 
 * @author Shane Quigley
 * @hacked Adam Szabo
 */
public class Home extends Activity {
    /**
     * Called when the activity is first created.
     */
    private final int NOTIFICATION_ID = 3434;
    MediaPlayer mp = new MediaPlayer();
    NotificationManager mNotificationManager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String ns = Context.NOTIFICATION_SERVICE;
        mNotificationManager = (NotificationManager) getSystemService(ns);
        setContentView(R.layout.startscreen);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
        mNotificationManager.cancel(NOTIFICATION_ID);
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
                p.putExtra("aLink", "http://jblive.fm/");
                p.putExtra("vLink", "rtsp://videocdn-us.geocdn.scaleengine.net/jblive/jblive.stream");
                startActivity(p);
            }
        });
    }
}