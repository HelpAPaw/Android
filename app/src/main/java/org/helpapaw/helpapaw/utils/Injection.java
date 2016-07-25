package org.helpapaw.helpapaw.utils;

import org.helpapaw.helpapaw.data.user.BackendlessUserManager;
import org.helpapaw.helpapaw.data.user.UserManager;

/**
 * Created by iliyan on 7/25/16
 */
public class Injection {
    private static UserManager userManagerInstance;

    public synchronized static UserManager getUserManagerInstance() {
        if (userManagerInstance == null) {
            userManagerInstance = new BackendlessUserManager();
        }
        return userManagerInstance;
    }

}
