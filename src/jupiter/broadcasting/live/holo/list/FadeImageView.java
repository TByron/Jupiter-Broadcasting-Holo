package jupiter.broadcasting.live.holo.list;

/**
 * Created by b on 2013.09.27..
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;

import com.android.volley.toolbox.NetworkImageView;

public class FadeImageView extends NetworkImageView {

    private static final int FADE_IN_TIME_MS = 250;

    public FadeImageView(Context context) {
        super(context);
    }

    public FadeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FadeImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        TransitionDrawable td = new TransitionDrawable(new Drawable[]{
                new ColorDrawable(android.R.color.transparent),
                new BitmapDrawable(getContext().getResources(), bm)
        });

        setImageDrawable(td);
        td.startTransition(FADE_IN_TIME_MS);
    }
}