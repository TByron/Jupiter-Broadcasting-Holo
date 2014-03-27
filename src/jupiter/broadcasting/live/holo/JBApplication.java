package jupiter.broadcasting.live.holo;

import android.app.Application;
import android.content.Context;
import android.widget.Toast;

import com.google.sample.castcompanionlibrary.cast.VideoCastManager;
import com.google.sample.castcompanionlibrary.cast.callbacks.VideoCastConsumerImpl;

/*
 * Copyright (c) 2014 Adam Szabo
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 *
 * @author Adam Szabo
 *
 */

public class JBApplication extends Application {
    static VideoCastManager mCastMgr;
    static String APPLICATION_ID = "7BF7A86F";
    //static String APPLICATION_ID = "E0DA6287";
    //Base class to display the Cast menu everywhere

    public static VideoCastManager getVideoCastManager(Context ctx) {
        if (null == mCastMgr) {
            mCastMgr = VideoCastManager.initialize(ctx, APPLICATION_ID, null, null);
            mCastMgr.enableFeatures(VideoCastManager.FEATURE_NOTIFICATION |
                    VideoCastManager.FEATURE_LOCKSCREEN |
                    VideoCastManager.FEATURE_DEBUGGING);
        }
        mCastMgr.setContext(ctx);
        mCastMgr.setStopOnDisconnect(true);
        return mCastMgr;
    }



}
