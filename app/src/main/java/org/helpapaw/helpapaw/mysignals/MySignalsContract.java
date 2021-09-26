package org.helpapaw.helpapaw.mysignals;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

public interface MySignalsContract {
    interface View {
        void showRegistrationRequiredAlert();

        void openLoginScreen();

        void displaySignals(List<Signal> signals);
    }

    interface UserActionsListener {
        void onOpenMySignalsScreen();
    }
}
