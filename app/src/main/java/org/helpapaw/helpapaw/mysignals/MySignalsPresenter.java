package org.helpapaw.helpapaw.mysignals;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;

public class MySignalsPresenter extends Presenter<MySignalsContract.View> implements MySignalsContract.UserActionsListener {

    private SignalRepository signalRepository;
    private UserManager userManager;
//    private int radius;
//    private int timeout;
//    private int signalTypes;

    MySignalsPresenter(MySignalsContract.View view) {
        super(view);
//        settingsRepository = Injection.getSettingsRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
    }

    @Override
    public void onOpenMySignalsScreen() {
        if (!userManager.isLoggedIn()) {
            getView().showRegistrationRequiredAlert();
        }
    }
}
