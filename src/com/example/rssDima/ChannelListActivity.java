package com.example.rssDima;

import android.app.Activity;
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

import java.util.ArrayList;


public class ChannelListActivity extends Activity {

    class MyAdapter extends ArrayAdapter<Record> {
        private Context context;

        public MyAdapter(Context context, int textViewResourceId, ArrayList<Record> items) {
            super(context, textViewResourceId, items);
            this.context = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            Record item = getItem(position);
            TextView itemView = new TextView(context);
            itemView.setTextSize(20);
            itemView.setTextColor(Color.GREEN);
            if (item != null) {
                itemView.setText(item.title);
            } else itemView.setText(R.string.ErrorChannel);
            return itemView;
        }
    }

    public AdapterView.OnItemClickListener goToRecord = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView parent, View v, int position, long id) {

            Intent intent = new Intent();
            intent.putExtra("title", array.get(position).title);
            intent.putExtra("summaries", array.get(position).summaries);
            intent.putExtra("link", array.get(position).link);
            intent.setClass(getApplicationContext(), ChannelActivity.class);

            startActivity(intent);
        }
    };

    public void updateChannel(View view) {
        Intent intent = new Intent(getApplicationContext(), UpdatingService.class);
        intent.putExtra("all_id", false);
        intent.putExtra("current_id", channelId);
        intent.putExtra("channel_name", getIntent().getStringExtra("channel_name"));

        startService(intent);
    }


    ArrayList<Record> array;
    ListView listView;
    MyAdapter adapter;
    int channelId;
    String channel;

    public void makingList() {

        TextView channelName = (TextView) findViewById(R.id.channelName);
        channelName.setText(getIntent().getStringExtra("channel_name"));
        channelName.setTextSize(20);
        channelName.setTextColor(Color.GREEN);

        channelId = getIntent().getIntExtra("channel_id", 0);
        channel = getIntent().getStringExtra("channel_name");

        MyDataBaseChannelHelper myDataBaseChannelHelper = new MyDataBaseChannelHelper(getApplicationContext(), channelId);
        SQLiteDatabase sqLiteDatabase = myDataBaseChannelHelper.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.query(MyDataBaseChannelHelper.DATABASE_NAME + channelId, null, null,
                null, null, null, null);
        int title_column = cursor.getColumnIndex(MyDataBaseChannelHelper.TITLE);
        int summaries_column = cursor.getColumnIndex(MyDataBaseChannelHelper.SUMMARIES);
        int link_column = cursor.getColumnIndex(MyDataBaseChannelHelper.LINK);

        array = new ArrayList<Record>();

        while (cursor.moveToNext())
            array.add(new Record(cursor.getString(title_column), cursor.getString(summaries_column),
                    cursor.getString(link_column)));

        cursor.close();
        sqLiteDatabase.close();
        myDataBaseChannelHelper.close();

        adapter = new MyAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, array);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(goToRecord);
        adapter.notifyDataSetChanged();

    }

    @Override
    public void onStart() {
        super.onStart();
        makingList();
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UpdatingService.UPDATING_ACTION + "_" + channel);
        registerReceiver(mMessageReceiver, intentFilter);
        makingList();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            makingList();
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
        setContentView(R.layout.feedlist);
        makingList();
    }
}
