package org.helpapaw.helpapaw.utils.services;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.db.SignalsDatabase;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.NotificationUtils;

import java.util.HashSet;
import java.util.List;

import static org.helpapaw.helpapaw.data.models.Signal.SOLVED;
import static org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter.DEFAULT_SEARCH_RADIUS;
import static org.helpapaw.helpapaw.signalsmap.SignalsMapPresenter.DEFAULT_SEARCH_TIMEOUT;

/**
 * Created by milen on 20/08/17.
 * This class to periodically check for signals around the user and notify them if there are
 */

public class BackgroundCheckJobService extends JobService {
    private SignalsDatabase database;
    public static final String TAG = BackgroundCheckJobService.class.getSimpleName();
    static final String CURRENT_NOTIFICATION_IDS = "CurrentNotificationIds";

    HashSet<String> mCurrentNotificationIds = new HashSet<>();
    NotificationManager mNotificationManager;
//    SharedPreferences mSharedPreferences;

    @Override
    public boolean onStartJob(final JobParameters job) {
        database = SignalsDatabase.getDatabase(this);

        Log.d(TAG, "onStartJob called");

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        mSharedPreferences = getApplicationContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);

        // Do some work here
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            //Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                getSignalsForLastKnownLocation(location, job);
                            } else {
                                Log.d(TAG, "got callback but last location is null");
                                jobFinished(job, true);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "failed to get location");
                            jobFinished(job, true);
                        }
                    });
        } else {
            Log.d(TAG, "No location permission");
        }

        return true; // Answers the question: "Is there still work going on?"
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        database = null;
        return true; // Answers the question: "Should this job be retried?"
    }

    private void getSignalsForLastKnownLocation(Location location, final JobParameters job) {

        Injection.getSignalRepositoryInstance().getAllSignals(location.getLatitude(), location.getLongitude(), DEFAULT_SEARCH_RADIUS * 1000, DEFAULT_SEARCH_TIMEOUT, new SignalRepository.LoadSignalsCallback() {
            @Override
            public void onSignalsLoaded(List<Signal> signals) {

                Log.d(TAG, "got signals");

                if (signals != null && !signals.isEmpty() && database != null) {

                    for (Signal signal : signals) {
                        if (signal.getStatus() < SOLVED) {
                            List<Signal> signalsFromDB = database.signalDao().getSignal(signal.getId());
                            if (signalsFromDB.size() > 0) {
                                Signal signalFromDb = signalsFromDB.get(0);
                                if (!signalFromDb.getSeen()) {
                                    NotificationUtils.showNotificationForSignal(signal, getApplicationContext());
                                    mCurrentNotificationIds.add(signal.getId());
                                    signalFromDb.setSeen(true);
                                    database.signalDao().saveSignal(signalFromDb);
                                }
                            }
                        }
                    }
                }

                // Cancel all previous notifications that are not currently present
//                Set<String> oldNotificationIds = mSharedPreferences.getStringSet(CURRENT_NOTIFICATION_IDS, null);
//                if (oldNotificationIds != null) {
//                    for (String id : oldNotificationIds) {
//                        if (!mCurrentNotificationIds.contains(id)) {
//                            mNotificationManager.cancel(id.hashCode());
//                        }
//                    }
//                }

                // Save ids of current notifications
//                SharedPreferences.Editor editor = mSharedPreferences.edit();
//                editor.putStringSet(CURRENT_NOTIFICATION_IDS, mCurrentNotificationIds);
//                editor.apply();

                jobFinished(job, false);
            }

            @Override
            public void onSignalsFailure(String message) {
                Log.d(TAG, "there was a problem obtaining signals: " + message);
                jobFinished(job, true);
            }
        });

    }
}
