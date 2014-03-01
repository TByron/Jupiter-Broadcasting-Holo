package jupiter.broadcasting.live.holo;

import android.graphics.Bitmap;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaStatus;
import com.google.sample.castcompanionlibrary.*;
import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.exceptions.CastException;
import com.google.sample.castcompanionlibrary.cast.exceptions.NoConnectionException;
import com.google.sample.castcompanionlibrary.cast.exceptions.TransientNetworkDisconnectionException;
import com.google.sample.castcompanionlibrary.cast.player.IVideoCastController;
import com.google.sample.castcompanionlibrary.cast.player.OnVideoCastControllerListener;
import com.google.sample.castcompanionlibrary.cast.player.VideoCastControllerActivity;
import com.google.sample.castcompanionlibrary.cast.player.VideoCastControllerFragment;
import com.google.sample.castcompanionlibrary.utils.LogUtils;
import com.google.sample.castcompanionlibrary.utils.Utils;

import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGD;
import static com.google.sample.castcompanionlibrary.utils.LogUtils.LOGE;

/**
 * Created by b on 2014.03.01..
 */
public class JBCastControllerActivity extends VideoCastControllerActivity implements IVideoCastController {


        private static final String TAG = LogUtils.makeLogTag(JBCastControllerActivity.class);
        private VideoCastManager mCastManager;
        private View mPageView;
        private ImageView mPlayPause;
        private TextView mStart;
        private TextView mEnd;
        private SeekBar mSeekbar;
        private TextView mLine1;
        private TextView mLine2;
        private ProgressBar mLoading;
        private float mVolumeIncrement;
        private View mControllers;
        private Drawable mPauseDrawable;
        private Drawable mPlayDrawable;
        private Drawable mStopDrawable;
        private VideoCastControllerFragment mediaAuthFragment;
        private OnVideoCastControllerListener mListener;
        private int mStreamType;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(com.google.sample.castcompanionlibrary.R.layout.cast_activity);
            loadAndSetupViews();
            mVolumeIncrement = Utils.getFloatFromPreference(
                    this, VideoCastManager.PREFS_KEY_VOLUME_INCREMENT);
            try {
                mCastManager = VideoCastManager.getInstance(this);
            } catch (CastException e) {
                // logged already
            }

            setupActionBar();
            Bundle extras = getIntent().getExtras();
            if (null == extras) {
                finish();
                return;
            }

            FragmentManager fm = getSupportFragmentManager();
            mediaAuthFragment = (VideoCastControllerFragment) fm.findFragmentByTag("task");

            // if fragment is null, it means this is the first time, so create it
            if (mediaAuthFragment == null) {
                mediaAuthFragment = VideoCastControllerFragment.newInstance(extras);
                fm.beginTransaction().add(mediaAuthFragment, "task").commit();
                mListener = mediaAuthFragment;
                setOnVideoCastControllerChangedListener(mListener);
            } else {
                mListener = mediaAuthFragment;
                mListener.onConfigurationChanged();
            }
        }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            super.onCreateOptionsMenu(menu);
            getMenuInflater().inflate(com.google.sample.castcompanionlibrary.R.menu.cast_player_menu, menu);
            mCastManager.addMediaRouterButton(menu, com.google.sample.castcompanionlibrary.R.id.media_route_menu_item);
            return true;
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            if (item.getItemId() == android.R.id.home) {
                finish();
            }
            return true;
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (mVolumeIncrement == Float.MIN_VALUE) {
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                onVolumeChange(mVolumeIncrement);
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                onVolumeChange(-(double) mVolumeIncrement);
            } else {
                return super.onKeyDown(keyCode, event);
            }
            return true;
        }

        private void onVolumeChange(double volumeIncrement) {
            if (mCastManager == null) {
                return;
            }
            try {
                mCastManager.incrementVolume(volumeIncrement);
            } catch (Exception e) {
                LOGE(TAG, "onVolumeChange() Failed to change volume", e);
                Utils.showErrorDialog(JBCastControllerActivity.this,
                        com.google.sample.castcompanionlibrary.R.string.failed_setting_volume);
            }
        }

        @Override
        protected void onResume() {
            LOGD(TAG, "onResume() was called");
            try {
                mCastManager = VideoCastManager.getInstance(JBCastControllerActivity.this);
            } catch (CastException e) {
                // logged already
            }

            super.onResume();
        }

        private void loadAndSetupViews() {
            mPauseDrawable = getResources().getDrawable(com.google.sample.castcompanionlibrary.R.drawable.ic_av_pause_dark);
            mPlayDrawable = getResources().getDrawable(com.google.sample.castcompanionlibrary.R.drawable.ic_av_play_dark);
            mStopDrawable = getResources().getDrawable(com.google.sample.castcompanionlibrary.R.drawable.ic_av_stop_dark);
            mPageView = findViewById(com.google.sample.castcompanionlibrary.R.id.pageView);
            mPlayPause = (ImageView) findViewById(com.google.sample.castcompanionlibrary.R.id.imageView1);
            mStart = (TextView) findViewById(com.google.sample.castcompanionlibrary.R.id.startText);
            mEnd = (TextView) findViewById(com.google.sample.castcompanionlibrary.R.id.endText);
            mSeekbar = (SeekBar) findViewById(com.google.sample.castcompanionlibrary.R.id.seekBar1);
            mLine1 = (TextView) findViewById(com.google.sample.castcompanionlibrary.R.id.textView1);
            mLine2 = (TextView) findViewById(com.google.sample.castcompanionlibrary.R.id.textView2);
            mLoading = (ProgressBar) findViewById(com.google.sample.castcompanionlibrary.R.id.progressBar1);
            mControllers = findViewById(com.google.sample.castcompanionlibrary.R.id.controllers);

            mPlayPause.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        mListener.onPlayPauseClicked(v);
                    } catch (TransientNetworkDisconnectionException e) {
                        LOGE(TAG, "Failed to toggle playback due to temporary network issue", e);
                        Utils.showErrorDialog(JBCastControllerActivity.this,
                                com.google.sample.castcompanionlibrary.R.string.failed_no_connection_trans);
                    } catch (NoConnectionException e) {
                        LOGE(TAG, "Failed to toggle playback due to network issues", e);
                        Utils.showErrorDialog(JBCastControllerActivity.this,
                                com.google.sample.castcompanionlibrary.R.string.failed_no_connection);
                    } catch (Exception e) {
                        LOGE(TAG, "Failed to toggle playback due to other issues", e);
                        Utils.showErrorDialog(JBCastControllerActivity.this,
                                com.google.sample.castcompanionlibrary.R.string.failed_perform_action);
                    }
                }
            });

            mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        if (null != mListener) {
                            mListener.onStopTrackingTouch(seekBar);
                        }
                    } catch (Exception e) {
                        LOGE(TAG, "Failed to complete seek", e);
                        finish();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    try {
                        if (null != mListener) {
                            mListener.onStartTrackingTouch(seekBar);
                        }
                    } catch (Exception e) {
                        LOGE(TAG, "Failed to start seek", e);
                        finish();
                    }
                }

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress,
                                              boolean fromUser) {
                    mStart.setText(Utils.formatMillis(progress));
                    try {
                        if (null != mListener) {
                            mListener.onProgressChanged(seekBar, progress, fromUser);
                        }
                    } catch (Exception e) {
                        LOGE(TAG, "Failed to set teh progress result", e);
                    }
                }
            });
        }

        private void setupActionBar() {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle(" "); // without a title, the "<" won't show
            getSupportActionBar().setBackgroundDrawable(
                    getResources().getDrawable(com.google.sample.castcompanionlibrary.R.drawable.actionbar_bg_gradient_light));
        }

        @Override
        public void showLoading(boolean visible) {
            mLoading.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
        }

        // -------------- IVideoCastController implementation ---------------- //
        @Override
        public void adjustControllersForLiveStream(boolean isLive) {
            int visibility = isLive ? View.INVISIBLE : View.VISIBLE;
            mEnd.setVisibility(visibility);
            mSeekbar.setVisibility(visibility);
        }

        @Override
        public void setPlaybackStatus(int state) {
            switch (state) {
                case MediaStatus.PLAYER_STATE_PLAYING:
                    mLoading.setVisibility(View.INVISIBLE);
                    mPlayPause.setVisibility(View.VISIBLE);

                    if (mStreamType == MediaInfo.STREAM_TYPE_LIVE) {
                        mPlayPause.setImageDrawable(mStopDrawable);
                    } else {
                        mPlayPause.setImageDrawable(mPauseDrawable);
                    }

                    mLine2.setText(getString(com.google.sample.castcompanionlibrary.R.string.casting_to_device,
                            mCastManager.getDeviceName()));
                    mControllers.setVisibility(View.VISIBLE);
                    break;
                case MediaStatus.PLAYER_STATE_PAUSED:
                    mControllers.setVisibility(View.VISIBLE);
                    mLoading.setVisibility(View.INVISIBLE);
                    mPlayPause.setVisibility(View.VISIBLE);
                    mPlayPause.setImageDrawable(mPlayDrawable);
                    mLine2.setText(getString(com.google.sample.castcompanionlibrary.R.string.casting_to_device,
                            mCastManager.getDeviceName()));
                    break;
                case MediaStatus.PLAYER_STATE_IDLE:
                    mLoading.setVisibility(View.INVISIBLE);
                    mPlayPause.setImageDrawable(mPlayDrawable);
                    mPlayPause.setVisibility(View.VISIBLE);
                    mLine2.setText(getString(com.google.sample.castcompanionlibrary.R.string.casting_to_device,
                            mCastManager.getDeviceName()));
                    break;
                case MediaStatus.PLAYER_STATE_BUFFERING:
                    mPlayPause.setVisibility(View.INVISIBLE);
                    mLoading.setVisibility(View.VISIBLE);
                    mLine2.setText(getString(com.google.sample.castcompanionlibrary.R.string.loading));
                    break;
                default:
                    break;
            }
        }

        @Override
        public void updateSeekbar(int position, int duration) {
            mSeekbar.setProgress(position);
            mSeekbar.setMax(duration);
            mStart.setText(Utils.formatMillis(position));
            mEnd.setText(Utils.formatMillis(duration));
        }

        @SuppressWarnings("deprecation")
        @Override
        public void setImage(Bitmap bitmap) {
            if (null != bitmap) {
                BitmapDrawable bm = new BitmapDrawable(getResources(),bitmap);
                mPageView.setBackgroundDrawable(bm);
            }
        }

        @Override
        public void setLine1(String text) {
            mLine1.setText(text);

        }

        @Override
        public void setLine2(String text) {
            mLine2.setText(text);

        }

        @Override
        public void setOnVideoCastControllerChangedListener(OnVideoCastControllerListener listener) {
            if (null != listener) {
                this.mListener = listener;
            }
        }

        @Override
        public void setStreamType(int streamType) {
            this.mStreamType = streamType;
        }

        @Override
        public void updateControllersStatus(boolean enabled) {
            mControllers.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
            if (enabled) {
                adjustControllersForLiveStream(mStreamType == MediaInfo.STREAM_TYPE_LIVE);
            }
        }

        @Override
        public void closeActivity() {
            finish();
        }

    }

