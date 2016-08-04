package org.helpapaw.helpapaw.signaldetails;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.List;

/**
 * Created by iliyan on 7/25/16
 */
public class SignalDetailsPresenter extends Presenter<SignalDetailsContract.View> implements SignalDetailsContract.UserActionsListener {

    private boolean showProgressBar;

    private CommentRepository commentRepository;

    public SignalDetailsPresenter(SignalDetailsContract.View view) {
        super(view);
        showProgressBar = false;
        commentRepository = Injection.getCommentRepositoryInstance();
    }

    @Override
    public void onInitDetailsScreen(Signal signal) {
        setProgressIndicator(true);
        getView().showSignalDetails(signal);

        //Start loading the comments
        commentRepository.getAllCommentsBySignalId(signal.getId(), new CommentRepository.LoadCommentsCallback() {
            @Override
            public void onCommentsLoaded(List<Comment> comments) {
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
