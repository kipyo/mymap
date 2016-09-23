package com.work.kipyo.mymap.test;

import android.database.Cursor;
import android.database.MatrixCursor;

/**
 * Created by kipyo on 2016-09-24.
 */

public class TestCursor {
    private MatrixCursor mCursor;
    public TestCursor() {
        mCursor = new MatrixCursor(new String[] {"_id", "date", "mileage", "km"});
        mCursor.addRow(new String[] {"0", "2016.08.12", "1212", "12"});
        mCursor.addRow(new String[] {"1", "2016.08.13", "3333", "03"});
        mCursor.addRow(new String[] {"2", "2016.08.14", "4444", "04"});
        mCursor.addRow(new String[] {"3", "2016.08.15", "5555", "05"});
        mCursor.addRow(new String[] {"4", "2016.08.16", "6666", "06"});
        mCursor.addRow(new String[] {"5", "2016.08.17", "7777", "07"});
        mCursor.addRow(new String[] {"6", "2016.08.18", "8888", "08"});
        mCursor.addRow(new String[] {"7", "2016.08.19", "9999", "09"});
        mCursor.addRow(new String[] {"8", "2016.08.20", "0000", "10"});
    }
    public Cursor getCursor() {
        return mCursor;
    }
}
