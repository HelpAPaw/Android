package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public interface SignalsMapContract {

    interface View {

        void showMessage(String message);

        void displaySignals(List<Signal> signals);

        void updateMapCameraPosition(double latitude, double longitude, int zoom);

        void showAddSignalView();
    }

    interface UserActionsListener {

        void onLocationChanged(double latitude, double longitude);

        void onAddSignalClicked();

    }
}
