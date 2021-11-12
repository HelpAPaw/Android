package org.helpapaw.helpapaw.utils.services;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.content.ContextCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.common.util.concurrent.ListenableFuture;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.ISettingsRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.db.NotificationsDatabase;
import org.helpapaw.helpapaw.db.SignalsDatabase;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.NotificationUtils;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.HashSet;
import java.util.List;

import static org.helpapaw.helpapaw.data.models.Signal.SOLVED;

/**
 * Created by milen on 20/08/17.
 * This class to periodically check for signals around the user and notify them if there are
 */

public class BackgroundCheckWorker extends ListenableWorker {
    private SignalsDatabase signalsDatabase;
    private NotificationsDatabase notificationDatabase;

    public static final String TAG = BackgroundCheckWorker.class.getSimpleName();
    static final String CURRENT_NOTIFICATION_IDS = "CurrentNotificationIds";

    HashSet<String> mCurrentNotificationIds = new HashSet<>();
    NotificationManager mNotificationManager;

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public BackgroundCheckWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }
//    SharedPreferences mSharedPreferences;

    @NonNull
    @Override
    public ListenableFuture<ListenableWorker.Result> startWork() {
        signalsDatabase = SignalsDatabase.getDatabase(getApplicationContext());
        notificationDatabase = NotificationsDatabase.getDatabase(getApplicationContext());

        Log.d(TAG, "onStartJob called");

        mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
//        mSharedPreferences = getApplicationContext().getSharedPreferences(TAG, Context.MODE_PRIVATE);

        // Do some work here
        return CallbackToFutureAdapter.getFuture(completer -> {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            //Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                getSignalsForLastKnownLocation(location, completer);
                                Injection.getPushNotificationsRepositoryInstance().
                                        updateDeviceInfoInCloud(location, null, null, null);
                            } else {
                                Log.d(TAG, "got callback but last location is null");
                                completer.set(Result.success());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "failed to get location");
                            completer.setException(e);
                        }
                    });
            } else {
                Log.d(TAG, "No location permission");
                completer.set(Result.failure());
            }
            return completer;
        });
    }

    @Override
    public void onStopped() {
        signalsDatabase = null;
    }

    private void getSignalsForLastKnownLocation(Location location, final CallbackToFutureAdapter.Completer<ListenableWorker.Result> completer) {

        ISettingsRepository settingsRepository = Injection.getSettingsRepositoryInstance();
        Injection.getSignalRepositoryInstance().getAllSignals(location.getLatitude(), location.getLongitude(), settingsRepository.getRadius(), settingsRepository.getTimeout(), new SignalRepository.LoadSignalsCallback() {
            @Override
            public void onSignalsLoaded(List<Signal> signals) {

                Log.d(TAG, "got signals");

                if (signals != null && !signals.isEmpty() && signalsDatabase != null) {

                    int signalTypesSetting = settingsRepository.getSignalTypes();

                    for (Signal signal : signals) {
                        // Notify user only if signal is not solved and user has subscribed for that signal type
                        if ((signal.getStatus() < SOLVED) && (Utils.shouldNotifyAboutSignalType(signal.getType(), signalTypesSetting))) {
                            List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignal(signal.getId());
                            if (signalsFromDB.size() > 0) {
                                Signal signalFromDb = signalsFromDB.get(0);
                                if (!signalFromDb.getSeen()) {
                                    NotificationUtils.showNotificationForSignal(signal, getApplicationContext());
                                    mCurrentNotificationIds.add(signal.getId());

                                    String notificationText = getApplicationContext().getString(R.string.txt_new_signal) + ": " + signal.getTitle();
                                    Notification notification = new Notification(signal.getId(), signal.getPhotoUrl(), notificationText);
                                    notificationDatabase.notificationDao().saveNotification(notification);

                                    signalFromDb.setSeen(true);
                                    signalsDatabase.signalDao().saveSignal(signalFromDb);
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

                completer.set(Result.success());
            }

            @Override
            public void onSignalsFailure(String message) {
                Log.d(TAG, "there was a problem obtaining signals: " + message);
                completer.set(Result.failure());
            }
        });
    }
}
