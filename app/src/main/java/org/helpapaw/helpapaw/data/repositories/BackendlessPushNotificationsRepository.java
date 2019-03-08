package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;
import android.util.Log;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.messaging.DeliveryOptions;
import com.backendless.messaging.MessageStatus;
import com.backendless.messaging.PublishOptions;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.push.DeviceRegistrationResult;

import org.helpapaw.helpapaw.utils.Injection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BackendlessPushNotificationsRepository implements PushNotificationsRepository {
    public static final String TAG = BackendlessPushNotificationsRepository.class.getSimpleName();

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

    /*
     * Queries Backendless for locally saved device-token,
     * and updates 4 properties on the corresponding db-entry
     */
    @Override
    public void saveNewDeviceLocation(final Location location) {
        // Get local device-token
        String localToken = Injection.getSettingsRepository().getTokenFromPreferences();

        // Make sure localToken exists
        if (localToken != null) {

            // Build query
            String whereClause = "deviceToken = '" + localToken + "'";
            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setWhereClause(whereClause);

            Backendless.Data.of("DeviceRegistration").find(queryBuilder,
                    new AsyncCallback<List<Map>>() {
                        @Override
                        public void handleResponse(List<Map> foundDevice) {
                            // every loaded object from the "DeviceRegistration" table
                            // is now an individual java.util.Map

                            // Extract 'Map' object from the 'List<Map>'
                            Map mapFoundDevice = foundDevice.get(0);
                            try {
                                mapFoundDevice.put("signalRadius", Injection.getSettingsRepository().getRadius());
                                mapFoundDevice.put("lastLatitude", location.getLatitude());
                                mapFoundDevice.put("lastLongitude", location.getLongitude());
                                mapFoundDevice.put("signalTimeout", Injection.getSettingsRepository().getTimeout());
                            }
                            catch (Error e) {
                                Log.e(TAG, e.getMessage());
                            }

                            // Save updated object
                            Backendless.Persistence.save(mapFoundDevice, new AsyncCallback<Map>() {
                                @Override
                                public void handleResponse(Map response) {
                                    Log.d(TAG, "obj updated");
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.d(TAG, fault.getMessage());
                                }
                            });
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            // an error has occurred, the error code can be retrieved with fault.getCode()
                            Log.d(TAG, fault.getMessage());
                        }
                    });
        } else {
            Log.d(TAG, "localToken is null -or- non-existent");
        }
    }

    /*
     * Sends a notification to all devices within a certain distance
     */
    @Override
    public void pushNotification(final String tickerText, final String contentTitle,
                                 final String contentText, final String message,
                                 final double latitude, final double longitude) {

        // Get local device-token, latitude & longitude (from settings)
        final String localToken = Injection.getSettingsRepository().getTokenFromPreferences();

        // Build query
        String whereClause = "distance( "+ latitude +", "+ longitude +", " +
                "lastLatitude, lastLongitude ) < signalRadius * 1000";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("DeviceRegistration").find(queryBuilder,
                new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> devices) {

                List<String> notifiedDevices = new ArrayList<>();

                // Iterates through all devices, excludes itself
                for (Map device : devices) {
                    String deviceToken = device.get("deviceToken").toString();

                    if(deviceToken != localToken) {
                        notifiedDevices.add(deviceToken);
                    }
                }

                // Checks to see if there are any devices
                if (notifiedDevices.size() > 0) {

                    // Creates delivery options
                    DeliveryOptions deliveryOptions = new DeliveryOptions();
                    deliveryOptions.setPushSinglecast(notifiedDevices);

                    // Creates publish options
                    PublishOptions publishOptions = new PublishOptions();
                    publishOptions.putHeader("android-ticker-text",
                            tickerText);
                    publishOptions.putHeader("android-content-title",
                            contentTitle);
                    publishOptions.putHeader("android-content-text",
                            message);

                    // Delivers notification
                    MessageStatus status = Backendless.Messaging.publish(message,
                            publishOptions, deliveryOptions);
                    Log.d(TAG, status.getMessageId());
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d(TAG, fault.getMessage());
            }
        });
    }
}
