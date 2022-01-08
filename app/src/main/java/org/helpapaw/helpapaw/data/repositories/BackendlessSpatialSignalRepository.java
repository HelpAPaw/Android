package org.helpapaw.helpapaw.data.repositories;

import static org.helpapaw.helpapaw.base.PawApplication.getContext;

import android.annotation.SuppressLint;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.Point;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Created by iliyan on 7/28/16
 */
public class BackendlessSpatialSignalRepository implements SignalRepository {

    private SignalsDatabase signalsDatabase;
    private PhotoRepository photoRepository;

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
    private static final String SIGNAL_TYPE = "signalType";
    private static final String DELETED = "isDeleted";
    private static final String OWNER_ID = "ownerId";

    private static final String WHERE_CLAUSE_NOT_DELETED = String.format(Locale.ENGLISH, "%s = %s", DELETED, "FALSE");

    private static final String SORT_DATE_CREATED_DESCENDING = "created DESC";

    private static final int PAGE_SIZE = 100;

    public BackendlessSpatialSignalRepository() {
        signalsDatabase = SignalsDatabase.getDatabase(getContext());
        photoRepository = Injection.getPhotoRepositoryInstance();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();

        SignalsDatabase.destroyInstance();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void getAllSignals(double latitude, double longitude, double radius, int timeout, final LoadSignalsCallback callback) {

        String whereClause1 = String.format(Locale.ENGLISH, "distanceOnSphere(location, '%s') <= %f", Utils.getWktPoint(longitude, latitude), radius * 1000);

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -timeout);
        Date dateSubmitted = calendar.getTime();
        String whereClause2 = String.format(Locale.ENGLISH, "%s > %d", CREATED_FIELD, dateSubmitted.getTime());

        String joinedWhereClause = String.format(Locale.ENGLISH, "(%s) AND (%s) AND (%s)", whereClause1, whereClause2, WHERE_CLAUSE_NOT_DELETED);

        getSignals(joinedWhereClause, callback);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void getFilteredSignals(double latitude, double longitude, double radius, int timeout,
                                   boolean[] selectedTypes, final LoadSignalsCallback callback) {

        String whereClause1 = String.format(Locale.ENGLISH, "distanceOnSphere(location, '%s') <= %f", Utils.getWktPoint(longitude, latitude), radius * 1000);

        final Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -timeout);
        Date dateSubmitted = calendar.getTime();
        String whereClause2 = String.format(Locale.ENGLISH, "%s > %d", CREATED_FIELD, dateSubmitted.getTime());
        String joinedWhereClause;

        if (selectedTypes != null && !Utils.allSelected(selectedTypes)) {
            String whereClause4 = createWhereClauseForType(selectedTypes);

            joinedWhereClause= String.format(Locale.ENGLISH, "(%s) AND (%s) AND (%s) AND (%s)",
                    whereClause1, whereClause2, WHERE_CLAUSE_NOT_DELETED, whereClause4);
        } else {
            joinedWhereClause = String.format(Locale.ENGLISH, "(%s) AND (%s) AND (%s)",
                    whereClause1, whereClause2, WHERE_CLAUSE_NOT_DELETED);
        }

        getSignals(joinedWhereClause, callback);
    }

    private String createWhereClauseForType(boolean[] selection) {
        StringBuilder whereClause3 = new StringBuilder();
        List<String> selected = new ArrayList<>();

        if (Utils.noneSelected(selection)) {
            return SIGNAL_TYPE + " = -1";
        }

        for (int i = 0; i < selection.length; ++i) {
            if (selection[i]) {
                selected.add(SIGNAL_TYPE + " = " + i);
            }
        }

        for (int i = 0; i < selected.size(); ++i) {
            if (i == selected.size() - 1) {
                whereClause3.append(selected.get(i));
            } else {
                whereClause3.append(selected.get(i) + " OR ");
            }
        }

        return whereClause3.toString();
    }

    @Override
    public void getSignalsByOwnerId(String ownerId, LoadSignalsCallback callback) {
        String whereClause = String.format(Locale.ENGLISH, "%s = '%s'", OWNER_ID, ownerId);

        getSignals(whereClause, callback);
    }

    @Override
    public void getSignal(String signalId, final LoadSignalsCallback callback) {
        String whereClause = String.format(Locale.ENGLISH, "%s='%s'", OBJECT_ID_FIELD, signalId);
        getSignals(whereClause, callback);
    }

    @Override
    public void getSignalsByListOfIds(Set<String> signalsIds, final LoadSignalsCallback callback) {
        getSignals(whereClauseSignalsIds(signalsIds), callback);
    }

    @Override
    public void getSignalsByListOfIdsExcludingCurrentUser(Set<String> signalsIds, final LoadSignalsCallback callback) {
        String whereClause = buildWhereClauseForListOfSignalsIdsExcludingCurrentUser(signalsIds);
        getSignals(whereClause, callback);
    }

    @NonNull
    private String buildWhereClauseForListOfSignalsIdsExcludingCurrentUser(Set<String> signalsIds) {
        String joinedWhereClause = String.format(Locale.ENGLISH, "(%s) AND (%s)",
                whereClauseExcludeCurrentUser(), whereClauseSignalsIds(signalsIds));

        return joinedWhereClause;
    }

    private String whereClauseSignalsIds(Set<String> signalsIds) {
        String whereClause = OBJECT_ID_FIELD + " = '";
        String delimiter ="' OR " + whereClause;
        if (signalsIds != null && signalsIds.size() > 0) {
            whereClause = whereClause + TextUtils.join(delimiter, signalsIds) + "'";
        }

        return whereClause;
    }

    private String whereClauseExcludeCurrentUser() {
        BackendlessUser currentUser = Backendless.UserService.CurrentUser();

        String where = OWNER_ID + " != '" + currentUser.getUserId() + "'";
        return where;
    }

    private void getSignals(String whereClause, final LoadSignalsCallback callback) {
        getSignals(whereClause, 0, callback);
    }

    private void getSignals(String whereClause, int offset, final LoadSignalsCallback callback) {
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setPageSize(PAGE_SIZE);
        queryBuilder.setOffset(offset);
        queryBuilder.setSortBy(SORT_DATE_CREATED_DESCENDING);
        Backendless.Data.of(getTableName()).find(queryBuilder, new AsyncCallback<List<Map>>() {
            @Override
            public void handleResponse(List<Map> response)
            {
                if (response == null) {
                    return;
                }

                List<Signal> signals = new ArrayList<>();
                for (int i = 0; i < response.size(); i++) {
                    try {
                        HashMap signalMap = (HashMap) response.get(i);

                        String objectId = (String) signalMap.get(OBJECT_ID_FIELD);
                        FirebaseCrashlytics.getInstance().log("Got signal with Id: " + objectId);
                        String signalTitle = (String) signalMap.get(SIGNAL_TITLE);
                        Date dateCreated = (Date) signalMap.get(CREATED_FIELD);
                        Integer status = (Integer) signalMap.get(SIGNAL_STATUS);
                        String signalAuthorPhone = (String) signalMap.get(SIGNAL_AUTHOR_PHONE);
                        Point location = (Point) signalMap.get(SIGNAL_LOCATION);
                        Boolean deleted = (Boolean) signalMap.get(DELETED);

                        Integer type = 0;
                        if (signalMap.get(SIGNAL_TYPE) != null) {
                            type = (Integer) signalMap.get(SIGNAL_TYPE);
                        }

                        String signalAuthorId = null;
                        String signalAuthorName = null;

                        BackendlessUser signalAuthor = (BackendlessUser) signalMap.get(SIGNAL_AUTHOR);
                        if (signalAuthor != null) {
                            signalAuthorId = getToStringOrNull(signalAuthor.getProperty(OBJECT_ID_FIELD));
                            signalAuthorName = getToStringOrNull(signalAuthor.getProperty(NAME_FIELD));
                        }

                        Signal newSignal = new Signal(objectId, signalTitle, dateCreated, status,
                                signalAuthorId, signalAuthorName, signalAuthorPhone, location.getLatitude(),
                                location.getLongitude(), false, type);
                        newSignal.setIsDeleted(deleted);
                        newSignal.setPhotoUrl(photoRepository.getSignalPhotoUrl(newSignal.getId()));

                        // If signal is already in DB - keep seen status
                        List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignal(objectId);
                        if (signalsFromDB.size() > 0) {
                            Signal signalFromDb = signalsFromDB.get(0);
                            signalFromDb.setPhotoUrl(newSignal.getPhotoUrl());
                            newSignal.setSeen(signalFromDb.getSeen());
                        }
                        signalsDatabase.signalDao().saveSignal(newSignal);

                        signals.add(newSignal);
                    } catch (Exception ex) {
                        FirebaseCrashlytics.getInstance().recordException(ex);
                    }
                }

                if (response.size() == PAGE_SIZE) {
                    getSignals(whereClause, offset + PAGE_SIZE, new LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> nextPageSignals) {
                            signals.addAll(nextPageSignals);
                            callback.onSignalsLoaded(signals);
                        }

                        @Override
                        public void onSignalsFailure(String message) {
                            callback.onSignalsFailure(message);
                        }
                    });
                }
                else {
                    callback.onSignalsLoaded(signals);
                }
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
        dataMap.put(SIGNAL_TYPE, signal.getType());

        Backendless.Data.of(getTableName()).save(dataMap, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map saveResponse) {
                HashMap<String, Object> parentObject = new HashMap<>();
                parentObject.put(OBJECT_ID_FIELD, saveResponse.get(OBJECT_ID_FIELD));

                BackendlessUser currentUser = Backendless.UserService.CurrentUser();
                if (currentUser == null) {
                    // currentUser should not be null but indeed here it is
                    // Cannot set the relation so just return
                    return;
                }
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
                                Injection.getPushNotificationsRepositoryInstance().
                                        pushNewSignalNotification(signal);
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

    @Override
    public void updateSignalTitle(final String signalId, final String title, final UpdateTitleCallback callback) {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(OBJECT_ID_FIELD, signalId);
        dataMap.put(SIGNAL_TITLE, title);

        Backendless.Data.of(getTableName()).save(dataMap, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map saveResponse) {
                // Update signal in database
                List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignal(signalId);
                if (signalsFromDB.size() > 0) {
                    Signal signal = signalsFromDB.get(0);
                    signal.setTitle(title);
                    signalsDatabase.signalDao().saveSignal(signal);
                }

                callback.onTitleUpdated(title);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                callback.onTitleFailure(fault.getMessage());
            }
        });
    }

    @Override
    public void deleteSignal(final String signalId, final DeleteSignalCallback callback) {

        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put(OBJECT_ID_FIELD, signalId);
        dataMap.put(DELETED, true);

        Backendless.Data.of(getTableName()).save(dataMap, new AsyncCallback<Map>() {
            @Override
            public void handleResponse(Map saveResponse) {
                // Update signal in database
                List<Signal> signalsFromDB = signalsDatabase.signalDao().getSignal(signalId);
                if (signalsFromDB.size() > 0) {
                    Signal signal = signalsFromDB.get(0);
                    signal.setIsDeleted(true);
                    signalsDatabase.signalDao().saveSignal(signal);
                }

                callback.onSignalDeleted();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                callback.onSignalDeletedFailed(fault.getMessage());
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
