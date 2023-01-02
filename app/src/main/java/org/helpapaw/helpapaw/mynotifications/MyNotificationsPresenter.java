package org.helpapaw.helpapaw.mynotifications;

import android.view.View;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.ReceivedNotificationsRepository;
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
    private ReceivedNotificationsRepository notificationsDatabase;

    MyNotificationsPresenter(MyNotificationsContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        notificationsDatabase = Injection.getReceivedNotificationsRepositoryInstance();

        notificationList = new ArrayList<>();
        signalsIds = new HashSet<>();
        mapSignalsToIds = new HashMap<>();
    }

    @Override
    public void onViewResume() {
        getNotificationsFromLocalDb();
    }

    @Override
    public void onDeleteMyNotificationsClicked() {
        getView().deleteMyNotifications();
    }

    @Override
    public void onDeleteMyNotifications() {
        Injection.getCrashLogger().log("Initiate delete notifications ");
        notificationsDatabase.deleteAll();
        notificationList = new ArrayList<>();
        mapSignalsToIds = new HashMap<>();

        getView().displayNotifications(notificationList, mapSignalsToIds);
        getView().onNoNotificationsToBeListed(true);
    }

    private void getNotificationsFromLocalDb() {
        if (Utils.hasNetworkConnection()) {
            notificationList = notificationsDatabase.getAll();

            if (notificationList.size() != 0) {
                getView().setProgressVisibility(View.VISIBLE);

                for (Notification notification : notificationList) {
                    signalsIds.add(notification.getSignalId());
                }

                signalRepository.getSignalsByListOfIds(signalsIds, new SignalRepository.LoadSignalsCallback() {
                    @Override
                    public void onSignalsLoaded(List<Signal> signals) {
                        if (!isViewAvailable()) return;

                        for (Signal signal : signals) {
                            mapSignalsToIds.put(signal.getId(), signal);
                        }
                        getView().displayNotifications(notificationList, mapSignalsToIds);
                        getView().setProgressVisibility(View.GONE);
                        getView().onNoNotificationsToBeListed(false);
                    }

                    @Override
                    public void onSignalsFailure(String message) {
                        if (!isViewAvailable()) return;
                        getView().setProgressVisibility(View.GONE);
                        getView().showMessage(message);
                    }
                });
            }
            else {
                getView().onNoNotificationsToBeListed(true);
            }
        } else {
            getView().showNoInternetMessage();
        }
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }
}
