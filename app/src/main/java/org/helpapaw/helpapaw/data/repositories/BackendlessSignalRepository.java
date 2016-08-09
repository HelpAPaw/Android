package org.helpapaw.helpapaw.data.repositories;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.geo.Units;

import org.helpapaw.helpapaw.data.models.Signal;

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
    private static final String NAME_FIELD = "name";


    @Override
    public void getSignalById(String signalId, final LoadSignalCallback callback) {
        String whereClause = "objectId = '" + signalId + "'";
        BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
        geoQuery.setWhereClause(whereClause);

        Backendless.Geo.getPoints(geoQuery, new AsyncCallback<BackendlessCollection<GeoPoint>>() {
            @Override
            public void handleResponse(BackendlessCollection<GeoPoint> response) {
                GeoPoint geoPoint = response.getData().get(0);
                if (geoPoint != null) {
                    Signal signal = new Signal(geoPoint.getObjectId(), geoPoint.getMetadata(SIGNAL_TITLE).toString(),
                            geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED).toString(),
                            Integer.parseInt(geoPoint.getMetadata(SIGNAL_STATUS).toString()),
                            ((BackendlessUser) geoPoint.getMetadata(SIGNAL_AUTHOR)).getProperty(NAME_FIELD).toString(),
                            geoPoint.getLatitude(), geoPoint.getLongitude());

                    callback.onSignalLoaded(signal);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                callback.onSignalsFailure(fault.getMessage());
            }
        });
    }

    @Override
    public void getAllSignals(double latitude, double longitude,
                              double radius, final LoadSignalsCallback callback) {
        BackendlessGeoQuery query =
                new BackendlessGeoQuery(latitude, longitude, radius, Units.KILOMETERS);
        query.setIncludeMeta(true);

        Backendless.Geo.getPoints(query, new AsyncCallback<BackendlessCollection<GeoPoint>>() {
            @Override
            public void handleResponse(BackendlessCollection<GeoPoint> response) {
                List<Signal> signals = new ArrayList<>();
                for (int i = 0; i < response.getData().size(); i++) {
                    GeoPoint geoPoint = response.getData().get(i);
                    signals.add(new Signal(geoPoint.getObjectId(), geoPoint.getMetadata(SIGNAL_TITLE).toString(),
                            geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED).toString(),
                            Integer.parseInt(geoPoint.getMetadata(SIGNAL_STATUS).toString()),
                            ((BackendlessUser) geoPoint.getMetadata(SIGNAL_AUTHOR)).getProperty(NAME_FIELD).toString(),
                            geoPoint.getLatitude(), geoPoint.getLongitude()));
                }
                callback.onSignalsLoaded(signals);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                callback.onSignalsFailure(fault.getMessage());
            }
        });
    }

    @Override
    public void saveSignal(Signal signal, final SaveSignalCallback callback) {

        Map<String, Object> meta = new HashMap<>();
        meta.put(SIGNAL_TITLE, signal.getTitle());
        meta.put(SIGNAL_DATE_SUBMITTED, signal.getDateSubmitted());
        meta.put(SIGNAL_STATUS, signal.getStatus());
        meta.put(SIGNAL_AUTHOR, Backendless.UserService.CurrentUser());

        Backendless.Geo.savePoint(signal.getLatitude(),
                signal.getLongitude(), meta, new AsyncCallback<GeoPoint>() {
                    @Override
                    public void handleResponse(GeoPoint geoPoint) {
                        Signal savedSignal = new Signal(geoPoint.getObjectId(), geoPoint.getMetadata(SIGNAL_TITLE).toString(),
                                geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED).toString(),
                                Integer.parseInt(geoPoint.getMetadata(SIGNAL_STATUS).toString()),
                                ((BackendlessUser) geoPoint.getMetadata(SIGNAL_AUTHOR)).getProperty(NAME_FIELD).toString(),
                                geoPoint.getLatitude(), geoPoint.getLongitude());

                        callback.onSignalSaved(savedSignal);
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        callback.onSignalFailure(backendlessFault.getMessage());
                    }
                });
    }
}
