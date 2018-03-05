package org.helpapaw.helpapaw.signalphoto;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.utils.Injection;

/**
 * Created by milen on 05/03/18.
 * The Presenter for showing a signal's photo on full screen
 */

public class SignalPhotoPresenter extends Presenter<SignalPhotoContract.View> implements SignalPhotoContract.UserActionsListener {

    private PhotoRepository photoRepository;

    SignalPhotoPresenter(SignalPhotoContract.View view) {
        super(view);
        photoRepository = Injection.getPhotoRepositoryInstance();
    }

    @Override
    public void onInitPhotoScreen(Signal signal) {
        if (signal != null) {
            signal.setPhotoUrl(photoRepository.getPhotoUrl(signal.getId()));
            getView().showSignalPhoto(signal);
        }
    }
}
