package org.helpapaw.helpapaw.mynotifications;

import static org.helpapaw.helpapaw.base.PawApplication.getContext;

import android.view.View;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Notification;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.PushNotificationsRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.db.NotificationsDatabase;
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
    private NotificationsDatabase notificationsDatabase;

    MyNotificationsPresenter(MyNotificationsContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        notificationsDatabase = NotificationsDatabase.getDatabase(getContext());

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
        notificationsDatabase.notificationDao().deleteAll();
        notificationList = new ArrayList<>();
        mapSignalsToIds = new HashMap<>();

        getView().displayNotifications(notificationList, mapSignalsToIds);
        getView().onNoNotificationsToBeListed(true);
    }

    private void getNotificationsFromLocalDb() {
        if (Utils.getInstance().hasNetworkConnection()) {
            notificationList = notificationsDatabase.notificationDao().getAll();

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
                        getView().onNoNotificationsToBeListed(false);
                    }

                    @Override
                    public void onSignalsFailure(String message) {
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
}
