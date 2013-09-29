package jupiter.broadcasting.live.holo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Window;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

/*
 * Copyright (c) 2013 Adam Szabo
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 *
 * @author Adam Szabo
 *
 */

public class JBPlayer extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.mediaplayer);
        final VideoView videoView = (VideoView) findViewById(R.id.videoView);

        getSupportActionBar().setTitle(getIntent().getStringExtra("Title"));

        videoView.setVideoPath(getIntent().getStringExtra("Link"));
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        videoView.setMediaController(mediaController);
        this.setProgressBarIndeterminateVisibility(true);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Toast.makeText(getBaseContext(), "Started", Toast.LENGTH_LONG).show();
                Progress();
            }
        });
        videoView.start();
    }

    public void Progress() {
        this.setProgressBarIndeterminateVisibility(false);
    }
}
