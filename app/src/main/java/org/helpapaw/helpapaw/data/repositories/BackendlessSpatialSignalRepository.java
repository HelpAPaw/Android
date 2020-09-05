package org.helpapaw.helpapaw.data.repositories;

import android.annotation.SuppressLint;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.Point;

import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.db.SignalsDatabase;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

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
public class BackendlessSpatialSignalRepository implements SignalRepository {

    private SignalsDatabase signalsDatabase;

    private static final String SIGNALS_TABLE = "Signals";
    private static final String TEST_SIGNALS_TABLE = "SignalsTest";

    private static final String SIGNAL_TITLE = "title";
    private static final String SIGNAL_LOCATION = "location";
    private static final String SIGNAL_STATUS = "status";
    private static final String SIGNAL_AUTHOR = "author";
    private static final String SIGNAL_AUTHOR_PHONE = "authorPhone";
    private static final String NAME_FIELD = "name";
    private static final String OBJECT_ID_FIELD = "objectId";
    private static final String CREATED_FIELD = "created";

    public BackendlessSpatialSignalRepository() {
        signalsDatabase = SignalsDatabase.getDatabase(getContext());
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        SignalsDatabase.destroyInstance();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void getAllSignals(double latitude, double longitude, double radius, int timeout, final LoadSignalsCallback callback) {

        String whereClause1 = String.format("distanceOnSphere(location, '%s') <= %f", Utils.getWktPoint(longitude, latitude), radius * 1000);

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -timeout);
        Date dateSubmitted = calendar.getTime();
        String whereClause2 = String.format("%s > %d", CREATED_FIELD, dateSubmitted.getTime());

        String joinedWhereClause = String.format("(%s) AND (%s)", whereClause1, whereClause2);

        getSignals(joinedWhereClause, callback);
    }

    @Override
    public void getSignal(String signalId, final LoadSignalsCallback callback) {
        String whereClause = String.format("%s='%s'", OBJECT_ID_FIELD, signalId);
        getSignals(whereClause, callback);
    }

    private void getSignals(String whereClause, final LoadSignalsCallback callback) {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        Backendless.Data.of(getTableName()).find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response)
            {
                if (response == null) {
                    return;
                }

                List<Signal> signals = new ArrayList<>();
                for (int i = 0; i < response.size(); i++) {
                    HashMap signalMap = (HashMap) response.get(i);

                    String objectId = (String) signalMap.get(OBJECT_ID_FIELD);
                    String signalTitle = (String) signalMap.get(SIGNAL_TITLE);
                    Date dateCreated = (Date) signalMap.get(CREATED_FIELD);
                    Integer status = (Integer) signalMap.get(SIGNAL_STATUS);
                    String signalAuthorPhone = (String) signalMap.get(SIGNAL_AUTHOR_PHONE);
                    Point location = (Point) signalMap.get(SIGNAL_LOCATION);

                    String signalAuthorId = null;
                    String signalAuthorName  = null;

                    BackendlessUser signalAuthor = (BackendlessUser) signalMap.get(SIGNAL_AUTHOR);
                    if (signalAuthor != null) {
                        signalAuthorId = getToStringOrNull(signalAuthor.getProperty(OBJECT_ID_FIELD));
                        signalAuthorName = getToStringOrNull(signalAuthor.getProperty(NAME_FIELD));
                    }

                    Signal newSignal = new Signal(objectId, signalTitle, dateCreated, status, signalAuthorId, signalAuthorName, signalAuthorPhone, location.getLatitude(), location.getLongitude(), false);

                    // If signal is already in DB - keep seen status
                    List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignal(objectId);
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
            public void handleFault( BackendlessFault fault )
            {
                callback.onSignalsFailure(fault.getMessage());
            }
        });
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void saveSignal(Signal signal, final SaveSignalCallback callback) {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(SIGNAL_TITLE, signal.getTitle());
        dataMap.put(SIGNAL_LOCATION, Utils.getWktPoint(signal.getLongitude(), signal.getLatitude()));
        dataMap.put(SIGNAL_STATUS, signal.getStatus());
        dataMap.put(SIGNAL_AUTHOR_PHONE, signal.getAuthorPhone());

        Backendless.Data.of(getTableName()).save(dataMap, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map saveResponse) {
                HashMap<String, Object> parentObject = new HashMap<>();
                parentObject.put(OBJECT_ID_FIELD, saveResponse.get(OBJECT_ID_FIELD));

                BackendlessUser currentUser = Backendless.UserService.CurrentUser();
                HashMap<String, Object> childObject = new HashMap<>();
                childObject.put(OBJECT_ID_FIELD, currentUser.getObjectId());

                ArrayList<Map> children = new ArrayList<>();
                children.add(childObject);

                Backendless.Data.of(getTableName()).setRelation(parentObject, SIGNAL_AUTHOR, children, new AsyncCallback<Integer>() {
                            @Override
                            public void handleResponse(Integer response)
                            {
                                signal.setAuthorId(currentUser.getObjectId());
                                signal.setAuthorName(getToStringOrNull(currentUser.getProperty(NAME_FIELD)));
                                signal.setId((String) saveResponse.get(OBJECT_ID_FIELD));
                                Date dateCreated = (Date) saveResponse.get(CREATED_FIELD);
                                signal.setDateSubmitted(dateCreated);
                                signal.setSeen(true);

                                signalsDatabase.signalDao().saveSignal(signal);
                                callback.onSignalSaved(signal);

                                // Push notification on successfully saved-signal
                                Injection.getPushNotificationsRepositoryInstance().pushNewSignalNotification(signal, signal.getLatitude(), signal.getLongitude());
                            }

                            @Override
                            public void handleFault( BackendlessFault fault )
                            {
                                callback.onSignalFailure(fault.getMessage());
                            }
                        } );
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                callback.onSignalFailure(fault.getMessage());
            }
        });
    }

    @Override
    public void updateSignalStatus(final String signalId, final int status, final List<Comment> currentComments, final UpdateStatusCallback callback) {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(OBJECT_ID_FIELD, signalId);
        dataMap.put(SIGNAL_STATUS, status);

        Backendless.Data.of(getTableName()).save(dataMap, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map saveResponse) {
                // Update signal in database
                List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignal(signalId);
                if (signalsFromDB.size() > 0) {
                    Signal signal = signalsFromDB.get(0);
                    signal.setStatus(status);
                    signalsDatabase.signalDao().saveSignal(signal);

                    Injection.getPushNotificationsRepositoryInstance().pushNewStatusNotification(signal, status, currentComments);
                }

                callback.onStatusUpdated(status);
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

    private String getTableName() {
        if (PawApplication.getIsTestEnvironment()) {
            return TEST_SIGNALS_TABLE;
        } else {
            return SIGNALS_TABLE;
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
