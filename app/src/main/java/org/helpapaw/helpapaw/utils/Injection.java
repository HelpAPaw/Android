package org.helpapaw.helpapaw.utils;

import org.helpapaw.helpapaw.data.repositories.BackendlessCommentRepository;
import org.helpapaw.helpapaw.data.repositories.BackendlessPhotoRepository;
import org.helpapaw.helpapaw.data.repositories.BackendlessPushNotificationsRepository;
import org.helpapaw.helpapaw.data.repositories.BackendlessSpatialSignalRepository;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.ISettingsRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.PushNotificationsRepository;
import org.helpapaw.helpapaw.data.repositories.SettingsRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.BackendlessUserManager;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.images.ImageLoader;
import org.helpapaw.helpapaw.utils.images.PicassoImageLoader;

/**
 * Created by iliyan on 7/25/16
 */
public class Injection {
    private static ImageLoader imageLoader;
    private static UserManager userManagerInstance;
    private static SignalRepository signalRepositoryInstance;
    private static PhotoRepository photoRepository;
    private static CommentRepository commentRepository;
    private static SettingsRepository settingsRepository;
    private static PushNotificationsRepository pushNotificationsRepository;

    public synchronized static ImageLoader getImageLoader() {
        if (imageLoader == null) {
            imageLoader = new PicassoImageLoader();
        }
        return imageLoader;
    }

    public synchronized static UserManager getUserManagerInstance() {
        if (userManagerInstance == null) {
            userManagerInstance = new BackendlessUserManager();
        }
        return userManagerInstance;
    }

    public synchronized static SignalRepository getSignalRepositoryInstance() {
        if (signalRepositoryInstance == null) {
            signalRepositoryInstance = new BackendlessSpatialSignalRepository();
        }
        return signalRepositoryInstance;
    }

    public synchronized static PhotoRepository getPhotoRepositoryInstance() {
        if (photoRepository == null) {
            photoRepository = new BackendlessPhotoRepository();
        }
        return photoRepository;
    }

    public synchronized static CommentRepository getCommentRepositoryInstance() {
        if (commentRepository == null) {
            commentRepository = new BackendlessCommentRepository();
        }
        return commentRepository;
    }

    public synchronized static ISettingsRepository getSettingsRepositoryInstance() {
        if (settingsRepository == null) {
            settingsRepository = new SettingsRepository();
        }

        return settingsRepository;
    }

    public synchronized static PushNotificationsRepository getPushNotificationsRepositoryInstance() {
        if (pushNotificationsRepository == null) {
            pushNotificationsRepository = new BackendlessPushNotificationsRepository();
        }

        return pushNotificationsRepository;
    }
}
