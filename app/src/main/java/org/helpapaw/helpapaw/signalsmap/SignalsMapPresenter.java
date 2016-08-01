package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;

import java.io.File;
import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public class SignalsMapPresenter extends Presenter<SignalsMapContract.View> implements SignalsMapContract.UserActionsListener {

    private static final float DEFAULT_MAP_ZOOM = 14.5f;
    private static final int DEFAULT_SEARCH_RADIUS = 50;

    private UserManager userManager;
    private SignalRepository signalRepository;

    public SignalsMapPresenter(SignalsMapContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
    }

    @Override
    public void onLocationChanged(double latitude, double longitude) {

        getView().updateMapCameraPosition(latitude, longitude, DEFAULT_MAP_ZOOM);

        signalRepository.getAllSignals(latitude, longitude, DEFAULT_SEARCH_RADIUS,
                new SignalRepository.LoadSignalsCallback() {
                    @Override
                    public void onSignalsLoaded(List<Signal> signals) {
                        getView().displaySignals(signals);
                    }

                    @Override
                    public void onSignalsFailure(String message) {
                        getView().showMessage(message);
                    }
                });
    }

    @Override
    public void onAddSignalClicked() {
        getView().toggleAddSignalView();
    }

    @Override
    public void onSendSignalClicked(String description, File photo) {
        getView().hideKeyboard();
        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess() {
                //sendSignal();
            }

            @Override
            public void onLoginFailure(String message) {
                getView().openLoginScreen();
            }
        });
    }

    @Override
    public void onSignalPhotoClicked() {
        getView().hideKeyboard();
        getView().showSendPhotoBottomSheet();
    }

    @Override
    public void onCameraOptionSelected() {
        getView().openCamera();
    }

    @Override
    public void onGalleryOptionSelected() {
        getView().openGallery();
    }


}
