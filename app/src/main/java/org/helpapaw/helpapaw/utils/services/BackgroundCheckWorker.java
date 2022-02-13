package org.helpapaw.helpapaw.utils.services;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.core.content.ContextCompat;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import android.util.Log;

import com.google.common.util.concurrent.ListenableFuture;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.ISettingsRepository;
import org.helpapaw.helpapaw.data.repositories.ReceivedNotificationsRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.db.SignalsDatabase;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.NotificationUtils;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.List;

import static org.helpapaw.helpapaw.data.models.Signal.SOLVED;

/**
 * Created by milen on 20/08/17.
 * This class to periodically check for signals around the user and notify them if there are
 */

public class BackgroundCheckWorker extends ListenableWorker {
    private SignalsDatabase signalsDatabase;
    private ReceivedNotificationsRepository notificationDatabase;

    public static final String TAG = BackgroundCheckWorker.class.getSimpleName();

    /**
     * @param appContext   The application {@link Context}
     * @param workerParams Parameters to setup the internal state of this worker
     */
    public BackgroundCheckWorker(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<ListenableWorker.Result> startWork() {
        Log.d(TAG, "onStartJob called");

        signalsDatabase = SignalsDatabase.getDatabase(getApplicationContext());
        notificationDatabase = Injection.getReceivedNotificationsRepositoryInstance();

        // Do some work here
        return CallbackToFutureAdapter.getFuture(completer -> {
            if (   (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)   ) {

                LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
                List<String> providers = locationManager.getProviders(true);
                Location location = null;
                for (String provider : providers) {
                    location = locationManager.getLastKnownLocation(provider);
                    if (location != null) {
                        break;
                    }
                }

                //Got last known location. In some rare situations this can be null.
                if (location != null) {
                    getSignalsForLastKnownLocation(location, completer);
                    Injection.getPushNotificationsRepositoryInstance().updateDeviceInfoInCloud(location, null, null, null);
                } else {
                    Log.d(TAG, "Could not obtain last location");
                    completer.set(Result.failure());
                }
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
        notificationDatabase = null;
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

                                    saveNotification(signal);

                                    signalFromDb.setSeen(true);
                                    signalsDatabase.signalDao().saveSignal(signalFromDb);
                                }
                            }
                        }
                    }
                }

                completer.set(Result.success());
            }

            @Override
            public void onSignalsFailure(String message) {
                Log.d(TAG, "there was a problem obtaining signals: " + message);
                completer.set(Result.failure());
            }
        });
    }

    private void saveNotification(Signal signal) {
        if (notificationDatabase != null) {
            String notificationText = getApplicationContext().getString(R.string.txt_new_signal) + ": " + signal.getTitle();
            Notification notification = new Notification(signal.getId(), signal.getPhotoUrl(), notificationText);
            notificationDatabase.saveNotification(notification);
        }
    }
}
