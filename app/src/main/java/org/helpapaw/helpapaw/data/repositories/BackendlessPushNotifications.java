package org.helpapaw.helpapaw.data.repositories;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.push.DeviceRegistrationResult;

public class BackendlessPushNotifications implements PushNotifications {

    @Override
    public void registerDeviceForToken() {
        Backendless.Messaging.registerDevice(new AsyncCallback<DeviceRegistrationResult>(){
            @Override
            public void handleResponse(DeviceRegistrationResult response) {
                //Log.d("INFO", "Device registered with toke: " + response.getDeviceToken());
            }
            @Override
            public void handleFault(BackendlessFault fault) {
                //Log.d("ERROR", "Device registration fault: " + fault.getMessage());
            }
        });
    }
}
