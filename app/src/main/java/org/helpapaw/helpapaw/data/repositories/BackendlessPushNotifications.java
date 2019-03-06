package org.helpapaw.helpapaw.data.repositories;

import android.util.Log;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.push.DeviceRegistrationResult;

import org.helpapaw.helpapaw.utils.Injection;

public class BackendlessPushNotifications implements PushNotifications{
    public static final String TAG = BackendlessPushNotifications.class.getSimpleName();

    @Override
    public void registerDeviceForToken() {
        Backendless.Messaging.registerDevice(new AsyncCallback<DeviceRegistrationResult>(){
            @Override
            public void handleResponse(DeviceRegistrationResult response) {
                //Save device-token in preferences
                Injection.getSettingsRepository().saveTokenToPreferences(response.getDeviceToken());

            }
            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Device registration fault: " + fault.getMessage());
            }
        });
    }
}
