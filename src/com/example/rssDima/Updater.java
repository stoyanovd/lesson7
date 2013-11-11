package com.example.rssDima;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xmlpull.v1.XmlPullParserException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class Updater {

    Context context;
    ArrayList<Record> array;
    int lastRecord;
    int id;
    String encode = "utf-8";
    public boolean successfulUpdate = false;
    String defaultEncoding = "ISO-8859-1";

    class MySAXParser extends DefaultHandler {

        public String currentElement = null;

        boolean openDescription = false;
        boolean isStart = false;

        @Override
        public void startDocument() throws SAXException {
            lastRecord = -1;
            array = new ArrayList<Record>();
            openDescription = false;
            isStart = false;
            System.out.println("Start document");
            currentElement = null;
        }

        @Override
        public void startElement(String uri, String local_name, String raw_name, Attributes amap) throws SAXException {

            if ("item".equals(local_name)) {
                lastRecord++;
                array.add(new Record());
                isStart = true;
            } else if (isStart) {
                currentElement = local_name;
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {

            String valueOld = new String(ch, start, length);
            String value = null;
            try {
                value = new String(valueOld.getBytes(defaultEncoding), encode);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            if (isStart) {
                if ("title".equals(currentElement)) {
                    array.set(lastRecord, new Record(array.get(lastRecord).title + value, array.get(lastRecord).summaries, array.get(lastRecord).link));
                } else if ("description".equals(currentElement)) {
                    array.set(lastRecord, new Record(array.get(lastRecord).title, array.get(lastRecord).summaries + value, array.get(lastRecord).link));
                } else if ("link".equals(currentElement))
                    array.set(lastRecord, new Record(array.get(lastRecord).title, array.get(lastRecord).summaries, array.get(lastRecord).link + value));
            }
        }

    }

    private boolean downloadPage(String urlString) throws XmlPullParserException, IOException, ParserConfigurationException, SAXException {

        boolean res = false;
        InputStream stream = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();
        XMLReader xmlReader = parser.getXMLReader();
        MySAXParser saxParser = new MySAXParser();
        xmlReader.setContentHandler(saxParser);

        URL url;
        HttpURLConnection conn;
        try {
            url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(7000);
            conn.setConnectTimeout(10000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            conn.connect();
            stream = conn.getInputStream();
            if (stream == null) {
                res = false;
                System.out.println("error stream");
            } else {
                System.out.println("going to parse_" + urlString);
                Scanner scanner = new Scanner(stream, defaultEncoding);
                String tempString = scanner.nextLine();
                encode = defaultEncoding;
                if (tempString.contains("encoding=")) {
                    encode = "";
                    int i = tempString.indexOf("encoding=") + "encoding=".length() + 1;
                    while (tempString.charAt(i) != '"') {
                        encode += tempString.charAt(i);
                        i++;
                    }
                }
                scanner.close();
                stream.close();

                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(7000);
                conn.setConnectTimeout(10000);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                stream = conn.getInputStream();

                if (stream == null) {
                    res = false;
                    System.out.println("error stream");
                } else {
                    InputSource inputSource = new InputSource(stream);

                    System.out.println("encode " + encode);
                    inputSource.setEncoding(defaultEncoding);
                    xmlReader.parse(inputSource);
                    System.out.println("count_" + array.size());
                    res = true;
                }
            }
        } catch (Exception e) {
            res = false;
            System.out.println("count_bad_" + array.size());
            e.printStackTrace();
        } finally {
            if (stream != null) {
                stream.close();
            }

        }
        return res;
    }

    public Updater(Context _context, int _id, String url) throws ParserConfigurationException, XmlPullParserException, SAXException, IOException {
        id = _id;
        context = _context;
        try {
            successfulUpdate = update(url);
        } catch (Exception e) {
            successfulUpdate = false;
        }

    }

    private boolean update(String url) throws XmlPullParserException, IOException, ParserConfigurationException, SAXException {

        boolean res = false;
        MyDataBaseChannelHelper myDataBaseChannelHelper = new MyDataBaseChannelHelper(context, id);
        SQLiteDatabase sqLiteDatabase = myDataBaseChannelHelper.getWritableDatabase();
        try {
            downloadPage(url);
            sqLiteDatabase.execSQL(MyDataBaseChannelHelper.DROP_DATABASE_NO_ID + id);
            sqLiteDatabase.execSQL(MyDataBaseChannelHelper.CREATE_DATABASE_BEFORE_ID + id + MyDataBaseChannelHelper.CREATE_DATABASE_AFTER_ID);

            for (int i = 0; i < array.size(); i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MyDataBaseChannelHelper.TITLE, array.get(i).title);
                contentValues.put(MyDataBaseChannelHelper.SUMMARIES, array.get(i).summaries);
                contentValues.put(MyDataBaseChannelHelper.LINK, array.get(i).link);
                sqLiteDatabase.insert(MyDataBaseChannelHelper.DATABASE_NAME + id, null, contentValues);
            }
            res = true;
        } catch (Exception e) {
            res = false;
        } finally {
            sqLiteDatabase.close();
            myDataBaseChannelHelper.close();
        }
        return res;
    }

}
