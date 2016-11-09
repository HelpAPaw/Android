package org.helpapaw.helpapaw.utils.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Emil Ivanov on 9/24/2016.
 */

public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WakeupAlarm alarm = new WakeupAlarm();

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Toast.makeText(context, "Device is awake", Toast.LENGTH_LONG).show();
//            alarm.setAlarm(context);
        }

    }
}
