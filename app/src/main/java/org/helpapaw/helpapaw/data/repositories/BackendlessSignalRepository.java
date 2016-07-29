package org.helpapaw.helpapaw.data.repositories;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.geo.Units;

import org.helpapaw.helpapaw.data.models.SignalPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by iliyan on 7/28/16
 */
public class BackendlessSignalRepository implements SignalRepository {


    private static final String SIGNAL_TITLE = "title";
    private static final String SIGNAL_DATE_SUBMITTED = "dateSubmitted";
    private static final String SIGNAL_STATUS = "status";
    private static final String SIGNAL_AUTHOR = "author";

    @Override
    public void getAllSignals(double latitude, double longitude,
                              double radius, final LoadSignalsCallback callback) {
        BackendlessGeoQuery query =
                new BackendlessGeoQuery(latitude, longitude, radius, Units.KILOMETERS);
        query.setIncludeMeta(true);

        Backendless.Geo.getPoints(query, new AsyncCallback<BackendlessCollection<GeoPoint>>() {
            @Override
            public void handleResponse(BackendlessCollection<GeoPoint> response) {
                List<SignalPoint> signalPoints = new ArrayList<>();
                for (int i = 0; i < response.getCurrentPage().size(); i++) {
                    GeoPoint geoPoint = response.getCurrentPage().get(i);
                    signalPoints.add(new SignalPoint(geoPoint.getMetadata(SIGNAL_TITLE).toString(),
                            geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED).toString(),
                            Integer.parseInt(geoPoint.getMetadata(SIGNAL_STATUS).toString()),
                            geoPoint.getLatitude(), geoPoint.getLongitude(),
                            ((BackendlessUser) geoPoint.getMetadata(SIGNAL_AUTHOR)).getEmail()));
                }
                callback.onSignalsLoaded(signalPoints);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                callback.onSignalsFailure(fault.getMessage());
            }
        });
    }

    @Override
    public void saveSignal(SignalPoint signalPoint, final SaveSignalCallback callback) {

        Map<String, Object> meta = new HashMap<>();
        meta.put(SIGNAL_TITLE, signalPoint.getTitle());
        meta.put(SIGNAL_DATE_SUBMITTED, signalPoint.getDateSubmitted());
        meta.put(SIGNAL_STATUS, signalPoint.getStatus());

        Backendless.Geo.savePoint(signalPoint.getLatitude(),
                signalPoint.getLongitude(), meta, new AsyncCallback<GeoPoint>() {
                    @Override
                    public void handleResponse(GeoPoint geoPoint) {
                        callback.onSignalSaved();
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        callback.onSignalFailure(backendlessFault.getMessage());
                    }
                });
    }
}
