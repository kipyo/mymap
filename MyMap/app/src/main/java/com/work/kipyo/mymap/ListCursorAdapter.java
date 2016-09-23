package com.work.kipyo.mymap;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by kipyo on 2016-09-24.
 */

public class ListCursorAdapter extends CursorAdapter {
    private static final int DB_ID = 0;
    protected static final int DB_DATE = 1;
    private static final int DB_MILEAGE = 2;
    private static final int DB_KM = 3;
    public static final String[] COLUMNS = {
            "id",
            "date",
            "mileage",
            "km"
    };

    public ListCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, true);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.map_list_view, null);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor != null) {
            ((TextView) view.findViewById(R.id.dateTextView)).setText(cursor.getString(DB_DATE));
            ((TextView) view.findViewById(R.id.mileageTextView)).setText(cursor.getString(DB_MILEAGE));
            ((TextView) view.findViewById(R.id.kmTextView)).setText(cursor.getString(DB_KM));
        }
    }
}
