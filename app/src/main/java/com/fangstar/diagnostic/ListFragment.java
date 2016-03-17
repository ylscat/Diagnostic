package com.fangstar.diagnostic;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

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
public class ListFragment extends Fragment {
    private SimpleCursorAdapter mAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mAdapter == null) {
            Activity activity = getActivity();
            /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALL_LOG) !=
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
            mAdapter = new SimpleCursorAdapter(getActivity(),
                    android.R.layout.simple_list_item_2,
                    null, new String[]{NUMBER, DATE, DURATION},
                    new int[]{android.R.id.text1, android.R.id.text2},
                    SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
            if(getView() != null) {
                ListView lv = (ListView)getView();
                lv.setAdapter(mAdapter);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ListView listView = new ListView(inflater.getContext());
        listView.setAdapter(mAdapter);
        return listView;
    }

    @Override
    public void onResume() {
        super.onResume();

        Cursor c = getActivity().getContentResolver().query(CONTENT_URI,
                new String[]{NUMBER, DATE, DURATION, _ID}, null, null, DEFAULT_SORT_ORDER);
        mAdapter.swapCursor(c);
        mAdapter.notifyDataSetChanged();
    }
}
