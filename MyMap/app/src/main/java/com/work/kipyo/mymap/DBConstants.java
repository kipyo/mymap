package com.work.kipyo.mymap;

import android.net.Uri;

/**
 * Created by q on 2016-09-25.
 */

public class DBConstants {
    public final static String DB_NAME = "MAPDB";

    public final static String ID = "_id";
    public final static String DATE = "date";
    public final static String MILEAGE = "mileage";
    public final static String METER = "meter";

    public static final String MAP_URI = "com.work.kipyo.mymap.MapProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + MAP_URI);
}
