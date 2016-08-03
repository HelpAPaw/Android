package org.helpapaw.helpapaw.utils;

import org.helpapaw.helpapaw.data.repositories.BackendlessPhotoRepository;
import org.helpapaw.helpapaw.data.repositories.BackendlessSignalRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
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
            signalRepositoryInstance = new BackendlessSignalRepository();
        }
        return signalRepositoryInstance;
    }

    public synchronized static PhotoRepository getPhotoRepositoryInstance() {
        if (photoRepository == null) {
            photoRepository = new BackendlessPhotoRepository();
        }
        return photoRepository;
    }
}
