package org.helpapaw.helpapaw.utils.services;

import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.signalsmap.SignalsMapActivity;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.LocationUtils;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.List;

import static org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter.DEFAULT_SEARCH_RADIUS;


/**
 * Created by Emil Ivanov on 10/8/2016.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class JobSchedulerService extends JobService {
    //TODO 1. Request users location.
    //TODO 2. On Location changed make request for data.
    //TODO 3. Send notifications

    private static final String TAG = JobSchedulerService.class.getSimpleName();
    private UpdateAppsAsyncTask updateTask = new UpdateAppsAsyncTask();
    LocationManager locationManager;
    public static int JOB_ID = 12;
    public static long TIME_INTERVAL =5* 60 * 1000;

    public static long LOCATION_UPDATE_TIME = 0;
    public static float LOCATION_MIN_DISTANCE = 0;
    public JobParameters jobParameters;
    public Location oldLocation;
    public Location current;
    int counter = 0;
    SignalRepository signalRepository = Injection.getSignalRepositoryInstance();
    private Handler mJobHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            Toast.makeText(getApplicationContext(),
                    "JobService task running", Toast.LENGTH_SHORT)
                    .show();
            return false;
        }
    });
    private boolean firstTime = true;


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "Service destroyed");
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        jobParameters = params;
        Log.e(TAG, "onStart JOB");
        // Note: this is preformed on the main thread.
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        LocationProvider locationProvider = locationManager.getProvider(locationManager.getBestProvider(getLocationCriteria(), true));
        //  LocationProvider locationProvider = locationManager.getProvider(locationManager.getBestProvider());
        // Define a listener that responds to location updates

        // Register the listener with the Location Manager to receive location updates

        Log.e(TAG, "onStart is location enabled");
        oldLocation = locationManager.getLastKnownLocation(locationProvider.getName());

        if (isLocationEnabled()) {
            Log.d(TAG, "Location is enabled." + locationProvider.getName());
            locationManager.requestLocationUpdates(locationProvider.getName(), LOCATION_UPDATE_TIME, LOCATION_MIN_DISTANCE, locationListener);
        }else{
            Log.d(TAG, "Location not enabled.");
        }
// }else {
//
//                Log.e(TAG, "onStart permission not available");
//                jobFinished(jobParameters, true);
//            }


        return true;
    }

    private boolean isLocationEnabled() {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) || locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
    }

    @Override
    public boolean onStopJob(JobParameters params) { // Note: return true to reschedule this job.
        Log.e(TAG, "onStop JOB");
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(locationListener);
//        }
//
        boolean shouldReschedule = true;// Return true if you want the job to be rescheduled.
        return shouldReschedule;
    }

    LocationListener locationListener = new LocationListener() {

        public void onLocationChanged(Location location) {
            Log.e(TAG, "onLocationChanged ");
            counter = (int) LocationUtils.getDistance(oldLocation, location);
            if( LocationUtils.shouldRequestUpdate(oldLocation, location)) {
                Log.e(TAG, "Location update triggered ");
                if (Utils.getInstance().hasNetworkConnection()) {
                    signalRepository.getAllSignals(location.getLatitude(), location.getLongitude(), DEFAULT_SEARCH_RADIUS, new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            Log.e(TAG, "onSignalsLoaded CHANGED JOB");
                            if (signals != null && !signals.isEmpty()) {
                                for (int i = 0; i < signals.size(); i++) {
                                    createNotificationForSignal(getApplicationContext(), SignalsMapActivity.class, signals.get(i), i);
                                }
                            } else {
//                                createOfflineNotification(getApplicationContext(), SignalsMapActivity.class);
                            }
                        }

                        @Override
                        public void onSignalsFailure(String message) {
                            Log.e(TAG, "onSignalsFailure CHANGED JOB");
                            jobFinished(jobParameters, true);
                        }
                    });
                } else {
                    Log.e(TAG, "No network ");
//                    createOfflineNotification(getApplicationContext(), SignalsMapActivity.class);
                //                jobFinished(jobParameters, true);
                }
            }else{
                Log.d(TAG, "Distance below");
//                createOfflineNotification(getApplicationContext(), SignalsMapActivity.class);
                jobFinished(jobParameters, true);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, provider + " status " + status);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, provider + " status: enabled");
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, provider + " status: disabled");
        }
    };

    private class UpdateAppsAsyncTask extends AsyncTask<JobParameters, Void, JobParameters[]> {

        @Override
        protected JobParameters[] doInBackground(JobParameters... params) {

            // Do updating and stopping logical here.
            return params;
        }

        @Override
        protected void onPostExecute(JobParameters[] result) {
            for (JobParameters params : result) {
                if (!hasJobBeenStopped(params)) {
                    jobFinished(params, false);
                }
            }
        }

        private boolean hasJobBeenStopped(JobParameters params) {
            // Logic for checking stop.
            return false;
        }

        public boolean stopJob(JobParameters params) {
            // Logic for stopping a job. return true if job should be rescheduled.
            return true;
        }
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

    private void createNotificationForSignal(Context context, Class<?> tClass, Signal signal, int notificationId){
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context);
        if (Utils.getInstance().hasNetworkConnection() && signal !=null) {
            mBuilder.setSmallIcon(R.drawable.ic_paw).setContentTitle("Help needed.");
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
            mBuilder.setSmallIcon(R.drawable.ic_paw).setContentTitle("Help might be needed.");
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
