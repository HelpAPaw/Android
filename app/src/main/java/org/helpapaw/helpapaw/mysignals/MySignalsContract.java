package org.helpapaw.helpapaw.mysignals;

import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

public interface MySignalsContract {
    interface View {
        List<Signal> getMySignals();

        List<Signal> getCommentedSignals();

        void showRegistrationRequiredAlert();

        void openLoginScreen();
    }

    interface UserActionsListener {
        void onOpenMySignalsScreen();
    }
}
