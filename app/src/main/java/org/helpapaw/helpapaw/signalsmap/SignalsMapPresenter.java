package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public class SignalsMapPresenter extends Presenter<SignalsMapContract.View> implements SignalsMapContract.UserActionsListener {

    private static final float DEFAULT_MAP_ZOOM = 14.5f;
    private static final int DEFAULT_SEARCH_RADIUS = 50;

    private UserManager userManager;
    private SignalRepository signalRepository;
    private PhotoRepository photoRepository;

    private double latitude;
    private double longitude;
    private String photoUri;

    public SignalsMapPresenter(SignalsMapContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
    }

    @Override
    public void onLocationChanged(double latitude, double longitude) {

        this.latitude = latitude;
        this.longitude = longitude;

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
    public void onSendSignalClicked(final String description) {
        getView().hideKeyboard();
        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess() {
                Long tsLong = System.currentTimeMillis() / 1000;
                String timestamp = tsLong.toString();

                signalRepository.saveSignal(new Signal(description, timestamp, 0, latitude, longitude), new SignalRepository.SaveSignalCallback() {
                    @Override
                    public void onSignalSaved(String signalId) {
                        savePhoto(photoUri, signalId);
                    }

                    @Override
                    public void onSignalFailure(String message) {
                        getView().showMessage(message);
                    }
                });
            }

            @Override
            public void onLoginFailure(String message) {
                getView().openLoginScreen();
            }
        });
    }

    private void savePhoto(String photoUri, String signalId) {
        photoRepository.savePhoto(photoUri, signalId, new PhotoRepository.SavePhotoCallback() {
            @Override
            public void onPhotoSaved() {
                getView().showMessage("Signal successfully added!");
            }

            @Override
            public void onPhotoFailure(String message) {
                getView().showMessage(message);
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

    @Override
    public void onSignalPhotoSelected(String photoUri) {
        this.photoUri = photoUri;
    }
}
