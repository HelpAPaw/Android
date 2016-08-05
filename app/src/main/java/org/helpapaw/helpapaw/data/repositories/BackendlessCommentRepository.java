package org.helpapaw.helpapaw.data.repositories;

import com.backendless.Backendless;
import com.backendless.BackendlessCollection;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.BackendlessDataQuery;
import com.backendless.persistence.QueryOptions;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.backendless.FINComment;

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

        String whereClause = "signalID = '" + signalId + "'";
        BackendlessDataQuery dataQuery = new BackendlessDataQuery();
        dataQuery.setWhereClause(whereClause);
        QueryOptions queryOptions = new QueryOptions();
        queryOptions.setSortBy(Collections.singletonList("created"));
        dataQuery.setQueryOptions(queryOptions);

        Backendless.Persistence.of(FINComment.class).find(dataQuery,
                new AsyncCallback<BackendlessCollection<FINComment>>() {
                    @Override
                    public void handleResponse(BackendlessCollection<FINComment> foundComments) {
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
        //TODO: change it to return date in MM/dd/yyyy hh:mm:ss format
        Long tsLong = System.currentTimeMillis() / 1000;
        String timestamp = tsLong.toString();

        FINComment backendlessComment =
                new FINComment(null,
                        commentText, timestamp, Backendless.UserService.CurrentUser());

        Backendless.Persistence.save(backendlessComment, new AsyncCallback<FINComment>() {
            public void handleResponse(FINComment response) {
                callback.onCommentSaved(response.getPOJOComment());
            }

            public void handleFault(BackendlessFault fault) {
                callback.onCommentFailure(fault.getMessage());
            }
        });
    }
}
