package org.helpapaw.helpapaw.data.repositories;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import org.helpapaw.helpapaw.data.models.Comment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by iliyan on 8/4/16
 */
public class BackendlessCommentRepository implements CommentRepository {
    @Override
    public void getAllCommentsBySignalId(String signalId, final LoadCommentsCallback callback) {
        final List<Comment> comments = new ArrayList<>();

        String whereClause = "signalID = " + signalId;
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(whereClause);
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setSortBy(Collections.singletonList("created"));
        dataQuery.setQueryOptions(queryOptions);

        Backendless.Persistence.of(org.helpapaw.helpapaw.data.models.backendless.Comment.class).find(dataQuery,
                new AsyncCallback<BackendlessCollection<org.helpapaw.helpapaw.data.models.backendless.Comment>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<org.helpapaw.helpapaw.data.models.backendless.Comment> foundComments) {
                        for (int i = 0; i < foundComments.getData().size(); i++) {
                            comments.add(foundComments.getData().get(i).getPOJOComment());
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
    public void saveComment(String commentText, final SaveCommentCallback callback) {
        Long tsLong = System.currentTimeMillis() / 1000;
        String timestamp = tsLong.toString();

        org.helpapaw.helpapaw.data.models.backendless.Comment backendlessComment =
                new org.helpapaw.helpapaw.data.models.backendless.Comment(null,
                        commentText, timestamp, Backendless.UserService.CurrentUser());

        Backendless.Persistence.save(backendlessComment, new AsyncCallback<org.helpapaw.helpapaw.data.models.backendless.Comment>() {
            public void handleResponse(org.helpapaw.helpapaw.data.models.backendless.Comment response) {
                callback.onCommentSaved(response.getPOJOComment());
            }

            public void handleFault(BackendlessFault fault) {
                callback.onCommentFailure(fault.getMessage());
            }
        });
    }
}
