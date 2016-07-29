package org.helpapaw.helpapaw.data.repositories;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public interface SignalRepository {

    void getSignalById(String signalId, LoadSignalCallback callback);

    void getAllSignals(double latitude, double longitude, double radius, LoadSignalsCallback callback);

    void saveSignal(Signal signal, SaveSignalCallback callback);


    interface LoadSignalCallback {

        void onSignalLoaded(Signal signal);

        void onSignalsFailure(String message);
    }

    interface LoadSignalsCallback {

        void onSignalsLoaded(List<Signal> signals);

        void onSignalsFailure(String message);
    }

    interface SaveSignalCallback {

        void onSignalSaved(String signalId);

        void onSignalFailure(String message);
    }

}

