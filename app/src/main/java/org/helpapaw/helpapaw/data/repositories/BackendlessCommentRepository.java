package org.helpapaw.helpapaw.data.repositories;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.backendless.FINComment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by iliyan on 8/4/16
 */
public class BackendlessCommentRepository implements CommentRepository {
    private static final String DATE_TIME_FORMAT = "MM/dd/yyyy hh:mm:ss";
    private static final String NAME_FIELD = "name";
    private static final String CREATED_FIELD = "created";

    @Override
    public void getAllCommentsBySignalId(String signalId, final LoadCommentsCallback callback) {
        final List<Comment> comments = new ArrayList<>();

        String whereClause = "signalID = '" + signalId + "'";
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(whereClause);
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setSortBy(Collections.singletonList(CREATED_FIELD));
        dataQuery.setQueryOptions(queryOptions);

        Backendless.Persistence.of(FINComment.class).find(dataQuery,
                new AsyncCallback<BackendlessCollection<FINComment>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<FINComment> foundComments) {
                        for (int i = 0; i < foundComments.getData().size(); i++) {
                            FINComment currentComment = foundComments.getData().get(i);
                            String authorName = null;
                            if (currentComment.getAuthor() != null && currentComment.getAuthor().getProperty(NAME_FIELD) != null) {
                                authorName = currentComment.getAuthor().getProperty(NAME_FIELD).toString();
                            }

                            Comment comment = new Comment(currentComment.getObjectId(),
                                    authorName, currentComment.getCreated(), currentComment.getText());
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
    public void saveComment(String commentText, String signalId, final SaveCommentCallback callback) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_TIME_FORMAT, Locale.getDefault());
        String currentDate = dateFormat.format(new Date());

        FINComment backendlessComment =
                new FINComment(commentText, currentDate, signalId, Backendless.UserService.CurrentUser());

        Backendless.Persistence.save(backendlessComment, new AsyncCallback<FINComment>() {
            public void handleResponse(FINComment newComment) {
                String authorName = null;
                if (newComment.getAuthor() != null && newComment.getAuthor().getProperty(NAME_FIELD) != null) {
                    authorName = newComment.getAuthor().getProperty(NAME_FIELD).toString();
                }
                Comment comment = new Comment(newComment.getObjectId(),
                        authorName, newComment.getCreated(), newComment.getText());
                callback.onCommentSaved(comment);
            }

            public void handleFault(BackendlessFault fault) {
                callback.onCommentFailure(fault.getMessage());
            }
        });
    }
}
