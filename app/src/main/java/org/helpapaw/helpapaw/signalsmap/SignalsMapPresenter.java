package org.helpapaw.helpapaw.signalsmap;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.photo.UploadPhotoContract;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;


/**
 * Created by iliyan on 7/28/16
 */
public class SignalsMapPresenter extends Presenter<SignalsMapContract.View>
        implements SignalsMapContract.UserActionsListener, UploadPhotoContract.UserActionsListener {

    private UserManager userManager;
    private SignalRepository signalRepository;
    private PhotoRepository photoRepository;

    private double latitude;
    private double longitude;
    private int radius;
    private int timeout;

    static boolean[] selectedSignalTypes;

    private double currentMapLatitude;
    private double currentMapLongitude;

    private File photoFile;
    private boolean sendSignalViewVisibility;
    private boolean filterSignalViewVisibility;
    private List<Signal> signalsList;

    SignalsMapPresenter(SignalsMapContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        sendSignalViewVisibility = false;
        filterSignalViewVisibility = false;
        signalsList = new ArrayList<>();
    }

    @Override
    public void onInitSignalsMap(String focusedSignalId) {
        getView().setAddSignalViewVisibility(sendSignalViewVisibility);
        getView().setFilterSignalViewVisibility(filterSignalViewVisibility);
        if (photoFile != null) {
            getView().setThumbnailImage(photoFile);
        }
        if (focusedSignalId != null) {
            getSignal(focusedSignalId);
        }
        if (signalsList != null && signalsList.size() > 0) {
            getView().displaySignals(signalsList, false, selectedSignalTypes);
        }

        getUserPhone();
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
                                getView().displaySignals(signals, true, selectedSignalTypes);
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

    private void getFilteredSignals(double latitude, double longitude, int radius, int timeout) {
        if (Utils.getInstance().hasNetworkConnection()) {
            getView().setProgressVisibility(true);

            signalRepository.getFilteredSignals(latitude, longitude, radius, timeout, selectedSignalTypes,
                    new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            if (!isViewAvailable()) return;
                            signalsList = signals;

                            signalRepository.markSignalsAsSeen(signals);
                            getView().displaySignals(signals, false, selectedSignalTypes);
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

        setActiveFilterTextVisibility(isActiveFilter(selectedSignalTypes));
    }

    @Override
    public void onLocationChanged(double latitude, double longitude, int radius, int timeout) {
        currentMapLatitude = latitude;
        currentMapLongitude = longitude;

        if ((Utils.getInstance().getDistanceBetween(latitude, longitude, this.latitude, this.longitude) > 300)
            || (this.radius != radius)) {
            getFilteredSignals(latitude, longitude, radius, timeout);

            this.latitude = latitude;
            this.longitude = longitude;
            this.radius = radius;
            this.timeout = timeout;
        }
    }

    @Override
    public void onAddSignalClicked(boolean visibility) {
        if (!visibility) {
            userManager.isLoggedIn(new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess(String userId) {
                    if (!isViewAvailable()) return;

                    setSendSignalViewVisibility(true);
                }

                @Override
                public void onLoginFailure(String message) {
                    if (!isViewAvailable()) return;
                    getView().showRegistrationRequiredAlert();
                }
            });
        }
        else {
            setSendSignalViewVisibility(false);
        }
    }

    @Override
    public void onCancelAddSignal() {
        getView().displaySignals(signalsList, false, selectedSignalTypes);
    }

    @Override
    public void onSendSignalClicked(final String description, final String authorPhone, final int type) {
        getView().hideKeyboard();

        if (isEmpty(description)) {
            getView().showDescriptionErrorMessage();
        } else {
            getView().setSignalViewProgressVisibility(true);
            saveSignal(description, authorPhone, new Date(), 0, currentMapLatitude, currentMapLongitude, type);
        }
    }

    @Override
    public void onFilterSignalsClicked(boolean[] selectedTypes) {
        SignalsMapPresenter.selectedSignalTypes = selectedTypes;

        // todo consider adding selectedSignals as parameter - hidden dependency
        getFilteredSignals(latitude, longitude, radius, timeout);
        setFilterSignalViewVisibility(false);
    }

    private void saveSignal(String description, String authorPhone, Date dateSubmitted, int status,
                            final double latitude, final double longitude, int type) {
        FirebaseCrashlytics.getInstance().log("Initiate save new signal");
        signalRepository.saveSignal(new Signal(description, authorPhone, dateSubmitted, status, latitude, longitude, type), new SignalRepository.SaveSignalCallback() {
            @Override
            public void onSignalSaved(Signal signal) {
                if (!isViewAvailable()) return;
                if (photoFile != null) {
                    savePhoto(photoFile, signal);
                } else {
                    signalsList.add(signal);

                    getView().displaySignals(signalsList, true, signal.getId(), selectedSignalTypes);
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

    private void savePhoto(final File photoFile, final Signal signal) {
        photoRepository.savePhoto(photoFile, signal.getId(), new PhotoRepository.SavePhotoCallback() {
            @Override
            public void onPhotoSaved(String photoUrl) {
                if (!isViewAvailable()) return;
                signalsList.add(signal);
                getView().displaySignals(signalsList, true, signal.getId(), selectedSignalTypes);
                setSendSignalViewVisibility(false);
                clearSignalViewData();
                getView().showAddedSignalMessage();
            }

            @Override
            public void onPhotoFailure(String message) {
                if (!isViewAvailable()) return;
                signalsList.add(signal);
                getView().displaySignals(signalsList, true, signal.getId(), selectedSignalTypes);
                setSendSignalViewVisibility(false);
                clearSignalViewData();
                getView().showMessage(message);
            }
        });
    }

    @Override
    public void onChoosePhotoIconClicked() {
        getView().hideKeyboard();
        if (getView() instanceof UploadPhotoContract.View) {
            ((UploadPhotoContract.View)getView()).showSendPhotoBottomSheet(this);
        }
    }

    private void openCamera() {
        if (getView() instanceof UploadPhotoContract.View) {
            photoFile = ((UploadPhotoContract.View)getView()).openCamera();
        }
    }

    @Override
    public void onCameraOptionSelected() {
        openCamera();
    }

    @Override
    public void onStoragePermissionForCameraGranted() {
        openCamera();
    }

    private void openGallery() {
        if (getView() instanceof UploadPhotoContract.View) {
            ((UploadPhotoContract.View)getView()).openGallery();
        }
    }

    @Override
    public void onGalleryOptionSelected() {
        openGallery();
    }

    @Override
    public void onStoragePermissionForGalleryGranted() {
        openGallery();
    }

    @Override
    public void onSignalPhotoSelected(File photoFile) {
        if (photoFile != null) {
            this.photoFile = photoFile;
        }
        getView().setThumbnailImage(this.photoFile);
    }

    @Override
    public void onSignalInfoWindowClicked(Signal signal) {
        getView().openSignalDetailsScreen(signal);
    }

    @Override
    public void onBackButtonPressed() {
        if (sendSignalViewVisibility) {
            setSendSignalViewVisibility(false);
            setFilterSignalViewVisibility(false);
        }
        else if (filterSignalViewVisibility) {
            setFilterSignalViewVisibility(false);
        }
        else {
            getView().closeSignalsMapScreen();
        }
    }

    @Override
    public void onRefreshButtonClicked() {
        getFilteredSignals(latitude, longitude, radius, timeout);
    }

    @Override
    public void onFilterSignalsButtonClicked() {
        setFilterSignalViewVisibility(true);
    }

    @Override
    public void onSignalStatusUpdated(Signal signal) {
        if (signal == null) return;

        replaceSignal(signal);
        getView().displaySignals(signalsList, true, selectedSignalTypes);
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

    private void getUserPhone() {
        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess(String userId) {
                // Prefill author phone field with current user's phone (if available)
                userManager.getUserPhone(new UserManager.GetUserPropertyCallback() {
                    @Override
                    public void onSuccess(Object value) {
                        if (!isViewAvailable()) return;
                        getView().setAuthorPhone((String)value);
                    }

                    @Override
                    public void onFailure(String message) {
                        // Do nothing
                    }
                });
            }

            @Override
            public void onLoginFailure(String message) {
                // Do nothing
            }
        });
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    private void clearSignalViewData() {
        getView().clearSignalViewData();
        photoFile = null;
    }

    private void setSendSignalViewVisibility(boolean visibility) {
        sendSignalViewVisibility = visibility;
        getView().setAddSignalViewVisibility(visibility);
    }

    private void setFilterSignalViewVisibility(boolean visibility) {
        filterSignalViewVisibility = visibility;
        getView().setFilterSignalViewVisibility(visibility);
    }

    private void setActiveFilterTextVisibility(boolean visibility) {
        getView().setActiveFilterTextVisibility(visibility);
    }

    private boolean isEmpty(String value) {
        return !(value != null && value.length() > 0);
    }

    private boolean isActiveFilter(boolean[] selectedTypes) {
        boolean isActiveFilter = false;

        if (selectedTypes != null && !Utils.allSelected(selectedTypes)) {
            isActiveFilter = true;
        }

        return isActiveFilter;
    }
}
