package org.helpapaw.helpapaw.data.repositories;

import android.location.Location;
import android.util.Log;

import com.backendless.Backendless;
import com.backendless.DeviceRegistration;
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
import org.helpapaw.helpapaw.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.helpapaw.helpapaw.base.PawApplication.getContext;
import static org.helpapaw.helpapaw.data.models.Signal.HELP_IS_NEEDED;
import static org.helpapaw.helpapaw.data.models.Signal.SOLVED;
import static org.helpapaw.helpapaw.data.models.Signal.SOMEBODY_ON_THE_WAY;

public class BackendlessPushNotificationsRepository implements PushNotificationsRepository {
    enum SignalUpdate {
        NEW_STATUS, NEW_COMMENT
    }

    private static final String TAG = BackendlessPushNotificationsRepository.class.getSimpleName();
    private static final String productionChannel = "default";
    private static final String debugChannel = "debug";
    public static final int SIGNAL_TYPES_SIZE = getContext().getResources().getStringArray(R.array.signal_types_items).length;
    public static final int MAX_PADDING = 65535;
    private static Location lastKnownDeviceLocation;
    private static final int pageSize = 100;

    private String getNotificationChannel() {
        if (PawApplication.getIsTestEnvironment()) {
            return debugChannel;
        }
        else {
            return productionChannel;
        }
    }

    /* Check if device is already registered and only register it if it's not. This avoids
       recreating notification channels which leads to current notifications from those channels
       disappearing
     */
    @Override
    public void registerDeviceTokenIfNeeded() {
        Backendless.Messaging.getDeviceRegistration(new AsyncCallback<DeviceRegistration>() {
            @Override
            public void handleResponse(DeviceRegistration response) {
                // Do nothing - device already registered
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                // 5000: Unable to retrieve device registration - unknown device ID.
                if (fault.getCode().equals("5000")) {
                    registerDeviceToken();
                }
                else {
                    Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                }
            }
        });
    }

    @Override
    public void registerDeviceToken() {
        Backendless.Messaging.registerDevice(Collections.singletonList(getNotificationChannel()), new AsyncCallback<DeviceRegistrationResult>(){
            @Override
            public void handleResponse(DeviceRegistrationResult response) {
                ISettingsRepository settingsRepository = Injection.getSettingsRepositoryInstance();

                //Save device-token in preferences
                settingsRepository.saveTokenToPreferences(response.getDeviceToken());

                //Update device info in case the registration is new
                updateDeviceInfoInCloud(
                        lastKnownDeviceLocation,
                        settingsRepository.getRadius(),
                        settingsRepository.getTimeout(),
                        settingsRepository.getSignalTypes()
                );
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
            }
        });
    }

    @Override
    public void unregisterDeviceToken() {
        Backendless.Messaging.unregisterDevice(Collections.singletonList(getNotificationChannel()), new AsyncCallback<Integer>() {
            @Override
            public void handleResponse(Integer response) {
                Injection.getSettingsRepositoryInstance().deleteTokenFromPreferences();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
            }
        });
    }

    /*
     * Queries Backendless for locally saved device-token and updates properties on the corresponding db-entry in the cloud
     * Send null for any parameter you don't want to update
     */
    @Override
    public void updateDeviceInfoInCloud(final Location location,
                                        final Integer radius,
                                        final Integer timeout,
                                        final Integer signalTypes) {
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
                                        String wktPoint = Utils.getWktPoint(location.getLongitude(), location.getLatitude());
                                        foundDevice.put("lastLocation", wktPoint);
                                        lastKnownDeviceLocation = location;
                                    }
                                    if (radius != null) {
                                        foundDevice.put("signalRadius", radius);
                                    }
                                    if (timeout != null) {
                                        foundDevice.put("signalTimeout", timeout);
                                    }
                                    if (signalTypes != null) {
                                        foundDevice.put("signalTypes", convertSignalTypeForDb(signalTypes));
                                    }
                                }
                                catch (Error e) {
                                    Injection.getCrashLogger().recordException(e);
                                }

                                // Save updated object
                                Backendless.Persistence.of("DeviceRegistration").save(foundDevice, new AsyncCallback<Map>() {
                                    @Override
                                    public void handleResponse(Map response) {
                                        Log.d(TAG, "obj updated");
                                    }

                                    @Override
                                    public void handleFault(BackendlessFault fault) {
                                        Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                                    }
                                });
                            }
                            else {
                                Injection.getCrashLogger().recordException(new Throwable("Device token not found in server DB."));
                            }
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                        }
                    });
        } else {
            Injection.getCrashLogger().recordException(new Throwable("localToken is null -or- non-existent"));
        }
    }

    /*
     * Public method that sends a notification to all devices within a certain distance
     */
    @Override
    public void pushNewSignalNotification(final Signal signal) {

        // Get local device-token, latitude & longitude (from settings)
        final String localToken = Injection.getSettingsRepositoryInstance().getTokenFromPreferences();

        // Build query
        String wktPoint = Utils.getWktPoint(signal.getLongitude(), signal.getLatitude());
        String whereClause = "distanceOnSphere( lastLocation, '" + wktPoint + "') < signalRadius * 1000";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setPageSize(pageSize);

        pushNewSignalNotifications(signal, localToken, queryBuilder, 0);
    }

    /*
     * Private method that recursively sends a notification to all interested devices
     */
    private void pushNewSignalNotifications(final Signal signal, final String localToken, final DataQueryBuilder queryBuilder, final int offset) {
        queryBuilder.setOffset(offset);

        Backendless.Data.of("DeviceRegistration").find(queryBuilder,
                new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> devices) {

                List<String> notifiedDevices = new ArrayList<>();

                // Iterates through all devices, excludes itself
                for (Map device : devices) {
                    String deviceToken = device.get("deviceToken").toString();

                    if(!deviceToken.equals(localToken)) {
                        int signalTypesSetting = (int) device.get("signalTypes");

                        if (Utils.shouldNotifyAboutSignalType(signal.getType(), signalTypesSetting)) {
                            notifiedDevices.add(device.get("deviceId").toString());
                        }
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
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                        }
                    });
                }

                if (devices.size() == pageSize) {
                    pushNewSignalNotifications(signal, localToken, queryBuilder, offset + pageSize);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
            }
        });
    }

    @Override
    public void pushNewCommentNotification(Signal signal, String newComment, List<Comment> currentComments) {
        pushSignalUpdatedNotification(signal, currentComments, SignalUpdate.NEW_COMMENT, 0, newComment);
    }

    @Override
    public void pushNewStatusNotification(final Signal signal, final int newStatus, final List<Comment> currentComments) {
        pushSignalUpdatedNotification(signal, currentComments, SignalUpdate.NEW_STATUS, newStatus, null);
    }

    /*
     * Public method that sends a notification to all users interested in a signal (author and all commenters)
     * This method is triggered in two situations (differentiated by the SignalUpdate parameter) - new comment or new status
     */
    private void pushSignalUpdatedNotification(final Signal signal, final List<Comment> currentComments, final SignalUpdate signalUpdate, final int newStatus, final String newComment) {

        // Use Set to ensure there are no double entries
        Set<String> interestedUserIds = new HashSet<>();
        // Add author of the signal
        if (signal.getAuthorId() != null) {
            interestedUserIds.add(signal.getAuthorId());
        }
        // Add all people that posted comments
        if (currentComments != null) {
            for (Comment comment : currentComments) {
                if (comment.getAuthorId() != null) {
                    interestedUserIds.add(comment.getAuthorId());
                }
            }
        }
        // Remove current user
        interestedUserIds.remove(Backendless.UserService.CurrentUser().getObjectId());

        if (interestedUserIds.size() == 0) {
            return;
        }

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
        queryBuilder.setPageSize(pageSize);

        pushSignalUpdatedNotifications(signal, signalUpdate, newStatus, newComment, queryBuilder, 0);
    }

    /*
     * Private method that recursively sends a notification to all pages of devices interested in a signal update
     */
    private void pushSignalUpdatedNotifications(final Signal signal, final SignalUpdate signalUpdate, final int newStatus, final String newComment, final DataQueryBuilder queryBuilder, final int offset) {
        queryBuilder.setOffset(offset);

        Backendless.Data.of("DeviceRegistration").find(queryBuilder,
                new AsyncCallback<List<Map>>() {
                    @Override
                    public void handleResponse(List<Map> devices) {

                        if (devices == null) return;

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
                                updateType = PawApplication.getContext().getString(R.string.txt_new_comment);
                                updateContent = newComment;
                            }
                            else if (signalUpdate == SignalUpdate.NEW_STATUS) {
                                updateType = PawApplication.getContext().getString(R.string.txt_new_status);
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
                                }

                                @Override
                                public void handleFault(BackendlessFault fault) {
                                    Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                                }
                            });
                        }

                        if (devices.size() == pageSize) {
                            pushSignalUpdatedNotifications(signal, signalUpdate, newStatus, newComment, queryBuilder, offset + pageSize);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                    }
                });
    }

    private int convertSignalTypeForDb(final int signalTypes) {
        return ((MAX_PADDING << SIGNAL_TYPES_SIZE) & MAX_PADDING) | signalTypes;
    }
}
