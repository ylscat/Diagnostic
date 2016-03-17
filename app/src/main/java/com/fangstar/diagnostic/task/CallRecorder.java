package com.fangstar.diagnostic.task;

import android.content.Context;

import com.fangstar.diagnostic.App;

/**
 * Created at 2016/3/17.
 *
 * @author YinLanShan
 */
public class CallRecorder {
    private static final String TAG = CallRecorder.class.getSimpleName();

    public CallRecorder(Context context) {

    }

    public void onResume() {

    }

    public void onPause() {

    }

    public void call(String number) {

    }

    private static final CallRecorder INSTANCE = new CallRecorder(App.sApp);

    public static CallRecorder getInstance() {
        return INSTANCE;
    }
}
