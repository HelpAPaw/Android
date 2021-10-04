package org.helpapaw.helpapaw.mynotifications;

import android.view.View;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.PushNotificationsRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MyNotificationsPresenter extends Presenter<MyNotificationsContract.View> implements MyNotificationsContract.UserActionsListener {

    private List<Notification> notificationList;
    private Set<String> signalsIds;
    private Map<String, Signal> mapSignalsToIds;

    private SignalRepository signalRepository;
    private PhotoRepository photoRepository;
    private PushNotificationsRepository notificationsRepository;

    MyNotificationsPresenter(MyNotificationsContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        notificationsRepository = Injection.getPushNotificationsRepositoryInstance();

        notificationList = new ArrayList<>();
        signalsIds = new HashSet<>();
        mapSignalsToIds = new HashMap<>();
    }

    @Override
    public void onOpenMyNotificationsScreen() {
        getNotificationsFromLocalDb();
    }

    @Override
    public void onDeleteMyNotificationsClicked() {
        getView().deleteMyNotifications();
    }

    @Override
    public void onDeleteMyNotifications() {
        FirebaseCrashlytics.getInstance().log("Initiate delete notifications ");
        notificationsRepository.deleteNotifications();
        notificationList = new ArrayList<>();
        mapSignalsToIds = new HashMap<>();

        getView().displayNotifications(notificationList, mapSignalsToIds);
    }

    private void getNotificationsFromLocalDb() {
        if (Utils.getInstance().hasNetworkConnection()) {
            notificationList = notificationsRepository.getAllNotifications();

            if (notificationList.size() != 0) {
                getView().setProgressVisibility(View.VISIBLE);

                for (Notification notification : notificationList) {
                    signalsIds.add(notification.getSignalId());
                }

                signalRepository.getSignalsByListOfIds(signalsIds, new SignalRepository.LoadSignalsCallback() {
                    @Override
                    public void onSignalsLoaded(List<Signal> signals) {
                        for (Signal signal : signals) {
                            signal.setPhotoUrl(photoRepository.getSignalPhotoUrl(signal.getId()));
                            mapSignalsToIds.put(signal.getId(), signal);
                        }
                        getView().displayNotifications(notificationList, mapSignalsToIds);
                        getView().setProgressVisibility(View.GONE);
                    }

                    @Override
                    public void onSignalsFailure(String message) {
                        getView().showMessage(message);
                    }
                });
            }
        } else {
            getView().showNoInternetMessage();
        }
    }
}
