package org.helpapaw.helpapaw.data.repositories;

import org.helpapaw.helpapaw.data.models.SignalPoint;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public interface SignalRepository {

    void getAllSignals(double latitude, double longitude, double radius, LoadSignalsCallback callback);

    void saveSignal(SignalPoint signalPoint, SaveSignalCallback callback);

    interface LoadSignalsCallback {

        void onSignalsLoaded(List<SignalPoint> signalPoints);

        void onSignalsFailure(String message);
    }

    interface SaveSignalCallback {

        void onSignalSaved();

        void onSignalFailure(String message);
    }

}

