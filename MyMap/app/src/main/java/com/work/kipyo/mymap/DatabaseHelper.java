package com.work.kipyo.mymap;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by q on 2016-09-25.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(Context context) {
        super(context, DBConstants.DB_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + DBConstants.DB_NAME
                + "(" + DBConstants.ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + DBConstants.DATE + " TEXT, "
                + DBConstants.MILEAGE + " INTEGER, " + DBConstants.METER + " INTEGER)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
