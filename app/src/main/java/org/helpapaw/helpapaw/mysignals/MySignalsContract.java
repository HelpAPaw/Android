package org.helpapaw.helpapaw.mysignals;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

public interface MySignalsContract {
    interface View {
        void showRegistrationRequiredAlert();

        void openLoginScreen();

        void displaySubmittedSignals(List<Signal> signals);

        void displayCommentedSignals(List<Signal> signals);

        void showMessage(String message);

        void showNoInternetMessage();

        void setProgressVisibility(int visibility);
    }

    interface UserActionsListener {
        void onOpenMySignalsScreen();

        void onLoadMySignals();
    }
}
