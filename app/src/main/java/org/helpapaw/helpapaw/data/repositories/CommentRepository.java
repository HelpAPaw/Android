package org.helpapaw.helpapaw.data.repositories;

import org.helpapaw.helpapaw.data.models.Comment;

import java.util.List;

/**
 * Created by iliyan on 8/4/16
 */
public interface CommentRepository {

    void getAllCommentsBySignalId(String signalId, LoadCommentsCallback callback);

    void saveComment(String commentText, String signalId, SaveCommentCallback callback);

    interface LoadCommentsCallback {

        void onCommentsLoaded(List<Comment> comments);

        void onCommentsFailure(String message);
    }

    interface SaveCommentCallback {

        void onCommentSaved(Comment comment);

        void onCommentFailure(String message);
    }
}
