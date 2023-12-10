package org.helpapaw.helpapaw.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.backendless.Backendless;
import com.google.firebase.analytics.FirebaseAnalytics;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.services.BackgroundCheckWorker;

import java.util.concurrent.TimeUnit;

import io.branch.referral.Branch;

/**
 * Created by iliyan on 7/25/16
 */
public class PawApplication extends MultiDexApplication {

    public static final String APP_OPEN_COUNTER = "APP_OPEN_COUNTER_KEY";
    public static final int APP_OPENINGS_TO_ASK_FOR_SHARE = 10;

    private static final String IS_TEST_ENVIRONMENT_KEY = "IS_TEST_ENVIRONMENT_KEY";

    private static Boolean isTestEnvironment;
    private static PawApplication pawApplication;

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        pawApplication = this;
        isTestEnvironment = loadIsTestEnvironment();
        Backendless.initApp(this, getResources().getString(R.string.BACKENDLESS_APP_ID), getResources().getString(R.string.BACKENDLESS_ANDROID_API_KEY));

        // Branch logging for debugging
        Branch.enableLogging();

        // Initialize the Branch object
        Branch.getAutoInstance(this);

        // Register device for token
        Injection.getPushNotificationsRepositoryInstance().registerDeviceTokenIfNeeded();

        doUserSetupIfNeeded();
        FirebaseAnalytics.getInstance(this);

        scheduleBackgroundChecks();

        // Prevent android.os.FileUriExposedException on API 24+
        // https://stackoverflow.com/a/45569709/2781218
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        // This counts how many times the application has been opened.
        // After APP_OPEN_MAX_COUNTER the counter will be set to 0 again
        // and the user will receive a reminder to share the application.
        updateApplicationCounter();
    }

    private void updateApplicationCounter() {
        SharedPreferences prefs = getSharedPreferences("HelpAPaw", MODE_PRIVATE);
        int counter = prefs.getInt(APP_OPEN_COUNTER, 0);

        if (counter <= APP_OPENINGS_TO_ASK_FOR_SHARE) {
            counter++;
            prefs.edit().putInt(APP_OPEN_COUNTER, counter).apply();
        }
    }

    private void doUserSetupIfNeeded() {
        String currentUserId = Injection.getUserManagerInstance().getLoggedUserId();
        if (currentUserId != null && !currentUserId.isEmpty()) {
            Injection.getCrashLogger().setUserId(currentUserId);
        }
    }

    public static PawApplication getContext() {
        return pawApplication;
    }

    public static Boolean getIsTestEnvironment() {
        return isTestEnvironment;
    }

    public static void setIsTestEnvironment(Boolean isTestEnvironment) {
        PawApplication.isTestEnvironment = isTestEnvironment;
        pawApplication.saveIsTestEnvironment(isTestEnvironment);
    }

    private Boolean loadIsTestEnvironment() {
        SharedPreferences prefs = getSharedPreferences("HelpAPaw", MODE_PRIVATE);
        return prefs.getBoolean(IS_TEST_ENVIRONMENT_KEY, false);
    }

    private void saveIsTestEnvironment(Boolean isTestEnvironment) {
        SharedPreferences prefs = pawApplication.getSharedPreferences("HelpAPaw", MODE_PRIVATE);
        prefs.edit().putBoolean(IS_TEST_ENVIRONMENT_KEY, isTestEnvironment).apply();
    }

    private void scheduleBackgroundChecks() {
        // constraints that need to be satisfied for the job to run
        Constraints workerConstraints = new Constraints.Builder()
                //Network connectivity required
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(BackgroundCheckWorker.class, 15, TimeUnit.MINUTES)
                // uniquely identifies the job
                .addTag("BackgroundCheckJobService")
                // start in 15 minutes from now
                .setInitialDelay(15, TimeUnit.MINUTES)
                .setConstraints(workerConstraints)
                .build();

        WorkManager.getInstance(this)
                .enqueueUniquePeriodicWork("BackgroundCheckJobService", ExistingPeriodicWorkPolicy.REPLACE, workRequest);
    }
}
