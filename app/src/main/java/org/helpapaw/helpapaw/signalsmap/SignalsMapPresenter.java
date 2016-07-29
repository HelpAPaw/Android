package org.helpapaw.helpapaw.signalsmap;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.List;

/**
 * Created by iliyan on 7/28/16
 */
public class SignalsMapPresenter extends Presenter<SignalsMapContract.View> implements SignalsMapContract.UserActionsListener {

    private static final int DEFAULT_MAP_ZOOM = 13;
    private static final int DEFAULT_SEARCH_RADIUS = 50;

    private SignalRepository signalRepository;

    public SignalsMapPresenter(SignalsMapContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
    }

    @Override
    public void onLocationChanged(double latitude, double longitude) {
        getView().updateMapCameraPosition(latitude, longitude, DEFAULT_MAP_ZOOM);

        signalRepository.getAllSignals(latitude, longitude, DEFAULT_SEARCH_RADIUS,
                new SignalRepository.LoadSignalsCallback() {
                    @Override
                    public void onSignalsLoaded(List<Signal> signals) {
                        getView().displaySignals(signals);
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
