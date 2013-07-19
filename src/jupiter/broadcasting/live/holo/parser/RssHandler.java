package jupiter.broadcasting.live.holo.parser;

import android.util.Log;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Hashtable;
import java.util.Vector;

/*
 * Copyright (c) 2012 Shane Quigley
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 * 
 * @author Shane Quigley
 * @hacked Adam Szabo
 */

/**
 * Constructor
 */
public class RssHandler extends DefaultHandler {
    private Vector<String> rssTitles;
    private Vector<String> rssLinks;
    private Vector<String> rssEnclosures;
    private Vector<String> thumbnails;
    private Vector<String> duration;
    private String linkString;
    private String titleString;
    private int counter = 0;
    private int maxRecords = 12;
    private int page = 0;
    private boolean isLink = false;
    private boolean isTitle = false;
    private boolean ifInsideItem = false;
    private boolean donethis = false;
    private boolean isDur = false;
    private StringBuffer toAdd;

    /**
     * Constructor
     */
    public RssHandler() {
        linkString = "link";
        titleString = "title";
        rssTitles = new Vector<String>();
        rssLinks = new Vector<String>();
        rssEnclosures = new Vector<String>();
        thumbnails = new Vector<String>();
        duration = new Vector<String>();
        toAdd = new StringBuffer();
    }

    /*
* Constructer that allows a little more control over parsing the feed
* @param title
* @param link
* @param numberOfRecords The max number of item to be parsed.
*/
    public RssHandler(String title, String link, int targetpage) {
        titleString = title;
        linkString = link;
        rssTitles = new Vector<String>();
        rssLinks = new Vector<String>();
        page = targetpage;
        rssEnclosures = new Vector<String>();
        thumbnails = new Vector<String>();
        duration = new Vector<String>();
        toAdd = new StringBuffer();


    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        if (ifInsideItem) {

            isLink = qName.equalsIgnoreCase(linkString);
            isTitle = qName.equalsIgnoreCase(titleString);
            isDur = qName.equalsIgnoreCase("itunes:duration");
            boolean enclosure = true;
            if (!donethis) {
                if (qName.equalsIgnoreCase("enclosure")) {
                    rssEnclosures.addElement(attributes.getValue("url"));
                }
                if (qName.equalsIgnoreCase("media:thumbnail")) {
                    thumbnails.addElement(attributes.getValue("url"));
                }
            }
        } else {
            ifInsideItem = qName.equalsIgnoreCase("item");
        }
        if (isTitle) {
            if (counter < (maxRecords * page + 1) && page > 0) {
                donethis = true;
            } else {
                donethis = false;
            }
            if (counter > maxRecords * (page + 1)) {
                throw new SAXException("Parsing limit reached");
            }
            Log.e("Counting", "checked this much:" + counter + "on page " + page);
            counter++;
        }
    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isLink && !donethis) {
            rssLinks.addElement(toAdd.toString());
        }else if (isTitle && !donethis) {
            rssTitles.addElement(toAdd.toString());
        }else if (isDur && !donethis) {
            duration.addElement(toAdd.toString());
        }
        toAdd = new StringBuffer();
    }

    public void characters(char ch[], int start, int length) throws SAXException {
        if ((isLink || isTitle || isDur) && !donethis) {
            toAdd.append(new String(ch,start,length));
        }
    }

    public Hashtable<String, String[]> getTable() {
        Hashtable<String, String[]> output = new Hashtable<String, String[]>();
        for (int i = 0; i < rssTitles.size(); i++) {
            try {
                if (thumbnails.size()>0) {
                    if (duration.size()>0){
                        output.put(rssTitles.elementAt(i), new String[]{rssLinks.elementAt(i), rssEnclosures.elementAt(i), thumbnails.elementAt(i), duration.elementAt(i)});
                    }
                    else {
                        output.put(rssTitles.elementAt(i), new String[]{rssLinks.elementAt(i), rssEnclosures.elementAt(i), thumbnails.elementAt(i)});
                    }
                } else if (duration.size()>0){
                    output.put(rssTitles.elementAt(i), new String[]{rssLinks.elementAt(i), rssEnclosures.elementAt(i), duration.elementAt(i)});
                }else{
                    output.put(rssTitles.elementAt(i), new String[]{rssLinks.elementAt(i), rssEnclosures.elementAt(i)});
                }

            } catch (Exception e) {
                Log.e("Woops", e.getMessage());
            }
        }
        return output;
    }

    public Vector<String> getTitles() {
        return rssTitles;
    }
}