package org.helpapaw.helpapaw.mysignals;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

public interface MySignalsContract {
    interface View {

        void displaySignals(List<Signal> signals);

        void showMessage(String message);

        void showNoInternetMessage();

        void setProgressVisibility(int visibility);

        void onNoSignalsToBeListed(boolean zeroSignals);
    }

    interface UserActionsListener {
        void onLoadMySignals();
    }
}
