package org.helpapaw.helpapaw.utils.backgroundscheduler;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.evernote.android.job.Job;
import com.google.android.gms.maps.model.LatLng;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.List;
import java.util.Random;

import static org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter.DEFAULT_SEARCH_RADIUS;

/**
 * Created by Emil Ivanov on 11/20/2016.
 */

public class SignalsSyncJob extends Job {
    SignalRepository signalRepository = Injection.getSignalRepositoryInstance();
    public static final String TAG = "signals_sync_tag";
    public Location oldLocation;
    LocationManager locationManager;
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        locationManager = (LocationManager) PawApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
        LocationProvider locationProvider = locationManager.getProvider(locationManager.getBestProvider(getLocationCriteria(), true));
        //  LocationProvider locationProvider = locationManager.getProvider(locationManager.getBestProvider());
        // Define a listener that responds to location updates

        // Register the listener with the Location Manager to receive location updates

        Log.e(TAG, "onStart is location enabled");
        oldLocation = locationManager.getLastKnownLocation(locationProvider.getName());
//        if (params.isPeriodic()) {
//            PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), SignalsMapActivity.class), 0);
//
//            Notification notification = new NotificationCompat.Builder(getContext())
//                    .setContentTitle("Job Demo")
//                    .setContentText("Periodic job ran")
//                    .setAutoCancel(true)
//                    .setContentIntent(pendingIntent)
//                    .setSmallIcon(R.drawable.ic_paw)
//                    .setShowWhen(true)
//                    .setColor(Color.GREEN)
//                    .setLocalOnly(true)
//                    .build();
//
//            NotificationManagerCompat.from(getContext()).notify(new Random().nextInt(), notification);
//        }

        requestSignals(oldLocation);


        return Result.SUCCESS;
    }

    @Override
    protected void onReschedule(int newJobId) {
        super.onReschedule(newJobId);
    }


    private static Criteria getLocationCriteria() {

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setSpeedRequired(false);
        criteria.setBearingRequired(false);

        return criteria;
    }


    private void requestSignals(Location location){
        if (Utils.getInstance().hasNetworkConnection()) {
            signalRepository.getAllSignals(location.getLatitude(), location.getLongitude(), DEFAULT_SEARCH_RADIUS, new SignalRepository.LoadSignalsCallback() {
                @Override
                public void onSignalsLoaded(List<Signal> signals) {
                    Log.e(TAG, "onSignalsLoaded CHANGED JOB");
                    if (signals != null && !signals.isEmpty()) {
                        for (int i = 0; i < signals.size(); i++) {
                            createNotificationForSignal(PawApplication.getContext(), SignalsMapActivity.class, signals.get(i), i);
                        }
                    } else {
                        createOfflineNotification(PawApplication.getContext(), SignalsMapActivity.class);
                    }
                }

                @Override
                public void onSignalsFailure(String message) {
                    Log.e(TAG, "onSignalsFailure CHANGED JOB");

                }
            });
        } else {
            Log.e(TAG, "No network ");
            createOfflineNotification(PawApplication.getContext(), SignalsMapActivity.class);
        }
    }

    private void createNotificationForSignal(Context context, Class<?> tClass, Signal signal, int notificationId){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        if (Utils.getInstance().hasNetworkConnection() && signal !=null) {
            mBuilder.setSmallIcon(R.drawable.ic_paw_notif).setContentTitle("Help needed. (SignalsSyncJob)");
            mBuilder.setContentText(signal.getTitle());
        }
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, tClass);
        resultIntent.putExtra(Signal.KEY_SIGNAL, signal);
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
                stackBuilder.getPendingIntent(notificationId,
                        PendingIntent.FLAG_ONE_SHOT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(notificationId, mBuilder.build());


    }


    private void createOfflineNotification(Context context, Class<?> tClass) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        if (!Utils.getInstance().hasNetworkConnection()){
            mBuilder.setSmallIcon(R.drawable.ic_paw_small).setContentTitle("Help might be needed.");
            mBuilder.setContentText("Paws might need help. Take a look.");
        }
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
        mBuilder.setAutoCancel(true);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(123, mBuilder.build());
    }
}
