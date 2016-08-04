package org.helpapaw.helpapaw.signaldetails;

import org.helpapaw.helpapaw.data.models.Signal;

/**
 * Created by iliyan on 7/25/16
 */
public interface SignalDetailsContract {

    interface View {

        void showMessage(String message);

        void setProgressIndicator(boolean active);

        void hideKeyboard();

        void showSignalDetails(Signal signal);
    }

    interface UserActionsListener {

        void onInitDetailsScreen(Signal signal);

    }
}
