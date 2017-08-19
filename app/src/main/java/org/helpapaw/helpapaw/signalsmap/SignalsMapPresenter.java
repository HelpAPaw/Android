package org.helpapaw.helpapaw.signalsmap;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;

import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.JobApi;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import net.vrallev.android.cat.Cat;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;
import org.helpapaw.helpapaw.utils.backgroundscheduler.SignalsSyncJob;
import org.helpapaw.helpapaw.utils.services.JobSchedulerService;
import org.helpapaw.helpapaw.utils.services.WakeupAlarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.helpapaw.helpapaw.utils.services.JobSchedulerService.JOB_ID;
import static org.helpapaw.helpapaw.utils.services.JobSchedulerService.TIME_INTERVAL;

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


    @Override
    public void onSetupSchedulerService(Context context) {
        mJobManager = JobManager.instance();
        testAllImpl(context);
//        if(Build.VERSION.SDK_INT>=21) {
//            setupScheduler(context);
//        }else{
//            setupAlarmManager(context);
//        }

    }

    private void setupAlarmManager(Context context){
        WakeupAlarm wakeupAlarm = new WakeupAlarm();
        wakeupAlarm.cancelAlarm(context);
        wakeupAlarm.setAlarm(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void setupScheduler(Context context){
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ComponentName serviceName = new ComponentName(context, JobSchedulerService.class);
//
        JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,serviceName);
//        builder.setRequiresDeviceIdle(true);
        builder.setPersisted(true);
//       // builder.setPeriodic(TIME_INTERVAL);
        builder.setPeriodic(TIME_INTERVAL);
        jobScheduler.cancelAll();
//
        jobScheduler.schedule(builder.build());

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCancelSchedulerService(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(JOB_ID);
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


    private void testSimple() {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString("key", "Hello world");

        mLastJobId = new JobRequest.Builder(SignalsSyncJob.TAG)
                .setExecutionWindow(3_000L, 4_000L)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.LINEAR)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiredNetworkType(JobRequest.NetworkType.values()[0])
                .setExtras(extras)
                .setRequirementsEnforced(true)
                .setPersisted(true)
                .build()
                .schedule();
    }

    private void testAllImpl(Context context) {
        JobApi currentApi = mJobManager.getApi();

        for (JobApi api : JobApi.values()) {
            if (api.isSupported(context)) {
                mJobManager.forceApi(api);
                testCancelAll();
//                testExact();
               // testSimple();
                testPeriodic();
            } else {
                Cat.w("%s is not supported", api);
            }
        }

        mJobManager.forceApi(currentApi);
    }

    private void testPeriodic() {
        mLastJobId = new JobRequest.Builder(SignalsSyncJob.TAG)
                .setPeriodic(JobRequest.MIN_INTERVAL, JobRequest.MIN_FLEX)
                .setRequiresCharging(REQUIRES_CHARGING)
                .setRequiresDeviceIdle(REQUIRES_IDLE)
                .setRequiredNetworkType(JobRequest.NetworkType.values()[0])
                .setPersisted(true)
                .build()
                .schedule();
    }

    private void testExact() {
        PersistableBundleCompat extras = new PersistableBundleCompat();
        extras.putString("key", "Hello world");

        mLastJobId = new JobRequest.Builder(SignalsSyncJob.TAG)
                .setBackoffCriteria(5_000L, JobRequest.BackoffPolicy.EXPONENTIAL)
                .setExtras(extras)
                .setExact(20_000L)
                .setPersisted(true)
                .setUpdateCurrent(true)
                .build()
                .schedule();
    }

    private void testCancelLast() {
        mJobManager.cancel(mLastJobId);
    }

    private void testCancelAll() {
        mJobManager.cancelAll();
    }
}
