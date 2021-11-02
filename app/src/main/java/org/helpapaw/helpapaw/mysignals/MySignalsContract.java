package org.helpapaw.helpapaw.mysignals;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

public interface MySignalsContract {
    interface View {

        void displaySubmittedSignals(List<Signal> signals);

        void displayCommentedSignals(List<Signal> signals);

        void showMessage(String message);

        void showNoInternetMessage();

        void setProgressVisibility(int visibility);
    }

    interface UserActionsListener {
        void onLoadMySignals();
    }
}
