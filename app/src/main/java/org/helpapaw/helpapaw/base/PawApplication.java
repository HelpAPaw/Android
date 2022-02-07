package org.helpapaw.helpapaw.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.backendless.Backendless;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.services.BackgroundCheckWorker;

import java.util.concurrent.TimeUnit;

import io.branch.referral.Branch;

/**
 * Created by iliyan on 7/25/16
 */
public class PawApplication extends MultiDexApplication {

    private static final String IS_TEST_ENVIRONMENT_KEY     = "IS_TEST_ENVIRONMENT_KEY";

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
        Injection.getPushNotificationsRepositoryInstance().registerDeviceToken();

        doUserSetupIfNeeded();
        FirebaseAnalytics.getInstance(this);

        scheduleBackgroundChecks();

        // Prevent android.os.FileUriExposedException on API 24+
        // https://stackoverflow.com/a/45569709/2781218
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void doUserSetupIfNeeded() {
        // This is done in order to handle the situation where user token is saved on the device but is invalidated on the server
        final UserManager userManager = Injection.getUserManagerInstance();
        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess(String userId) {
                FirebaseCrashlytics.getInstance().setUserId(userId);

                // Check if user has accepted privacy policy. If not - log out to force acceptance
                userManager.getHasAcceptedPrivacyPolicy(new UserManager.GetUserPropertyCallback() {
                    @Override
                    public void onSuccess(Object hasAcceptedPrivacyPolicy) {
                        try {
                            Boolean accepted = (Boolean) hasAcceptedPrivacyPolicy;
                            if (!accepted) {
                                userManager.logout(new UserManager.LogoutCallback() {
                                    @Override
                                    public void onLogoutSuccess() {
                                        // Do nothing
                                    }

                                    @Override
                                    public void onLogoutFailure(String message) {
                                        // Do nothing
                                    }
                                });
                            }
                        }
                        catch (Exception ignored) {}
                    }

                    @Override
                    public void onFailure(String message) {
                        // Do nothing
                    }
                });
            }

            @Override
            public void onLoginFailure(String message) {
                userManager.logout(new UserManager.LogoutCallback() {
                    @Override
                    public void onLogoutSuccess() {}

                    @Override
                    public void onLogoutFailure(String message) {}
                });
            }
        });
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
