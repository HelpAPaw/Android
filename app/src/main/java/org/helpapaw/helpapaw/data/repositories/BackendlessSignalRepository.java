package org.helpapaw.helpapaw.data.repositories;

import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.geo.BackendlessGeoQuery;
import com.backendless.geo.GeoPoint;
import com.backendless.geo.Units;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.db.SignalsDatabase;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.helpapaw.helpapaw.base.PawApplication.getContext;

/**
 * Created by iliyan on 7/28/16
 */
public class BackendlessSignalRepository implements SignalRepository {

    private SignalsDatabase signalsDatabase;

    private static final String SIGNAL_TITLE = "title";
    private static final String SIGNAL_DATE_SUBMITTED = "dateSubmitted";
    private static final String SIGNAL_STATUS = "status";
    private static final String SIGNAL_AUTHOR = "author";
    private static final String NAME_FIELD = "name";
    private static final String PHONE_FIELD = "phoneNumber";

    public BackendlessSignalRepository() {
        signalsDatabase = SignalsDatabase.getDatabase(getContext());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        SignalsDatabase.destroyInstance();
    }

    @Override
    public void getAllSignals(double latitude, double longitude, double radius, int timeout, final LoadSignalsCallback callback) {
        BackendlessGeoQuery query = new BackendlessGeoQuery(latitude, longitude, radius * 1000, Units.METERS);
        query.setIncludeMeta(true);

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -timeout);
        Date dateSubmitted = calendar.getTime();
        query.setWhereClause(String.format("dateSubmitted > %d", dateSubmitted.getTime()));

        String category = getCategory();
        if (category != null) {
            query.addCategory(category);
        }

        Backendless.Geo.getPoints(query, new AsyncCallback<List<GeoPoint>>() {
            @Override
            public void handleResponse(List<GeoPoint> response) {
                if (response == null) {
                    return;
                }

                List<Signal> signals = new ArrayList<>();
                for (int i = 0; i < response.size(); i++) {
                    GeoPoint geoPoint = response.get(i);

                    String signalTitle = getToStringOrNull(geoPoint.getMetadata(SIGNAL_TITLE));
                    String dateSubmittedString = getToStringOrNull(geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED));
                    String signalStatus = getToStringOrNull(geoPoint.getMetadata(SIGNAL_STATUS));

                    Date dateSubmitted = null;
                    try {
                        dateSubmitted = new Date(Long.valueOf(dateSubmittedString));
                    } catch (Exception ex) {
                        Log.d(BackendlessSignalRepository.class.getName(), "Failed to parse signal date.");
                    }

                    String signalAuthorName = null;
                    String signalAuthorPhone = null;

                    if ((geoPoint.getMetadata(SIGNAL_AUTHOR)) != null) {
                        signalAuthorName = getToStringOrNull(((BackendlessUser) geoPoint.getMetadata(SIGNAL_AUTHOR)).getProperty(NAME_FIELD));
                    }

                    if ((geoPoint.getMetadata(SIGNAL_AUTHOR)) != null) {
                        signalAuthorPhone = getToStringOrNull(((BackendlessUser) geoPoint.getMetadata(SIGNAL_AUTHOR)).getProperty(PHONE_FIELD));
                    }

                    Signal newSignal = new Signal(geoPoint.getObjectId(), signalTitle, dateSubmitted, Integer.parseInt(signalStatus),
                            signalAuthorName, signalAuthorPhone, geoPoint.getLatitude(), geoPoint.getLongitude(), false);

                    // If signal is already in DB - keep seen status
                    List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignal(geoPoint.getObjectId());
                    if (signalsFromDB.size() > 0) {
                        Signal signalFromDb = signalsFromDB.get(0);
                        newSignal.setSeen(signalFromDb.getSeen());
                    }
                    signalsDatabase.signalDao().saveSignal(newSignal);

                    signals.add(newSignal);
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
        meta.put(SIGNAL_DATE_SUBMITTED, signal.getDateSubmitted().getTime());
        meta.put(SIGNAL_STATUS, signal.getStatus());
        meta.put(SIGNAL_AUTHOR, Backendless.UserService.CurrentUser());

        List<String> categories = new ArrayList<>();
        String category = getCategory();
        if (category != null) {
            categories.add(category);
        }

        Backendless.Geo.savePoint(signal.getLatitude(), signal.getLongitude(), categories, meta, new AsyncCallback<GeoPoint>() {
            @Override
            public void handleResponse(GeoPoint geoPoint) {
                String signalTitle = getToStringOrNull(geoPoint.getMetadata(SIGNAL_TITLE));

                String dateSubmittedString = getToStringOrNull(geoPoint.getMetadata(SIGNAL_DATE_SUBMITTED));
                Date dateSubmitted = new Date(Long.valueOf(dateSubmittedString));
                String signalStatus = getToStringOrNull(geoPoint.getMetadata(SIGNAL_STATUS));

                String signalAuthorName = null;
                String signalAuthorPhone = null;

                if ((geoPoint.getMetadata(SIGNAL_AUTHOR)) != null) {
                    signalAuthorName = getToStringOrNull(((BackendlessUser) geoPoint.getMetadata(SIGNAL_AUTHOR)).getProperty(NAME_FIELD));
                }

                if ((geoPoint.getMetadata(SIGNAL_AUTHOR)) != null) {
                    signalAuthorPhone = getToStringOrNull(((BackendlessUser) geoPoint.getMetadata(SIGNAL_AUTHOR)).getProperty(PHONE_FIELD));
                }

                Signal savedSignal = new Signal(geoPoint.getObjectId(), signalTitle, dateSubmitted, Integer.parseInt(signalStatus),
                        signalAuthorName, signalAuthorPhone, geoPoint.getLatitude(), geoPoint.getLongitude(), true);
                signalsDatabase.signalDao().saveSignal(savedSignal);
                callback.onSignalSaved(savedSignal);

                // Push notification on successfully saved-signal
                Injection.getPushNotificationsInstance().pushNewSignalNotification("New signal", savedSignal.getTitle(), savedSignal.getId(),
                        savedSignal.getLatitude(), savedSignal.getLongitude());
            }

            @Override
            public void handleFault(BackendlessFault backendlessFault) {
                callback.onSignalFailure(backendlessFault.getMessage());
            }
        });
    }

    @Override
    public void updateSignalStatus(final String signalId, final int status, final UpdateStatusCallback callback) {
        String whereClause = "objectId = '" + signalId + "'";
        BackendlessGeoQuery geoQuery = new BackendlessGeoQuery();
        geoQuery.setWhereClause(whereClause);
        geoQuery.setIncludeMeta(true);

        String category = getCategory();
        if (category != null) {
            geoQuery.addCategory(category);
        }

        Backendless.Geo.getPoints(geoQuery, new AsyncCallback<List<GeoPoint>>() {
            @Override
            public void handleResponse(List<GeoPoint> response) {
                if (response.size() < 1) {
                    callback.onStatusFailure(PawApplication.getContext().getString(R.string.error_empty_signal_response));
                    return;
                }

                GeoPoint signalPoint = response.get(0);
                if (signalPoint != null) {
                    Map<String, Object> meta = signalPoint.getMetadata();
                    meta.put(SIGNAL_STATUS, status);

                    signalPoint.setMetadata(meta);
                    Backendless.Geo.savePoint(signalPoint, new AsyncCallback<GeoPoint>() {
                        @Override
                        public void handleResponse(GeoPoint geoPoint) {
                            String newSignalStatusString = getToStringOrNull(geoPoint.getMetadata(SIGNAL_STATUS));
                            Integer newSignalStatusInt = Integer.parseInt(newSignalStatusString);

                            // Update signal in database
                            List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignal(signalId);
                            if (signalsFromDB.size() > 0) {
                                Signal signal = signalsFromDB.get(0);
                                signal.setStatus(newSignalStatusInt);
                                signalsDatabase.signalDao().saveSignal(signal);
                            }

                            callback.onStatusUpdated(newSignalStatusInt);
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            callback.onStatusFailure(fault.getMessage());
                        }
                    });
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                callback.onStatusFailure(fault.getMessage());
            }
        });
    }

    public void markSignalsAsSeen(List<Signal> signals) {
        String[] signalIds = new String[signals.size()];
        for (int i = 0; i < signals.size(); i++) {
            Signal signal = signals.get(i);
            signalIds[i] = signal.getId();
        }

        List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignals(signalIds);
        for (Signal signal : signalsFromDB) {
            signal.setSeen(true);
            signalsDatabase.signalDao().saveSignal(signal);
        }
    }

    private String getCategory() {
        if (PawApplication.getIsTestEnvironment()) {
            return "Debug";
        } else {
            // Category should only be added if it's not Default
            return null;
        }
    }

    private String getToStringOrNull(Object object) {
        if (object != null) {
            return object.toString();
        } else {
            return null;
        }
    }
}
