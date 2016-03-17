package com.fangstar.diagnostic;

import android.app.Application;

/**
 * Created at 2016/3/17.
 *
 * @author YinLanShan
 */
public class App extends Application {
    public static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
    }
}
