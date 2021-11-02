package org.helpapaw.helpapaw.utils.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.backendless.messaging.PublishOptions;
import com.backendless.push.BackendlessFCMService;

import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.db.NotificationsDatabase;
import org.helpapaw.helpapaw.db.SignalsDatabase;

public class CustomBackendlessFCMService extends BackendlessFCMService {

    private NotificationsDatabase notificationsDatabase;
    private SignalsDatabase signalsDatabase;

    @Override
    public boolean onMessage(Context appContext, Intent msgIntent)
    {
        Notification notification = createNotificationFromIntent(appContext, msgIntent);

        notificationsDatabase = NotificationsDatabase.getDatabase(appContext);
        notificationsDatabase.notificationDao().saveNotification(notification);

        return true;
    }

    @NonNull
    private Notification createNotificationFromIntent(Context appContext, Intent msgIntent) {
        signalsDatabase = SignalsDatabase.getDatabase(appContext);

        String signalId = msgIntent.getStringExtra("signalId");
        Signal signal = signalsDatabase.signalDao().getSignal(signalId).get(0);

        String notificationType = msgIntent.getStringExtra("android-ticker-text");
        String notificationMessage = msgIntent.getStringExtra( PublishOptions.MESSAGE_TAG );
        if ("new signal".equalsIgnoreCase(notificationType)) {
            notificationMessage = msgIntent.getStringExtra("android-content-title");
        }

        String notificationText = notificationType + ": " + notificationMessage;

        Notification notification = new Notification(signal.getId(), signal.getPhotoUrl(), notificationText);

        return notification;
    }
}
