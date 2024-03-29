package org.helpapaw.helpapaw.signalphoto;

import org.helpapaw.helpapaw.data.models.Signal;

/**
 * Created by milen on 05/03/18.
 *
 */

public interface SignalPhotoContract {
    interface View {

        void showSignalPhoto(String photoUrl);
    }

    interface UserActionsListener {

        void onInitPhotoScreen(String photoUrl);
    }
}
