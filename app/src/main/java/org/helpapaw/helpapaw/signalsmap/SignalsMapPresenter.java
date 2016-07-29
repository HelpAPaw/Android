package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.SignalPoint;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public class SignalsMapPresenter extends Presenter<SignalsMapContract.View> implements SignalsMapContract.UserActionsListener {

    private SignalRepository signalRepository;

    public SignalsMapPresenter(SignalsMapContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
    }

    @Override
    public void onLocationChanged(double latitude, double longitude) {
        getView().updateMapCameraPosition(latitude, longitude, 13);

        signalRepository.getAllSignals(latitude, longitude, 50, new SignalRepository.LoadSignalsCallback() {
            @Override
            public void onSignalsLoaded(List<SignalPoint> signalPoints) {
                getView().displaySignalPoints(signalPoints);
            }

            @Override
            public void onSignalsFailure(String message) {
                getView().showMessage(message);
            }
        });
    }

    @Override
    public void onAddSignalClicked() {
        getView().showAddSignalView();
    }
}
