package jupiter.broadcasting.live.holo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import java.io.IOException;

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

        final AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
        play.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (!mp.isPlaying()) {
                    alertbox.setMessage(R.string.whichstream);
                    alertbox.setPositiveButton(R.string.audio, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            try {
                                mp.setDataSource("http://jblive.am/");
                            } catch (IllegalArgumentException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IllegalStateException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            try {
                                mp.prepare();
                            } catch (IllegalStateException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            mp.start();
                            if (mp.isPlaying()) {//Incase there is a network issue and the stream doesn't work
                                play.setText(R.string.pause);
                            }
                        }
                    });
                    alertbox.setNegativeButton(R.string.video, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                            NetworkInfo wifiInfo = connectivity
                                    .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                            if (wifiInfo == null || wifiInfo.getState() != NetworkInfo.State.CONNECTED) {
                                //AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(getParent());
                                alertbox.setTitle(R.string.alert);
                                alertbox.setMessage(R.string.areyousure);
                                alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        // // start videostreaming if the user agrees
                                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("rtsp://videocdn-us.geocdn.scaleengine.net/jblive/jblive.stream"));
                                        startActivity(i);
                                    }
                                });
                                alertbox.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface arg0, int arg1) {
                                    }
                                });
                                alertbox.show();
                            } else {
                                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("rtsp://videocdn-us.geocdn.scaleengine.net/jblive/jblive.stream"));
                                startActivity(i);
                            }
                        }
                    });
                    alertbox.show();
                } else {
                    mp.stop();
                    play.setText(R.string.play);
                }
            }

        }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mNotificationManager.cancel(NOTIFICATION_ID);//For good measure because app pauses before it quits aswell as on pause
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mp.isPlaying()) {
            putNotificationUp(mp.isPlaying());
        } else {
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mp.isPlaying()) {
            mp.stop();
            mp.release();
        }
        mNotificationManager.cancel(NOTIFICATION_ID);//because onPause is called first
    }

    private void putNotificationUp(boolean play) {

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.jb_icon)
                        .setContentTitle("Jupiter Broadcasting")
                        .setContentText(getString(R.string.plaiyinglivestream))
                        .setAutoCancel(true);

        Intent resultIntent = new Intent(this, Home.class);
        // pending intent to call back the already running resultIntent
        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, 0);
        mBuilder.setContentIntent(resultPendingIntent);

        //display action buttons if 4.1+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            //Play-Pause button
            Intent playPause = new Intent("DO_NOTIFICATION_ACTION");
            Bundle playPauseBundle = new Bundle();
            playPauseBundle.putInt("click", 1);//This is the value I want to pass
            playPause.putExtras(playPauseBundle);
            PendingIntent pendingPlayPause = PendingIntent.getActivity(this, 12345, playPause, PendingIntent.FLAG_UPDATE_CURRENT);

            String text = play ? getString(R.string.pause) : getString(R.string.play);
            mBuilder.addAction(android.R.drawable.ic_media_pause, text, pendingPlayPause);

            //Exit button
            Intent stop = new Intent("DO_NOTIFICATION_ACTION");
            Bundle stopBundle = new Bundle();
            stopBundle.putInt("click", 3);//This is the value I want to pass
            stop.putExtras(stopBundle);
            PendingIntent pendingStop = PendingIntent.getActivity(this, 12345, stop, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Exit", pendingStop);

        }
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public class NotificationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle answerBundle = intent.getExtras();
            int event = answerBundle.getInt("click");
            if (event == 1) {
                if (mp.isPlaying()) {
                    mp.stop();
                    putNotificationUp(mp.isPlaying());
                } else {
                    mp.start();
                    putNotificationUp(mp.isPlaying());
                }
            } else if (event == 3) {
                mNotificationManager.cancel(NOTIFICATION_ID);
                getParent().finish();
            }
        }
    }
}