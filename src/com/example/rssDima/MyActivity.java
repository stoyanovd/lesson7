package com.example.rssDima;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParserException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;


public class MyActivity extends Activity {

    MyAdapter adapter;
    ArrayList<Channel> array;
    ListView listView;
    int maxChannel = 0;


    class MyAdapter extends ArrayAdapter<Channel> {
        private Context context;

        public MyAdapter(Context context, int textViewResourceId, ArrayList<Channel> items) {
            super(context, textViewResourceId, items);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            Channel item = getItem(position);
            TextView itemView = new TextView(context);
            itemView.setTextSize(30);
            itemView.setTextColor(Color.GREEN);
            if (item != null) {
                itemView.setText(item.name);
            } else itemView.setText(R.string.ErrorChannel);
            return itemView;
        }
    }

    public AdapterView.OnItemClickListener goToChannel = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {
            Intent intent = new Intent();
            intent.putExtra("channel_name", array.get(position).name);
            intent.putExtra("channel_address", array.get(position).address);
            intent.putExtra("channel_id", array.get(position).id);
            intent.setClass(getApplicationContext(), ChannelListActivity.class);

            startActivity(intent);
        }
    };

    public AdapterView.OnItemLongClickListener changeChannel = new AdapterView.OnItemLongClickListener() {
        public boolean onItemLongClick(AdapterView parent, View v, int position, long id) {

            Intent intent = new Intent();
            intent.putExtra("old_name", array.get(position).name);
            intent.putExtra("old_address", array.get(position).address);
            intent.putExtra("current_id", array.get(position).id);
            intent.putExtra("if_new", false);
            intent.setClass(getApplicationContext(), ChannelChangeActivity.class);

            startActivity(intent);

            return true;
        }
    };

    public void addChannel(View view) {
        Intent intent = new Intent();
        intent.putExtra("old_name", "");
        intent.putExtra("old_address", "");
        maxChannel++;
        intent.putExtra("current_id", maxChannel);
        intent.putExtra("if_new", true);
        intent.setClass(getApplicationContext(), ChannelChangeActivity.class);

        startActivity(intent);
    }

    public void updateFeed(View view) throws ParserConfigurationException, XmlPullParserException, SAXException, IOException {
        Intent intent = new Intent(getApplicationContext(), UpdatingService.class);
        intent.putExtra("all_id", true);
        startService(intent);
    }

    public void fillingArraysAndAdapter() {
        MyDataBaseHelper myDataBaseHelper = new MyDataBaseHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = myDataBaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(MyDataBaseHelper.DATABASE_NAME, null, null, null, null, null, null);
        int id_column = cursor.getColumnIndex(MyDataBaseHelper._ID);
        int name_column = cursor.getColumnIndex(MyDataBaseHelper.CHANNEL_NAME);
        int address_column = cursor.getColumnIndex(MyDataBaseHelper.CHANNEL_ADDRESS);

        array = new ArrayList<Channel>();

        while (cursor.moveToNext()) {
            if (cursor.getString(name_column) == null || "".equals(cursor.getString(name_column)) ||
                    cursor.getString(address_column) == null || "".equals(cursor.getString(address_column)))
                continue;
            array.add(new Channel(cursor.getInt(id_column), cursor.getString(name_column), cursor.getString(address_column)));
            maxChannel = Math.max(maxChannel, cursor.getInt(id_column));
        }
        cursor.close();
        sqLiteDatabase.close();
        myDataBaseHelper.close();

        adapter = new MyAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, array);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(goToChannel);
        listView.setOnItemLongClickListener(changeChannel);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        fillingArraysAndAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceiver(mMessageReceiver, new IntentFilter(UpdatingService.UPDATING_ACTION));
        fillingArraysAndAdapter();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            fillingArraysAndAdapter();
            Toast.makeText(context, intent.getStringExtra("result"), Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onPause() {
        unregisterReceiver(mMessageReceiver);
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Intent intent = new Intent(getApplicationContext(), UpdatingService.class);
        intent.putExtra("all_id", true);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 132423421, intent, 0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, 15000, AlarmManager.INTERVAL_HALF_HOUR, pendingIntent);

    }
}