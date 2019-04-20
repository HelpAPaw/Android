package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;
import android.util.Log;
import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.push.DeviceRegistrationResult;

import org.helpapaw.helpapaw.utils.Injection;

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
                Injection.getSettingsRepositoryInstance().saveTokenToPreferences(response.getDeviceToken());

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
        String localToken = Injection.getSettingsRepositoryInstance().getTokenFromPreferences();

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
                                mapFoundDevice.put("signalRadius", Injection.getSettingsRepositoryInstance().getRadius());
                                mapFoundDevice.put("lastLatitude", location.getLatitude());
                                mapFoundDevice.put("lastLongitude", location.getLongitude());
                                mapFoundDevice.put("signalTimeout", Injection.getSettingsRepositoryInstance().getTimeout());
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
}
