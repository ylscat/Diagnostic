package com.fangstar.diagnostic;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import static android.provider.CallLog.Calls.CONTENT_URI;
import static android.provider.CallLog.Calls.DATE;
import static android.provider.CallLog.Calls.DEFAULT_SORT_ORDER;
import static android.provider.CallLog.Calls.DURATION;
import static android.provider.CallLog.Calls.NUMBER;
import static android.provider.CallLog.Calls._ID;

/**
 * Created at 2016/3/15.
 *
 * @author YinLanShan
 */
public class ListFragment extends Fragment implements View.OnClickListener {
    private Adapter mAdapter;
    private View mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLoaderManager().initLoader(1, null, mCallbacks);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAdapter == null) {
            /*Activity activity = getActivity();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) !=
                    PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }*/
            mAdapter = new Adapter(getActivity().getLayoutInflater());
            if(getView() != null) {
                ListView lv = (ListView)getView().findViewById(R.id.list);
                lv.setAdapter(mAdapter);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.logs, container, false);
        view.findViewById(R.id.button).setOnClickListener(this);
        mProgress = view.findViewById(R.id.progress);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mAdapter != null)
            mAdapter.swapCursor(null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                getLoaderManager().restartLoader(1, null, mCallbacks);
                break;
        }
    }

    private LoaderManager.LoaderCallbacks<Cursor> mCallbacks = new LoaderManager.LoaderCallbacks<Cursor>() {
        @Override
        public Loader<Cursor> onCreateLoader(int id, Bundle args) {
            String[] cols = new String[]{NUMBER, DATE, DURATION, _ID};
            CursorLoader loader = new CursorLoader(getActivity(), CONTENT_URI,
                    cols, null, null, DEFAULT_SORT_ORDER + " limit 20");
            return loader;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mAdapter.swapCursor(data);
            mProgress.setVisibility(View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mAdapter.swapCursor(null);
            mProgress.setVisibility(View.VISIBLE);
        }
    };

    class Adapter extends BaseAdapter {
        private Cursor mCursor;
        private LayoutInflater mInflater;

        public Adapter(LayoutInflater inflater) {
            mInflater = inflater;
        }

        @Override
        public int getCount() {
            if(mCursor == null)
                return 0;
            return mCursor.getCount();
        }

        @Override
        public Object getItem(int position) {
            return mCursor.moveToPosition(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = mInflater.inflate(android.R.layout.simple_list_item_2,
                        parent, false);
            }

            Cursor cursor = mCursor;
            cursor.moveToPosition(position);
            TextView text = (TextView)convertView.findViewById(android.R.id.text1);
            text.setText(cursor.getString(0));
            text = (TextView)convertView.findViewById(android.R.id.text2);
            text.setText(cursor.getString(1));

            return convertView;
        }

        public void swapCursor(Cursor cursor) {
            if(mCursor != null) {
                mCursor.close();
            }
            mCursor = cursor;
            notifyDataSetChanged();
        }
    }
}
