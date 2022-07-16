package org.helpapaw.helpapaw.utils.services;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.backendless.push.BackendlessFCMService;

import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.utils.Injection;

public class CustomBackendlessFCMService extends BackendlessFCMService {

    @Override
    public boolean onMessage(Context appContext, Intent msgIntent)
    {
        Notification notification = createNotificationFromIntent(msgIntent);

        Injection.getReceivedNotificationsRepositoryInstance().saveNotification(notification);

        return true;
    }

    @NonNull
    private Notification createNotificationFromIntent(Intent msgIntent) {

        String signalId = msgIntent.getStringExtra("signalId");
        String photoUrl = Injection.getPhotoRepositoryInstance().getSignalPhotoUrl(signalId);
        String notificationType = msgIntent.getStringExtra("android-ticker-text");
        String notificationMessage = msgIntent.getStringExtra("android-content-title");
        String notificationText = notificationType + ": " + notificationMessage;

        return new Notification(signalId, photoUrl, notificationText);
    }
}
