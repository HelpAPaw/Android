package org.helpapaw.helpapaw.signaldetails;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.List;

/**
 * Created by iliyan on 7/25/16
 */
public class SignalDetailsPresenter extends Presenter<SignalDetailsContract.View> implements SignalDetailsContract.UserActionsListener {

    private boolean showProgressBar;
    private List<Comment> commentList;
    private Signal signal;

    private boolean statusChanged;

    private CommentRepository commentRepository;
    private PhotoRepository photoRepository;
    private SignalRepository signalRepository;
    private UserManager userManager;

    public SignalDetailsPresenter(SignalDetailsContract.View view) {
        super(view);
        showProgressBar = true;
        statusChanged = false;
        commentRepository = Injection.getCommentRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
        signalRepository = Injection.getSignalRepositoryInstance();
    }

    @Override
    public void onInitDetailsScreen(Signal signal) {
        setProgressIndicator(showProgressBar);
        if (signal != null) {
            this.signal = signal;
            signal.setPhotoUrl(photoRepository.getPhotoUrl(signal.getId()));
            getView().showSignalDetails(signal);

            if (commentList != null) {
                setProgressIndicator(false);

                if (commentList.size() == 0) {
                    getView().setNoCommentsTextVisibility(true);
                } else {
                    getView().displayComments(commentList);
                    getView().setNoCommentsTextVisibility(false);
                }
            } else {
                loadCommentsForSignal(signal.getId());
            }
        }
    }

    public void loadCommentsForSignal(String signalId) {
        if(Utils.getInstance().hasNetworkConnection()) {
            commentRepository.getAllCommentsBySignalId(signalId, new CommentRepository.LoadCommentsCallback() {
                @Override
                public void onCommentsLoaded(List<Comment> comments) {
                    if (!isViewAvailable()) return;
                    commentList = comments;
                    setProgressIndicator(false);

                    if (commentList.size() == 0) {
                        getView().setNoCommentsTextVisibility(true);
                    } else {
                        getView().displayComments(comments);
                        getView().setNoCommentsTextVisibility(false);
                    }
                }

                @Override
                public void onCommentsFailure(String message) {
                    if (!isViewAvailable()) return;
                    getView().showMessage(message);
                }
            });
        } else {
            if(isViewAvailable()) {
                getView().showNoInternetMessage();
            }
        }
    }

    @Override
    public void onAddCommentButtonClicked(final String comment) {
        if (Utils.getInstance().hasNetworkConnection()) {
            if (comment != null && comment.trim().length() > 0) {
                getView().hideKeyboard();
                setProgressIndicator(true);
                getView().scrollToBottom();

                userManager.isLoggedIn(new UserManager.LoginCallback() {
                    @Override
                    public void onLoginSuccess() {
                        if (!isViewAvailable()) return;
                        getView().clearSendCommentView();
                        saveComment(comment, signal.getId());
                    }

                    @Override
                    public void onLoginFailure(String message) {
                        if (!isViewAvailable()) return;
                        setProgressIndicator(false);
                        getView().openLoginScreen();
                    }
                });

            } else {
                getView().showCommentErrorMessage();
            }
        } else {
            getView().showNoInternetMessage();
        }
    }

    @Override
    public void onRequestStatusChange(final int status) {
        if (Utils.getInstance().hasNetworkConnection()) {

            userManager.isLoggedIn(new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess() {
                    signalRepository.updateSignalStatus(signal.getId(), status, new SignalRepository.UpdateStatusCallback() {
                        @Override
                        public void onStatusUpdated(int status) {
                            if(!isViewAvailable()) return;
                            setSignalStatus(status);
                            getView().showStatusUpdatedMessage();
                            getView().onStatusChangeRequestFinished(true, status);
                        }

                        @Override
                        public void onStatusFailure(String message) {
                            if (!isViewAvailable()) return;
                            getView().showMessage(message);
                            getView().onStatusChangeRequestFinished(false, 0);
                        }
                    });
                }

                @Override
                public void onLoginFailure(String message) {
                    if (!isViewAvailable()) return;
                    getView().onStatusChangeRequestFinished(false, 0);
                    getView().openLoginScreen();
                }
            });
        } else {
            getView().showNoInternetMessage();
        }
    }

    @Override
    public void onCallButtonClicked() {
        String phoneNumber = signal.getAuthorPhone();
        getView().openNumberDialer(phoneNumber);
    }

    @Override
    public void onSignalDetailsClosing() {
        getView().closeScreenWithResult(signal);
    }

    @Override
    public void onBottomReached(boolean isBottomReached) {
        getView().setShadowVisibility(!isBottomReached);
    }

    private void saveComment(String comment, String signalId) {
        commentRepository.saveComment(comment, signalId, new CommentRepository.SaveCommentCallback() {
            @Override
            public void onCommentSaved(Comment comment) {
                if (!isViewAvailable()) return;
                setProgressIndicator(false);
                commentList.add(comment);
                getView().setNoCommentsTextVisibility(false);
                getView().displayComments(commentList);
            }

            @Override
            public void onCommentFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    private void setSignalStatus(int status) {
        this.signal.setStatus(status);
        statusChanged = true;
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    private void setProgressIndicator(boolean active) {
        getView().setProgressIndicator(active);
        this.showProgressBar = active;
    }
}
