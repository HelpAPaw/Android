package org.helpapaw.helpapaw.data.repositories;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;
import java.util.Set;

/**
 * Created by iliyan on 7/28/16
 */
public interface SignalRepository {

    void getAllSignals(double latitude, double longitude, double radius, int timeout, LoadSignalsCallback callback);

    void getFilteredSignals(double latitude, double longitude, double radius, int timeout, boolean[] selection, LoadSignalsCallback callback);

    void getSignalsByOwnerId(String ownerId, LoadSignalsCallback callback);

    void getSignalsByListOfIds(Set<String> signalsIds, final LoadSignalsCallback callback);

    void getSignalsByListOfIdsExcludingCurrentUser(Set<String> signalsIds, final LoadSignalsCallback callback);

    void getSignal(String signalId, final LoadSignalsCallback callback);

    void saveSignal(Signal signal, SaveSignalCallback callback);

    void updateSignalStatus(String signalId, int status, List<Comment> currentComments, UpdateStatusCallback callback);

    void updateSignalTitle(String signalId, String title, UpdateTitleCallback callback);

    void markSignalsAsSeen(List<Signal> signals);

    void deleteSignal(String signalId, DeleteSignalCallback callback);


    interface LoadSignalsCallback {

        void onSignalsLoaded(List<Signal> signals);

        void onSignalsFailure(String message);
    }

    interface SaveSignalCallback {

        void onSignalSaved(Signal signal);

        void onSignalFailure(String message);
    }

    interface UpdateStatusCallback {

        void onStatusUpdated(int status);

        void onStatusFailure(String message);
    }

    interface UpdateTitleCallback {

        void onTitleUpdated(String title);

        void onTitleFailure(String message);
    }

    interface DeleteSignalCallback {

        void onSignalDeleted();

        void onSignalDeletedFailed(String message);
    }
}

