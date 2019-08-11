package org.helpapaw.helpapaw.data.repositories;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public interface SignalRepository {

    void getAllSignals(double latitude, double longitude, double radius, int timeout, LoadSignalsCallback callback);

    void getSignal(String signalId, final LoadSignalsCallback callback);

    void saveSignal(Signal signal, SaveSignalCallback callback);

    void updateSignalStatus(String signalId, int status, UpdateStatusCallback callback);

    void markSignalsAsSeen(List<Signal> signals);


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
}

