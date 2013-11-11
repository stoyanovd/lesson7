package com.example.rssDima;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

public class UpdatingService extends IntentService {


    public UpdatingService(String name) {
        super(name);
    }

    public UpdatingService() {
        super("default_name_");
    }

    public static final String UPDATING_ACTION = "updating";

    ArrayList<Channel> array;
    int maxChannel = 0;

    public void fillingArrayAtStart() {
        MyDataBaseHelper myDataBaseHelper = new MyDataBaseHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = myDataBaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query(MyDataBaseHelper.DATABASE_NAME, null, null, null, null, null, null);
        int id_column = cursor.getColumnIndex(MyDataBaseHelper._ID);
        int name_column = cursor.getColumnIndex(MyDataBaseHelper.CHANNEL_NAME);
        int address_column = cursor.getColumnIndex(MyDataBaseHelper.CHANNEL_ADDRESS);

        array = new ArrayList<Channel>();

        while (cursor.moveToNext()) {
            array.add(new Channel(cursor.getInt(id_column), cursor.getString(name_column), cursor.getString(address_column)));
            maxChannel = Math.max(maxChannel, cursor.getInt(id_column));
        }

        cursor.close();
        sqLiteDatabase.close();
        myDataBaseHelper.close();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        fillingArrayAtStart();

        boolean allId = intent.getBooleanExtra("all_id", true);
        int currentId = intent.getIntExtra("current_id", 0);
        Intent intentResponse = new Intent();
        boolean bad = false;

        for (int i = 0; i < array.size(); i++) {
            if (allId || currentId == array.get(i).id) {
                try {
                    Updater updater = new Updater(getApplicationContext(), array.get(i).id, array.get(i).address);
                    if (!updater.successfulUpdate)
                        bad = true;
                } catch (Exception e) {
                    e.printStackTrace();
                    bad = true;
                }
            }
        }

        if (bad)
            intentResponse.putExtra("result", getString(R.string.ErrorWithInternet) + currentId);
        else
            intentResponse.putExtra("result", getString(R.string.SuccessfulUpdate));
        if (!allId)
            intentResponse.setAction(UPDATING_ACTION + "_" + intent.getStringExtra("channel_name"));
        else
            intentResponse.setAction(UPDATING_ACTION);
        sendBroadcast(intentResponse);
    }

}
