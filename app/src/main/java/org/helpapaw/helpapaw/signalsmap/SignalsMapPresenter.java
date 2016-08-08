package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public class SignalsMapPresenter extends Presenter<SignalsMapContract.View> implements SignalsMapContract.UserActionsListener {

    private static final float DEFAULT_MAP_ZOOM = 14.5f;
    private static final int DEFAULT_SEARCH_RADIUS = 4000;

    private UserManager userManager;
    private SignalRepository signalRepository;
    private PhotoRepository photoRepository;

    private double latitude;
    private double longitude;
    private String photoUri;
    private boolean sendSignalViewVisibility;
    private List<Signal> signalsList;

    public SignalsMapPresenter(SignalsMapContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        sendSignalViewVisibility = false;
        signalsList = new ArrayList<>();
    }

    @Override
    public void onInitSignalsMap() {
        getView().setAddSignalViewVisibility(sendSignalViewVisibility);
        if (!isEmpty(photoUri)) {
            getView().setThumbnailImage(photoUri);
        }
        if (signalsList != null && signalsList.size() > 0) {
            getView().displaySignals(signalsList);
        }
    }

    private void getAllSignals(double latitude, double longitude) {
        signalRepository.getAllSignals(latitude, longitude, DEFAULT_SEARCH_RADIUS,
                new SignalRepository.LoadSignalsCallback() {
                    @Override
                    public void onSignalsLoaded(List<Signal> signals) {
                        if (!isViewAvailable()) return;
                        signalsList = signals;
                        getView().displaySignals(signals);
                    }

                    @Override
                    public void onSignalsFailure(String message) {
                        if (!isViewAvailable()) return;
                        getView().showMessage(message);
                    }
                });
    }

    @Override
    public void onLocationChanged(double latitude, double longitude) {

        if (this.latitude != 0 && this.longitude != 0) {
            if (Utils.getInstance().getDistanceBetween(latitude, longitude, this.latitude, this.longitude) > 300) {
                getAllSignals(latitude, longitude);
            }
        } else {
            getAllSignals(latitude, longitude);
        }

        this.latitude = latitude;
        this.longitude = longitude;

        getView().updateMapCameraPosition(latitude, longitude, DEFAULT_MAP_ZOOM);
    }

    @Override
    public void onAddSignalClicked(boolean visibility) {
        setSendSignalViewVisibility(!visibility);
    }

    @Override
    public void onSendSignalClicked(final String description) {
        getView().hideKeyboard();
        getView().setSignalViewProgressVisibility(true);

        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess() {
                if (!isViewAvailable()) return;
                Long tsLong = System.currentTimeMillis() / 1000;
                String timestamp = tsLong.toString();

                if (isEmpty(description)) {
                    //TODO: extract text
                    getView().showMessage("Signal description is required!");
                } else {
                    saveSignal(description, timestamp, 0, latitude, longitude);
                }
            }

            @Override
            public void onLoginFailure(String message) {
                if (!isViewAvailable()) return;
                getView().setSignalViewProgressVisibility(false);
                getView().openLoginScreen();
            }
        });
    }

    private void saveSignal(String description, String timestamp, int status,
                            final double latitude, final double longitude) {
        signalRepository.saveSignal(new Signal(description, timestamp, status, latitude, longitude), new SignalRepository.SaveSignalCallback() {
            @Override
            public void onSignalSaved(String signalId) {
                if (!isViewAvailable()) return;
                if (!isEmpty(photoUri)) {
                    savePhoto(photoUri, signalId);
                } else {
                    getAllSignals(latitude, longitude);
                    getView().setAddSignalViewVisibility(false);
                    getView().clearSignalViewData();
                }
            }

            @Override
            public void onSignalFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    private void savePhoto(String photoUri, String signalId) {
        photoRepository.savePhoto(photoUri, signalId, new PhotoRepository.SavePhotoCallback() {
            @Override
            public void onPhotoSaved() {
                if (!isViewAvailable()) return;
                getAllSignals(latitude, longitude);
                //TODO: extract text
                getView().setAddSignalViewVisibility(false);
                getView().clearSignalViewData();
                getView().showMessage("Signal successfully added!");
            }

            @Override
            public void onPhotoFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    @Override
    public void onChoosePhotoIconClicked() {
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

    @Override
    public void onStoragePermissionGranted() {
        getView().setThumbnailImage(photoUri);
    }

    @Override
    public void onSignalInfoWindowClicked(Signal signal) {
        getView().openSignalDetailsScreen(signal);
    }

    @Override
    public void onBackButtonPressed() {
        if (sendSignalViewVisibility) {
            setSendSignalViewVisibility(false);
        } else {
            getView().closeSignalsMapScreen();
        }
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    private void setSendSignalViewVisibility(boolean visibility) {
        sendSignalViewVisibility = visibility;
        getView().setAddSignalViewVisibility(visibility);
    }

    private boolean isEmpty(String value) {
        return !(value != null && value.length() > 0);
    }
}
