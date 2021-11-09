package org.helpapaw.helpapaw.mysignals;

import android.view.View;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.HashSet;
import java.util.List;

public class MySubmittedSignalsPresenter extends Presenter<MySignalsContract.View> implements MySignalsContract.UserActionsListener {

    private SignalRepository signalRepository;
    private PhotoRepository photoRepository;
    private UserManager userManager;

    MySubmittedSignalsPresenter(MySignalsContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
    }

    @Override
    public void onLoadMySignals() {
        if (userManager.isLoggedIn()) {
            String loggedUserId = userManager.getLoggedUserId();

            getSubmittedSignalsfromDb(loggedUserId);
        }
    }

    private void getSubmittedSignalsfromDb(String ownerId) {
        if (Utils.getInstance().hasNetworkConnection()) {

            if (userManager.isLoggedIn()) {
                getView().setProgressVisibility(View.VISIBLE);
            }

            signalRepository.getSignalsByOwnerId(ownerId,
                    new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            if (!isViewAvailable()) return;

                            if (signals.size() != 0) {

                                for (Signal signal : signals) {
                                    signal.setPhotoUrl(photoRepository.getSignalPhotoUrl(signal.getId()));
                                }

                                getView().displaySignals(signals);
                                getView().setProgressVisibility(View.GONE);
                                getView().onNoSignalsToBeListed(false);
                            } else {
                                getView().onNoSignalsToBeListed(true);
                            }
                        }

                        @Override
                        public void onSignalsFailure(String message) {
                            if (!isViewAvailable()) return;
                            getView().showMessage(message);
                        }
                    });

        } else {
            getView().showNoInternetMessage();
        }
    }

    private boolean isViewAvailable() {
        return getView() != null;
    }
}
