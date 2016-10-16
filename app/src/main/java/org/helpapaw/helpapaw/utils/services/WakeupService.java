package org.helpapaw.helpapaw.utils.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Emil Ivanov on 9/24/2016.
 */

public class WakeupService extends Service {

    WakeupAlarm alarm = new WakeupAlarm();

    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        alarm.setAlarm(this);
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }
}
