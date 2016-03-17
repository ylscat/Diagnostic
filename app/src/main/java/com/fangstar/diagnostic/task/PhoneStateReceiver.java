package com.fangstar.diagnostic.task;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.TelephonyManager;
import android.util.Log;

/**
 * Created at 2016/3/17.
 *
 * @author YinLanShan
 */
public class PhoneStateReceiver extends BroadcastReceiver {
    private static final int MIN_BROADCAST_INTERVAL = 2000;

    @Override
    public void onReceive(Context context, Intent intent) {
        long time = System.currentTimeMillis();
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if(TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)){
            Log.d("PhoneState", state);
        }
        else if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            Log.d("PhoneState", state);
        }
    }

    public static void setEnable(Context context, boolean enable) {
        PackageManager pm = context.getPackageManager();
        ComponentName cn = new ComponentName(context, PhoneStateReceiver.class);
        int en = enable ?
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED :
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        int pre = pm.getComponentEnabledSetting(cn);
        if(en != pre) {
            pm.setComponentEnabledSetting(cn, en, PackageManager.DONT_KILL_APP);
        }
    }
}
