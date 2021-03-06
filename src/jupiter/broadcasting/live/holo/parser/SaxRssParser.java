package jupiter.broadcasting.live.holo.parser;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/*
 * Copyright (c) 2012 Shane Quigley
 *
 * This software is MIT licensed see link for details
 * http://www.opensource.org/licenses/MIT
 * 
 * @author Shane Quigley
 */
public class SaxRssParser {
    private SAXParser saxParser;
    private RssHandler handler;
    private Vector<String> titles;

    public SaxRssParser() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            saxParser = factory.newSAXParser();
            handler = new RssHandler();
        } catch (ParserConfigurationException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
    }

    public Hashtable<String, String[]> parse(String rssfeed) {
        try {
            InputSource feedSource = new InputSource(rssfeed);
            saxParser.parse(feedSource, handler);
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (SAXException ex) {
            ex.printStackTrace();
        }
        titles = handler.getTitles();
        return handler.getTable();
    }

    public Vector<String> getTitles() {
        return titles;
    }

    /**
     * Method to allow people to use custom handlers
     */
    public void setRssHandler(RssHandler h) {
        handler = h;
    }

}
