package org.helpapaw.helpapaw.mynotifications;

import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;
import java.util.Map;

public class MyNotificationsContract {

    interface View {
        void displayNotifications(List<Notification> notifications, Map<String, Signal> mapSignalsToIds);

        void deleteMyNotifications();

        void showMessage(String message);

        void showNoInternetMessage();

        void setProgressVisibility(int visibility);

        void onNoNotificationsToBeListed(boolean zeroNotifications);
    }

    interface UserActionsListener {
        void onViewResume();

        void onDeleteMyNotificationsClicked();

        void onDeleteMyNotifications();
    }
}
