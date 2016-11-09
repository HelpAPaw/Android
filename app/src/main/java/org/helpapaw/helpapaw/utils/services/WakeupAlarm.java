package org.helpapaw.helpapaw.utils.services;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity;

import static android.app.AlarmManager.RTC_WAKEUP;
import static org.helpapaw.helpapaw.utils.services.JobSchedulerService.JOB_ID;
import static org.helpapaw.helpapaw.utils.services.JobSchedulerService.TIME_INTERVAL;

/**
 * Created by Emil Ivanov on 9/24/2016.
 */

public class WakeupAlarm extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = null;
        wl =  pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK|PowerManager.ACQUIRE_CAUSES_WAKEUP , "");
        wl.acquire();

        // Put here YOUR code.
//        Toast.makeText(context, "Alarm !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
        createNotification(context, SignalsMapActivity.class);

        wl.release();
    }

    public void setAlarm(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, WakeupAlarm.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);

        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                AlarmManager.INTERVAL_HALF_HOUR,
                AlarmManager.INTERVAL_HALF_HOUR, pi);
//        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 *2, pi); // Millisec * Second * Minute
    }

    public void cancelAlarm(Context context) {
        Intent intent = new Intent(context, WakeupAlarm.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    private void createNotification(Context context, Class<?> tClass) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        mBuilder.setSmallIcon(R.drawable.ic_paw).setContentTitle("Help might be needed. ");
        mBuilder.setContentText("Paws might need help. Take a look.");

        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, tClass);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(tClass);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_CANCEL_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(123, mBuilder.build());
    }

}
