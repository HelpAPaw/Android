package org.helpapaw.helpapaw.signalsmap;

import com.evernote.android.job.JobManager;

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

    private static final float DEFAULT_MAP_ZOOM = 14.5f;
    public static final int DEFAULT_SEARCH_RADIUS = 4000;
    private static final String DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss";

    private UserManager userManager;
    private SignalRepository signalRepository;
    private PhotoRepository photoRepository;

    private double latitude;
    private double longitude;

    private String photoUri;
    private boolean sendSignalViewVisibility;
    private List<Signal> signalsList;

    private JobManager mJobManager;
    private static final String LAST_JOB_ID = "LAST_JOB_ID";
    private static final boolean REQUIRES_CHARGING = false;
    private static final boolean REQUIRES_NETWORK = false;
    private static final boolean REQUIRES_IDLE = false;

    private int mLastJobId ;

    SignalsMapPresenter(SignalsMapContract.View view) {
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
            getView().displaySignals(signalsList, false);
        }
    }

    private void getAllSignals(double latitude, double longitude, final boolean showPopup) {
        if (Utils.getInstance().hasNetworkConnection()) {
            getView().setProgressVisibility(true);
            signalRepository.getAllSignals(latitude, longitude, DEFAULT_SEARCH_RADIUS,
                    new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            if (!isViewAvailable()) return;
                            signalsList = signals;
                            getView().displaySignals(signals, showPopup);
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

    public static void getAllSignalsWithoutViewUpate(){

    }

    @Override
    public void onLocationChanged(double latitude, double longitude) {

        if (this.latitude != 0 && this.longitude != 0) {
            if (Utils.getInstance().getDistanceBetween(latitude, longitude, this.latitude, this.longitude) > 300) {
                getAllSignals(latitude, longitude, false);
            }
        } else {
            getAllSignals(latitude, longitude, false);

            getView().updateMapCameraPosition(latitude, longitude, DEFAULT_MAP_ZOOM);
        }

        this.latitude = latitude;
        this.longitude = longitude;


    }

    @Override
    public void onAddSignalClicked(boolean visibility) {
        setSendSignalViewVisibility(!visibility);
        if(!visibility){
            getView().addMapMarker(latitude,longitude);
        }else{
            getView().displaySignals(signalsList,false);
            //onRefreshButtonClicked();
        }
    }

    @Override
    public void onMarkerMoved(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    public void onCancelAddSignal() {
       getView().displaySignals(signalsList,false);
    }

    @Override
    public void onSendSignalClicked(final String description) {
        getView().hideKeyboard();
        getView().setSignalViewProgressVisibility(true);

        userManager.isLoggedIn(new UserManager.LoginCallback() {
            @Override
            public void onLoginSuccess() {
                if (!isViewAvailable()) return;

                if (isEmpty(description)) {
                    getView().showDescriptionErrorMessage();
                } else {
                    saveSignal(description, new Date(), 0, latitude, longitude);
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



    private void saveSignal(String description, Date dateSubmitted, int status,
                            final double latitude, final double longitude) {
        signalRepository.saveSignal(new Signal(description, dateSubmitted, status, latitude, longitude), new SignalRepository.SaveSignalCallback() {
            @Override
            public void onSignalSaved(Signal signal) {
                if (!isViewAvailable()) return;
                if (!isEmpty(photoUri)) {
                    savePhoto(photoUri, signal);
                } else {
                    signalsList.add(signal);
                    getView().displaySignals(signalsList, true);
                    getView().setAddSignalViewVisibility(false);
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
                getView().displaySignals(signalsList, true);
                getView().setAddSignalViewVisibility(false);
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
        getAllSignals(latitude, longitude, false);
    }

    @Override
    public void onSignalStatusUpdated(Signal signal) {
        for (int i = 0; i < signalsList.size(); i++) {
            Signal currentSignal = signalsList.get(i);
            if(currentSignal.getId().equals(signal.getId())){
                signalsList.remove(i);
                signalsList.add(signal);
                getView().displaySignals(signalsList, true);
                break;
            }
        }
    }

    @Override
    public void onFilterEmergency() {
        List<Signal> emergencyList = new ArrayList<>();
        for (int i = 0; i < signalsList.size(); i++) {
            Signal currentSignal = signalsList.get(i);

            if(currentSignal.getStatus() == 0){
                emergencyList.add(currentSignal);
            }
        }
        getView().displaySignals(emergencyList, true);
    }

    @Override
    public void onFilterInProgress() {
        List<Signal> emergencyList = new ArrayList<>();
        for (int i = 0; i < signalsList.size(); i++) {
            Signal currentSignal = signalsList.get(i);

            if(currentSignal.getStatus() == 1){
                emergencyList.add(currentSignal);

            }
        }
        getView().displaySignals(emergencyList, true);
    }

    @Override
    public void onFilterSolved() {
        List<Signal> emergencyList = new ArrayList<>();
        for (int i = 0; i < signalsList.size(); i++) {
            Signal currentSignal = signalsList.get(i);

            if(currentSignal.getStatus() == 2){
                emergencyList.add(currentSignal);

            }
        }
        getView().displaySignals(emergencyList, true);
    }

    @Override
    public void onAuthenticationAction() {
        getView().hideKeyboard();
        String userToken = userManager.getUserToken();

        if(userToken!=null && !userToken.isEmpty()){
            logoutUser();
        }else{
            getView().openLoginScreen();
        }
    }

    private void logoutUser(){
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
        }else{
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
