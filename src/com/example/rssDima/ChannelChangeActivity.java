package com.example.rssDima;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ChannelChangeActivity extends Activity {

    EditText name;
    EditText url;
    int id;

    public void changing() {
        MyDataBaseHelper myDataBaseHelper = new MyDataBaseHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = myDataBaseHelper.getWritableDatabase();

        boolean ifNew = getIntent().getBooleanExtra("if_new", true);

        String newName = getIntent().getStringExtra("new_name");
        String newAddress = getIntent().getStringExtra("new_address");
        int currentId = getIntent().getIntExtra("current_id", 0);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MyDataBaseHelper.CHANNEL_NAME, newName);
        contentValues.put(MyDataBaseHelper.CHANNEL_ADDRESS, newAddress);
        contentValues.put(MyDataBaseHelper._ID, currentId);

        if (ifNew)
            sqLiteDatabase.insert(MyDataBaseHelper.DATABASE_NAME, null, contentValues);
        else
            sqLiteDatabase.update(MyDataBaseHelper.DATABASE_NAME, contentValues, MyDataBaseHelper._ID + "=" + id, null);

        sqLiteDatabase.close();
        myDataBaseHelper.close();
    }

    public void deleting() {

        MyDataBaseHelper myDataBaseHelper = new MyDataBaseHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = myDataBaseHelper.getWritableDatabase();
        sqLiteDatabase.execSQL(MyDataBaseChannelHelper.DROP_DATABASE_NO_ID + id);
        sqLiteDatabase.delete(MyDataBaseHelper.DATABASE_NAME, MyDataBaseHelper._ID + "=" + id, null);
        sqLiteDatabase.close();
        myDataBaseHelper.close();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.changing);
        name = (EditText) findViewById(R.id.editNameChange);
        url = (EditText) findViewById(R.id.editURLChange);
        id = getIntent().getIntExtra("current_id", 0);

        name.setText(getIntent().getStringExtra("old_name"));
        url.setText(getIntent().getStringExtra("old_address"));

        final Button buttonSave = (Button) findViewById(R.id.buttonSaveChannel);

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("".equals(name.getText().toString()) || "".equals(url.getText().toString()))
                    Toast.makeText(getApplicationContext(), getString(R.string.ClearSpace), Toast.LENGTH_SHORT).show();
                else {
                    getIntent().putExtra("new_name", name.getText().toString());
                    getIntent().putExtra("new_address", url.getText().toString());
                    changing();
                    finish();
                }
            }
        });

        final Button buttonDelete = (Button) findViewById(R.id.buttonDeleteChannel);

        boolean ifNew = getIntent().getBooleanExtra("if_new", false);
        if (ifNew)
            buttonDelete.setVisibility(View.INVISIBLE);
        else
            buttonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleting();
                    finish();
                }
            });


    }

}
