package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public class SignalsMapPresenter extends Presenter<SignalsMapContract.View> implements SignalsMapContract.UserActionsListener {

    private UserManager userManager;
    private SignalRepository signalRepository;
    private PhotoRepository photoRepository;

    private double latitude;
    private double longitude;
    private int radius;
    private int timeout;

    private double currentMapLatitude;
    private double currentMapLongitude;

    private String photoUri;
    private boolean sendSignalViewVisibility;
    private List<Signal> signalsList;

    SignalsMapPresenter(SignalsMapContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        sendSignalViewVisibility = false;
        signalsList = new ArrayList<>();
    }

    @Override
    public void onInitSignalsMap(String focusedSignalId) {
        getView().setAddSignalViewVisibility(sendSignalViewVisibility);
        if (!isEmpty(photoUri)) {
            getView().setThumbnailImage(photoUri);
        }
        if (focusedSignalId != null) {
            getSignal(focusedSignalId);
        }
        if (signalsList != null && signalsList.size() > 0) {
            getView().displaySignals(signalsList, false);
        }
    }

    private void getSignal(String signalId) {
        if (Utils.getInstance().hasNetworkConnection()) {
            getView().setProgressVisibility(true);

            signalRepository.getSignal(signalId,
                    new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            if (!isViewAvailable()) return;
                            if (signals.size() > 0) {
                                replaceSignal(signals.get(0));
                                signalRepository.markSignalsAsSeen(signals);
                                getView().displaySignals(signals, true);
                                getView().setProgressVisibility(false);
                            }
                        }

                        @Override
                        public void onSignalsFailure(String message) {
                            if (!isViewAvailable()) return;
                            getView().showMessage(message);
                            getView().setProgressVisibility(false);
                        }
                    });
        } else {
            getView().showNoInternetMessage();
        }
    }

    private void getAllSignals(double latitude, double longitude, int radius, int timeout) {
        if (Utils.getInstance().hasNetworkConnection()) {
            getView().setProgressVisibility(true);

            signalRepository.getAllSignals(latitude, longitude, radius, timeout,
                    new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            if (!isViewAvailable()) return;
                            signalsList = signals;
                            signalRepository.markSignalsAsSeen(signals);
                            getView().displaySignals(signals, false);
                            getView().setProgressVisibility(false);
                        }

                        @Override
                        public void onSignalsFailure(String message) {
                            if (!isViewAvailable()) return;
                            getView().showMessage(message);
                            getView().setProgressVisibility(false);
                        }
                    });
        } else {
            getView().showNoInternetMessage();
        }
    }

    @Override
    public void onLocationChanged(double latitude, double longitude, int radius, int timeout) {
        currentMapLatitude = latitude;
        currentMapLongitude = longitude;

        if ((Utils.getInstance().getDistanceBetween(latitude, longitude, this.latitude, this.longitude) > 300)
            || (this.radius != radius)) {
            getAllSignals(latitude, longitude, radius, timeout);

            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
            this.timeout = timeout;
        }
    }

    @Override
    public void onAddSignalClicked(boolean visibility) {
        setSendSignalViewVisibility(!visibility);
    }

    @Override
    public void onCancelAddSignal() {
        getView().displaySignals(signalsList, false);
    }

    @Override
    public void onSendSignalClicked(final String description, final String authorPhone) {
        getView().hideKeyboard();
        getView().setSignalViewProgressVisibility(true);

        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess() {
                if (!isViewAvailable()) return;

                if (isEmpty(description)) {
                    getView().showDescriptionErrorMessage();
                    getView().setSignalViewProgressVisibility(false);
                } else {
                    saveSignal(description, authorPhone, new Date(), 0, currentMapLatitude, currentMapLongitude);
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

    private void saveSignal(String description, String authorPhone, Date dateSubmitted, int status, final double latitude, final double longitude) {
        signalRepository.saveSignal(new Signal(description, authorPhone, dateSubmitted, status, latitude, longitude), new SignalRepository.SaveSignalCallback() {
            @Override
            public void onSignalSaved(Signal signal) {
                if (!isViewAvailable()) return;
                if (!isEmpty(photoUri)) {
                    savePhoto(photoUri, signal);
                } else {
                    signalsList.add(signal);

                    getView().displaySignals(signalsList, true, signal.getId());
                    setSendSignalViewVisibility(false);
                    clearSignalViewData();
                }
            }

            @Override
            public void onSignalFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    private void savePhoto(final String photoUri, final Signal signal) {
        photoRepository.savePhoto(photoUri, signal.getId(), new PhotoRepository.SavePhotoCallback() {
            @Override
            public void onPhotoSaved() {
                if (!isViewAvailable()) return;
                signalsList.add(signal);
                getView().displaySignals(signalsList, true, signal.getId());
                setSendSignalViewVisibility(false);
                clearSignalViewData();
                getView().showAddedSignalMessage();
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
        getView().setThumbnailImage(photoUri);
    }

    @Override
    public void onStoragePermissionForCameraGranted() {
        getView().openCamera();
    }

    @Override
    public void onStoragePermissionForGalleryGranted() {
        getView().openGallery();
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

    @Override
    public void onRefreshButtonClicked() {
        getAllSignals(latitude, longitude, radius, timeout);
    }

    @Override
    public void onSignalStatusUpdated(Signal signal) {
        if (signal == null) return;

        replaceSignal(signal);
        getView().displaySignals(signalsList, true);
    }

    private void replaceSignal(Signal signal) {
        for (int i = 0; i < signalsList.size(); i++) {
            Signal currentSignal = signalsList.get(i);
            if (currentSignal.getId().equals(signal.getId())) {
                signalsList.remove(i);
                break;
            }
            signalsList.add(signal);
        }
    }

    @Override
    public void onAuthenticationAction() {
        getView().hideKeyboard();
        String userToken = userManager.getUserToken();

        if (userToken != null && !userToken.isEmpty()) {
            logoutUser();
        } else {
            getView().openLoginScreen();
        }
    }

    private void logoutUser() {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.logout(new UserManager.LogoutCallback() {
                @Override
                public void onLogoutSuccess() {
                    getView().onLogoutSuccess();
                }

                @Override
                public void onLogoutFailure(String message) {
                    getView().onLogoutFailure(message);
                }
            });
        } else {
            getView().onLogoutFailure("No connection.");
        }
    }

    @Override
    public void onLoginAction() {
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    private void clearSignalViewData() {
        getView().clearSignalViewData();
        photoUri = null;
    }

    private void setSendSignalViewVisibility(boolean visibility) {
        sendSignalViewVisibility = visibility;
        getView().setAddSignalViewVisibility(visibility);
    }

    private boolean isEmpty(String value) {
        return !(value != null && value.length() > 0);
    }
}
