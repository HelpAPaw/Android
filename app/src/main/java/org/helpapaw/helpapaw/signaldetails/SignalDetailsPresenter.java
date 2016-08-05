package org.helpapaw.helpapaw.signaldetails;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.List;

/**
 * Created by iliyan on 7/25/16
 */
public class SignalDetailsPresenter extends Presenter<SignalDetailsContract.View> implements SignalDetailsContract.UserActionsListener {

    private boolean showProgressBar;
    private List<Comment> commentList;

    private CommentRepository commentRepository;
    private PhotoRepository photoRepository;

    public SignalDetailsPresenter(SignalDetailsContract.View view) {
        super(view);
        showProgressBar = false;
        commentRepository = Injection.getCommentRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
    }

    @Override
    public void onInitDetailsScreen(Signal signal) {
        if (signal != null) {
            setProgressIndicator(true);
            signal.setPhotoUrl(photoRepository.getPhotoUrl(signal.getId()));
            getView().showSignalDetails(signal);

            if (commentList != null) {
                getView().displayComments(commentList);
                setProgressIndicator(false);
            } else {
                loadCommentsForSignal(signal.getId());
            }
        }
    }

    private void loadCommentsForSignal(String signalId) {
        commentRepository.getAllCommentsBySignalId(signalId, new CommentRepository.LoadCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {
                commentList = comments;
                getView().displayComments(comments);
                setProgressIndicator(false);
            }

            @Override
            public void onCommentsFailure(String message) {
                getView().showMessage(message);
            }
        });
    }

    @Override
    public void onAddCommentButtonClicked(String comment) {

    }

    private void setProgressIndicator(boolean active) {
        getView().setProgressIndicator(active);
        this.showProgressBar = active;
    }
}
