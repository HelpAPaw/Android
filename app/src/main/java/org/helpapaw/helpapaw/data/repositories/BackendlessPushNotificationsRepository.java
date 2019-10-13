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

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.helpapaw.helpapaw.data.models.Signal.HELP_IS_NEEDED;
import static org.helpapaw.helpapaw.data.models.Signal.SOLVED;
import static org.helpapaw.helpapaw.data.models.Signal.SOMEBODY_ON_THE_WAY;

public class BackendlessPushNotificationsRepository implements PushNotificationsRepository {
    private static final String TAG = BackendlessPushNotificationsRepository.class.getSimpleName();
    private static final String productionChannel = "default";
    private static final String debugChannel = "debug";
    private static Location lastKnownDeviceLocation;

    private String getNotificationChannel() {
        if (PawApplication.getIsTestEnvironment()) {
            return debugChannel;
        }
        else {
            return productionChannel;
        }
    }

    @Override
    public void registerDeviceToken() {
        Backendless.Messaging.registerDevice(Arrays.asList(getNotificationChannel()), new AsyncCallback<DeviceRegistrationResult>(){
            @Override
            public void handleResponse(DeviceRegistrationResult response) {
                ISettingsRepository settingsRepository = Injection.getSettingsRepositoryInstance();

                //Save device-token in preferences
                settingsRepository.saveTokenToPreferences(response.getDeviceToken());

                //Update device info in case the registration is new
                updateDeviceInfoInCloud(lastKnownDeviceLocation, settingsRepository.getRadius(), settingsRepository.getTimeout());
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Device registration fault: " + fault.getMessage());
            }
        });
    }

    @Override
    public void unregisterDeviceToken() {
        Backendless.Messaging.unregisterDevice(Arrays.asList(getNotificationChannel()), new AsyncCallback<Integer>() {
            @Override
            public void handleResponse(Integer response) {
                Injection.getSettingsRepositoryInstance().deleteTokenFromPreferences();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.e(TAG, "Device unregistration fault: " + fault.getMessage());
            }
        });
    }

    /*
     * Queries Backendless for locally saved device-token and updates properties on the corresponding db-entry in the cloud
     * Send null for any parameter you don't want to update
     */
    @Override
    public void updateDeviceInfoInCloud(final Location location, final Integer radius, final Integer timeout) {
        // Get local device-token
        final String localToken = Injection.getSettingsRepositoryInstance().getTokenFromPreferences();

        // Make sure localToken exists
        if (localToken != null) {

            // Build query
            String whereClause = "deviceToken = '" + localToken + "'";
            DataQueryBuilder queryBuilder = DataQueryBuilder.create();
            queryBuilder.setWhereClause(whereClause);

            Backendless.Data.of("DeviceRegistration").find(queryBuilder,
                    new AsyncCallback<List<Map>>() {
                        @Override
                        public void handleResponse(List<Map> foundDevices) {
                            // every loaded object from the "DeviceRegistration" table
                            // is now an individual java.util.Map

                            if ((foundDevices != null) && (foundDevices.size() > 0)) {
                                // Extract 'Map' object from the 'List<Map>'
                                Map foundDevice = foundDevices.get(0);
                                try {
                                    if (location != null) {
                                        foundDevice.put("lastLatitude", location.getLatitude());
                                        foundDevice.put("lastLongitude", location.getLongitude());
                                        lastKnownDeviceLocation = location;
                                    }
                                    if (radius != null) {
                                        foundDevice.put("signalRadius", radius);
                                    }
                                    if (timeout != null) {
                                        foundDevice.put("signalTimeout", timeout);
                                    }
                                }
                                catch (Error e) {
                                    Log.e(TAG, e.getMessage());
                                }

                                // Save updated object
                                Backendless.Persistence.of("DeviceRegistration").save(foundDevice, new AsyncCallback<Map>() {
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
                            else {
                                Log.e(TAG, "Device token not found in server DB.");
                            }
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
    public void pushNewSignalNotification(final Signal signal, final double latitude, final double longitude) {

        // Get local device-token, latitude & longitude (from settings)
        final String localToken = Injection.getSettingsRepositoryInstance().getTokenFromPreferences();

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

                    if(!deviceToken.equals(localToken)) {
                        notifiedDevices.add(device.get("deviceId").toString());
                    }
                }

                // Checks to see if there are any devices
                if (notifiedDevices.size() > 0) {

                    String newSignalString = PawApplication.getContext().getString(R.string.txt_new_signal);

                    // Creates delivery options
                    DeliveryOptions deliveryOptions = new DeliveryOptions();
                    deliveryOptions.setPushSinglecast(notifiedDevices);

                    // Creates publish options
                    PublishOptions publishOptions = new PublishOptions();
                    publishOptions.putHeader("android-ticker-text", newSignalString);
                    publishOptions.putHeader("android-content-title", signal.getTitle());
                    publishOptions.putHeader("ios-alert-title", signal.getTitle());
                    publishOptions.putHeader("ios-badge", "1");
                    publishOptions.putHeader("ios-sound", "default");
                    publishOptions.putHeader(Signal.KEY_SIGNAL_ID, signal.getId());
                    publishOptions.putHeader("ios-category", "kNotificationCategoryNewSignal");


                    // Delivers notification
                    Backendless.Messaging.publish(getNotificationChannel(), newSignalString, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                        @Override
                        public void handleResponse(MessageStatus response) {
                            Log.d(TAG, response.getMessageId());
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Log.d(TAG, fault.getMessage());
                        }
                    });

                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Log.d(TAG, fault.getMessage());
            }
        });
    }

    /*
     * Sends a notification to all users interested in a signal (author and all commenters)
     * This method is triggered in two situations (differentiated by the SignalUpdate parameter) - new comment or new status
     */
    @Override
    public void pushSignalUpdatedNotification(final Signal signal, final List<Comment> currentComments, final PushNotificationsRepository.SignalUpdate signalUpdate, final int newStatus, final String newComment) {

        // Use Set to ensure there are no double entries
        Set<String> interestedUserIds = new HashSet<>();
        // Add author of the signal
        interestedUserIds.add(signal.getAuthorId());
        // Add all people that posted comments
        for (Comment comment : currentComments) {
            interestedUserIds.add(comment.getAuthorId());
        }
        // Remove current user
        interestedUserIds.remove(Backendless.UserService.CurrentUser().getObjectId());

        // Create a comma separated list
        StringBuilder userIdsBuilder = new StringBuilder();
        for (String string : interestedUserIds)
        {
            // Strings in the WHERE clause array need to be in single quotes
            string = "'" + string + "'";
            userIdsBuilder = userIdsBuilder.length() > 0 ? userIdsBuilder.append(",").append(string) : userIdsBuilder.append(string);
        }
        String userIds = userIdsBuilder.toString();

        // Build query
        String whereClause = "user.objectid IN (" + userIds + ")";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);

        Backendless.Data.of("DeviceRegistration").find(queryBuilder,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> devices) {

                        List<String> deviceIds = new ArrayList<>();
                        for (Map device : devices) {
                            deviceIds.add(device.get("deviceId").toString());
                        }

                        // Checks to see if there are any devices
                        if (deviceIds.size() > 0) {
                            // Creates delivery options
                            DeliveryOptions deliveryOptions = new DeliveryOptions();
                            deliveryOptions.setPushSinglecast(deviceIds);

                            String updateType = "";
                            String updateContent = "";
                            if (signalUpdate == SignalUpdate.NEW_COMMENT) {
                                updateType = "New comment";
                                updateContent = newComment;
                            }
                            else if (signalUpdate == SignalUpdate.NEW_STATUS) {
                                updateType = "New status";
                                if (newStatus == HELP_IS_NEEDED) {
                                    updateContent = PawApplication.getContext().getString(R.string.txt_you_help_is_needed);
                                }
                                else if (newStatus == SOMEBODY_ON_THE_WAY) {
                                    updateContent = PawApplication.getContext().getString(R.string.txt_somebody_is_on_the_way);
                                }
                                else if (newStatus == SOLVED) {
                                    updateContent = PawApplication.getContext().getString(R.string.txt_solved);
                                }
                            }

                            // Creates publish options
                            PublishOptions publishOptions = new PublishOptions();
                            publishOptions.putHeader("android-ticker-text", updateType);
                            publishOptions.putHeader("android-content-title", signal.getTitle());
                            publishOptions.putHeader("android-content-text", updateContent);
                            publishOptions.putHeader("ios-alert", updateContent);
                            publishOptions.putHeader("ios-alert-title", updateType);
                            publishOptions.putHeader("ios-alert-subtitle", signal.getTitle());
                            publishOptions.putHeader("ios-alert-body", updateContent);
                            publishOptions.putHeader("ios-badge", "1");
                            publishOptions.putHeader("ios-sound", "default");
                            publishOptions.putHeader(Signal.KEY_SIGNAL_ID, signal.getId());
                            //TODO: create new category
                            publishOptions.putHeader("ios-category", "kNotificationCategoryNewSignal");

                            // Delivers notification
                            Backendless.Messaging.publish(getNotificationChannel(), updateContent, publishOptions, deliveryOptions, new AsyncCallback<MessageStatus>() {
                                @Override
                                public void handleResponse(MessageStatus response) {
                                    Log.d(TAG, response.getMessageId());
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Log.d(TAG, fault.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Log.d(TAG, fault.getMessage());
                    }
                });
    }
}
