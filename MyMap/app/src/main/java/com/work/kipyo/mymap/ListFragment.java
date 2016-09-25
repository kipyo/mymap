package com.work.kipyo.mymap;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.work.kipyo.mymap.test.TestCursor;

/**
 * Created by kipyo on 2016-09-23.
 */

public class ListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private ListView mListView;
    private ListCursorAdapter mListAdapter;
    private final static Boolean FOR_TEST = false;
    public ListFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.list_fragment, container, false);
        mListView = (ListView)rootView.findViewById(R.id.item_list);
        if (FOR_TEST) {
            TestCursor testCursor = new TestCursor();
            mListAdapter = new ListCursorAdapter(getActivity(), testCursor.getCursor());
        } else {
            getLoaderManager().initLoader(0, null, this);
            mListAdapter = new ListCursorAdapter(getActivity(), null);
        }
        mListView.setAdapter(mListAdapter);

        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://" + MapProvider.MAP_URI);
        String selection ="";
        return new CursorLoader(getActivity(), uri, ListCursorAdapter.COLUMNS, selection, null, ListCursorAdapter.COLUMNS[ListCursorAdapter.DB_DATE] + " COLLATE LOCALIZED DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null && cursor.getCount() > 0) {
            mListAdapter.swapCursor(cursor);
            getActivity().findViewById(R.id.item_list).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.emptyList).setVisibility(View.GONE);
        } else {
            getActivity().findViewById(R.id.emptyList).setVisibility(View.VISIBLE);
            getActivity().findViewById(R.id.item_list).setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mListAdapter.swapCursor(null);
    }
}