package io.alcatraz.audiohq;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import io.alcatraz.audiohq.services.AHQProtector;
import io.alcatraz.audiohq.utils.SharedPreferenceUtil;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferenceUtil spfu=SharedPreferenceUtil.getInstance();
        boolean start_protector =
                (boolean) spfu.get(context,Constants.PREF_PROTECTOR,Constants.DEFAULT_VALUE_PREF_PROTECTOR);
        if(start_protector){
            context.startService(new Intent(context, AHQProtector.class));
        }
    }
}
