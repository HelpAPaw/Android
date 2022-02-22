package org.helpapaw.helpapaw.userprofile;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;


/**
 * Created by iliyan on 7/25/16
 */
public class UserProfilePresenter extends Presenter<UserProfileContract.View>
        implements UserProfileContract.UserActionsListener {

    private final UserManager userManager;

    public UserProfilePresenter(UserProfileContract.View view) {
        super(view);

        userManager = Injection.getUserManagerInstance();
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }
}
