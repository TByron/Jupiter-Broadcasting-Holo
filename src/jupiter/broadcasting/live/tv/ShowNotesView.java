package jupiter.broadcasting.live.tv;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.SherlockFragment;


/*
 * Copyright (c) 2013 Adam Szabo
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 *
 * @author Adam Szabo
 *
 */


public class ShowNotesView extends SherlockFragment {

    View v;
    WebView wv;
    String link;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle b = getArguments();
        link = b.getString("Notes");

        v = inflater.inflate(R.layout.shownotes_fragment, null);
        wv = (WebView) v.findViewById(R.id.notesview);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageFinished(WebView view, String url){
                wv.loadUrl("javascript:(function() {var%20content=document.getElementsByClassName('shownotes')[0].innerHTML;})()");
                }
        });
        wv.loadUrl(link);

        return v;
    }
}