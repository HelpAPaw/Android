package org.helpapaw.helpapaw.mysignals;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.List;


public class MySignalsPresenter extends Presenter<MySignalsContract.View> implements MySignalsContract.UserActionsListener {

    private SignalRepository signalRepository;
    private PhotoRepository photoRepository;
    private UserManager userManager;

    MySignalsPresenter(MySignalsContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
    }

    @Override
    public void onOpenMySignalsScreen() {
        if (userManager.isLoggedIn()) {
            String loggedUserId = userManager.getLoggedUserId();
            getMySignalsfromDb(loggedUserId);
            // TODO do the same with the commented signals
        } else {
            getView().showRegistrationRequiredAlert();
        }
    }

    private void getMySignalsfromDb(String ownerId) {
        if (Utils.getInstance().hasNetworkConnection()) {

            signalRepository.getSignalsByOwnerId(ownerId,
                    new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            if (!isViewAvailable()) return;

                            for (Signal signal : signals) {
                                signal.setPhotoUrl(photoRepository.getSignalPhotoUrl(signal.getId()));
                            }

                            getView().displaySignals(signals);
                        }

                        @Override
                        public void onSignalsFailure(String message) {
                            if (!isViewAvailable()) return;
//                            getView().showMessage(message);
                        }
                    });

        } else {
//            getView().showNoInternetMessage();
        }
    }

    private boolean isViewAvailable() {
        return getView() != null;
    }
}
