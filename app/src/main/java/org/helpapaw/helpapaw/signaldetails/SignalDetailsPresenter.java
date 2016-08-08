package org.helpapaw.helpapaw.signaldetails;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;

import java.util.Collections;
import java.util.List;

/**
 * Created by iliyan on 7/25/16
 */
public class SignalDetailsPresenter extends Presenter<SignalDetailsContract.View> implements SignalDetailsContract.UserActionsListener {

    private boolean showProgressBar;
    private List<Comment> commentList;
    private Signal signal;

    private CommentRepository commentRepository;
    private PhotoRepository photoRepository;
    private UserManager userManager;

    public SignalDetailsPresenter(SignalDetailsContract.View view) {
        super(view);
        showProgressBar = false;
        commentRepository = Injection.getCommentRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
    }

    @Override
    public void onInitDetailsScreen(Signal signal) {
        if (signal != null) {
            this.signal = signal;
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
                if (getView() == null || !getView().isActive()) return;

                getView().displayComments(comments);
                commentList = comments;
                setProgressIndicator(false);
            }

            @Override
            public void onCommentsFailure(String message) {
                if (getView() == null || !getView().isActive()) return;

                getView().showMessage(message);
            }
        });
    }

    @Override
    public void onAddCommentButtonClicked(final String comment) {
        if (comment != null && comment.trim().length() > 0) {
            getView().hideKeyboard();
            setProgressIndicator(true);

            userManager.isLoggedIn(new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    if (getView() == null || !getView().isActive()) return;

                    getView().clearSendCommentView();
                    saveComment(comment, signal.getId());
                }

                @Override
                public void onLoginFailure(String message) {
                    if (getView() == null || !getView().isActive()) return;
                    setProgressIndicator(false);
                    getView().openLoginScreen();
                }
            });

        } else {
            getView().showCommentErrorMessage();
        }
    }

    private void saveComment(String comment, String signalId) {
        commentRepository.saveComment(comment, signalId, new CommentRepository.SaveCommentCallback() {
            @Override
            public void onCommentSaved(Comment comment) {
                if (getView() == null || !getView().isActive()) return;
                setProgressIndicator(false);
                getView().displayComments(Collections.singletonList(comment));
            }

            @Override
            public void onCommentFailure(String message) {
                if (getView() == null || !getView().isActive()) return;
                getView().showMessage(message);
            }
        });
    }

    private void setProgressIndicator(boolean active) {
        getView().setProgressIndicator(active);
        this.showProgressBar = active;
    }
}
