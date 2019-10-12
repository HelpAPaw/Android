package org.helpapaw.helpapaw.data.repositories;

import android.util.Log;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.IDataStore;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.models.backendless.FINComment;
import org.helpapaw.helpapaw.utils.Injection;

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

    @Override
    public void getAllCommentsBySignalId(String signalId, final LoadCommentsCallback callback) {
        final List<Comment> comments = new ArrayList<>();

        String whereClause = "signalID = '" + signalId + "'";
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setWhereClause(whereClause);
        queryBuilder.setPageSize(30);
        queryBuilder.setSortBy(Collections.singletonList(CREATED_FIELD));

        Backendless.Persistence.of(FINComment.class).find(queryBuilder, new AsyncCallback<List<FINComment>>() {
                    @Override
                    public void handleResponse(List<FINComment> foundComments) {
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
                                Log.d(BackendlessCommentRepository.class.getName(), "Failed to parse comment date.");
                            }

                            Comment comment = new Comment(currentComment.getObjectId(), authorId, authorName, dateCreated, currentComment.getText(), currentComment.getType());
                            comments.add(comment);
                        }

                        callback.onCommentsLoaded(comments);
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        callback.onCommentsFailure(fault.getMessage());
                    }
                });
    }

    @Override
    public void saveComment(String commentText, final Signal signal, final List<Comment> currentComments, final SaveCommentCallback callback) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        FINComment backendlessComment = new FINComment(commentText, currentDate, signal.getId(), COMMENT_TYPE_USER_COMMENT, Backendless.UserService.CurrentUser());

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
                                Log.d(BackendlessCommentRepository.class.getName(), "Failed to parse comment date.");
                            }

                            Injection.getPushNotificationsRepositoryInstance().pushSignalUpdatedNotification(signal, currentComments, PushNotificationsRepository.SignalUpdate.NEW_COMMENT, 0, newComment.getText());
                            Comment comment = new Comment(newComment.getObjectId(), authorId, authorName, dateCreated, newComment.getText(), COMMENT_TYPE_USER_COMMENT);
                            callback.onCommentSaved(comment);
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
