package org.helpapaw.helpapaw.utils;

import org.helpapaw.helpapaw.data.repositories.BackendlessSignalRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.BackendlessUserManager;
import org.helpapaw.helpapaw.data.user.UserManager;

/**
 * Created by iliyan on 7/25/16
 */
public class Injection {
    private static ImageLoader glideImageLoader;
    private static UserManager userManagerInstance;
    private static SignalRepository signalRepositoryInstance;

    public synchronized static ImageLoader getImageLoader() {
        if (glideImageLoader == null) {
            glideImageLoader = new GlideImageLoader();
        }
        return glideImageLoader;
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
}
