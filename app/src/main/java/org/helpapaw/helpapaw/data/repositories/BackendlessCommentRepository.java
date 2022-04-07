package org.helpapaw.helpapaw.data.repositories;

import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.IDataStore;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.models.backendless.FINComment;
import org.helpapaw.helpapaw.utils.Injection;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static org.helpapaw.helpapaw.data.models.Comment.COMMENT_TYPE_USER_COMMENT;


/**
 * Created by iliyan on 8/4/16
 */
public class BackendlessCommentRepository implements CommentRepository {
    private static final String DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss";
    private static final String ID_FIELD = "objectId";
    private static final String NAME_FIELD = "name";
    private static final String CREATED_FIELD = "created";
    private static final String SIGNAL_ID = "signalID";
    private static final String OWNER_ID = "ownerID";
    private static final int pageSize = 100;

    @Override
    public void getAllCommentsBySignalId(String signalId, final LoadCommentsCallback callback) {

        String whereClause = SIGNAL_ID + " = '" + signalId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setPageSize(pageSize);
        queryBuilder.setSortBy(Collections.singletonList(CREATED_FIELD));

        getAllComments(callback, queryBuilder, 0);
    }

    @Override
    public void getCommentsByAuthorId(String authorId, final LoadCommentsCallback callback) {

        String whereClause = OWNER_ID + " = '" + authorId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setPageSize(pageSize);

        getAllComments(callback, queryBuilder, 0);
    }

    private void getAllComments(final LoadCommentsCallback callback, final DataQueryBuilder queryBuilder, final int offset) {
        queryBuilder.setOffset(offset);

        Backendless.Persistence.of(FINComment.class).find(queryBuilder, new AsyncCallback<List<FINComment>>() {
                    @Override
                    public void handleResponse(List<FINComment> foundComments) {
                        final List<Comment> comments = new ArrayList<>();

                        if (foundComments == null) {
                            callback.onCommentsLoaded(comments);
                            return;
                        }

                        for (int i = 0; i < foundComments.size(); i++) {
                            FINComment currentComment = foundComments.get(i);
                            String authorId = null;
                            String authorName = null;

                            BackendlessUser author = currentComment.getAuthor();
                            if (author != null) {
                                authorId = getToStringOrNull(author.getProperty(ID_FIELD));
                                authorName = getToStringOrNull(author.getProperty(NAME_FIELD));
                            }

                            Date dateCreated = null;
                            try {
                                String dateCreatedString = currentComment.getCreated();
                                DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
                                dateCreated = dateFormat.parse(dateCreatedString);
                            }
                            catch (Exception ex) {
                                FirebaseCrashlytics.getInstance().recordException(ex);
                            }

                            Comment comment = new Comment(
                                    currentComment.getObjectId(), currentComment.getSignalID(), authorId, authorName, currentComment.getPhoto(),
                                    dateCreated, currentComment.getText(), currentComment.getType());
                            comments.add(comment);
                        }

                        if (foundComments.size() == pageSize) {
                            getAllComments(new LoadCommentsCallback() {
                                @Override
                                public void onCommentsLoaded(List<Comment> comments2) {
                                    comments.addAll(comments2);
                                    callback.onCommentsLoaded(comments);
                                }

                                @Override
                                public void onCommentsFailure(String message) {
                                    callback.onCommentsFailure(message);
                                }
                            }, queryBuilder, offset + pageSize);
                        }
                        else {
                            callback.onCommentsLoaded(comments);
                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        callback.onCommentsFailure(fault.getMessage());
                    }
                });
    }

    @Override
    public void saveComment(String commentText, final Signal signal, final List<Comment> currentComments,
                            final PhotoRepository photoRepository, final File photoFile,
                            final SaveCommentCallback callback) {

        FINComment backendlessComment = new FINComment(commentText, signal.getId(),
                COMMENT_TYPE_USER_COMMENT, Backendless.UserService.CurrentUser());

        final IDataStore<FINComment> commentsStore = Backendless.Data.of(FINComment.class);
        commentsStore.save(backendlessComment, new AsyncCallback<FINComment>() {
            public void handleResponse(final FINComment newComment) {

                ArrayList<BackendlessUser> userList = new ArrayList<>();
                userList.add(Backendless.UserService.CurrentUser());
                commentsStore.setRelation( newComment, "author", userList,
                        new AsyncCallback<Integer>()
                        {
                            @Override
                            public void handleResponse( Integer response )
                            {
                                newComment.setAuthor(Backendless.UserService.CurrentUser());
                                String authorId = null;
                                String authorName = null;
                                if (newComment.getAuthor() != null) {
                                    authorId = getToStringOrNull(newComment.getAuthor().getProperty(ID_FIELD));
                                    authorName = getToStringOrNull(newComment.getAuthor().getProperty(NAME_FIELD));
                                }

                                Date dateCreated = null;
                                try {
                                    String dateCreatedString = newComment.getCreated();
                                    DateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
                                    dateCreated = dateFormat.parse(dateCreatedString);
                                }
                                catch (Exception ex) {
                                    FirebaseCrashlytics.getInstance().recordException(ex);
                                }

                                Injection.getPushNotificationsRepositoryInstance().pushNewCommentNotification(
                                        signal, newComment.getText(), currentComments);

                                Comment comment = new Comment(
                                        newComment.getObjectId(), signal.getId(), authorId, authorName, newComment.getPhoto(),
                                        dateCreated, newComment.getText(), COMMENT_TYPE_USER_COMMENT);

                                if (photoFile != null) {
                                    photoRepository.saveCommentPhoto(photoFile, backendlessComment.getObjectId(), new PhotoRepository.SavePhotoCallback() {
                                        @Override
                                        public void onPhotoSaved(String photoUrl) {
                                            newComment.setPhoto(photoUrl);
                                            comment.setPhotoUrl(photoUrl);
                                            commentsStore.save(newComment, new AsyncCallback<FINComment>()
                                                    {
                                                        @Override
                                                        public void handleResponse(final FINComment newComment )
                                                        {
                                                            callback.onCommentSaved(comment);
                                                        }

                                                        @Override
                                                        public void handleFault( BackendlessFault fault )
                                                        {
                                                            callback.onCommentFailure(fault.getMessage());
                                                        }
                                                    } );
                                        }

                                        @Override
                                        public void onPhotoFailure(String message) {
                                            callback.onCommentFailure(message);
                                        }
                                    });
                                }
                                else {
                                    callback.onCommentSaved(comment);
                                }
                            }

                            @Override
                            public void handleFault( BackendlessFault fault )
                            {
                                callback.onCommentFailure(fault.getMessage());
                            }
                        } );
            }

            public void handleFault(BackendlessFault fault) {
                callback.onCommentFailure(fault.getMessage());
            }
        });
    }

    private String getToStringOrNull(Object object) {
        if (object != null) {
            return object.toString();
        } else {
            return null;
        }
    }
}
