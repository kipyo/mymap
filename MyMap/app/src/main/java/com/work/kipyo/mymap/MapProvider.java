package com.work.kipyo.mymap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by q on 2016-09-25.
 */

public class MapProvider extends ContentProvider {

    SQLiteDatabase db;
    UriMatcher uriMatcher;

    static final int SPECIPIC_MESSAGE = 0;

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (uriMatcher.match(uri) == SPECIPIC_MESSAGE) {
            if (selection == null) {
                selection = "_id=" + uri.getLastPathSegment();
            } else {
                selection += "_id=" + uri.getLastPathSegment();
            }
        }
        return db.delete(DBConstants.DB_NAME, selection,
                selectionArgs);
    }

    @Override
    public String getType(Uri uri) {
        if (uriMatcher.match(uri) == SPECIPIC_MESSAGE) {
            return "vnd.android.cursor.item/vnd.kipyo.constant";
        }
        return "vnd.android.cursor.dir/vnd.kipyo.constant";
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (uriMatcher.match(uri) == SPECIPIC_MESSAGE) {
            return null;
        }

        long rowId = db.insert(DBConstants.DB_NAME, null, values);

        if (rowId > 0) {
            Uri resultUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(resultUri, null);
            return resultUri;
        }

        return null;
    }

    @Override
    public boolean onCreate() {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DBConstants.MAP_URI, "#",
                SPECIPIC_MESSAGE);
        db = new DatabaseHelper(getContext()).getWritableDatabase();
        if (db == null) {
            return false;
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DBConstants.DB_NAME);

        if (uriMatcher.match(uri) == SPECIPIC_MESSAGE) {
            qb.appendWhere("_id=" + uri.getLastPathSegment());
        }

        return qb.query(db, projection, selection, selectionArgs, null, null,
                sortOrder);
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        if (uriMatcher.match(uri) == SPECIPIC_MESSAGE) {
            if (selection == null) {
                selection = "_id=" + uri.getLastPathSegment();
            } else {
                selection += "_id=" + uri.getLastPathSegment();
            }
        }

        return db.update(DBConstants.DB_NAME, values, selection,
                selectionArgs);
    }

}
