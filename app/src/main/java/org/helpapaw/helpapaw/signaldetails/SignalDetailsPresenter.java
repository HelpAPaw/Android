package org.helpapaw.helpapaw.signaldetails;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.signalphoto.SignalPhotoContract;
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

    private CommentRepository commentRepository;
    private PhotoRepository photoRepository;
    private SignalRepository signalRepository;
    private UserManager userManager;

    private FragmentManager fragmentManager;
    private Fragment fragment;

    public SignalDetailsPresenter(SignalDetailsContract.View view,
                                  FragmentManager fragmentManager,
                                  Fragment fragment) {
        super(view);

        this.fragmentManager = fragmentManager;
        this.fragment = fragment;

        showProgressBar = true;
        commentRepository = Injection.getCommentRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        userManager = Injection.getUserManagerInstance();
        signalRepository = Injection.getSignalRepositoryInstance();

    }

    @Override
    public void onInitDetailsScreen(Signal signal) {
        setProgressIndicator(showProgressBar);
        if (signal != null) {
            FirebaseCrashlytics.getInstance().log("Show signal details for " + signal.getId());
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
    public void onTryToAddComment() {
        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.isLoggedIn(new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess(String userId) {}

                @Override
                public void onLoginFailure(String message) {
                    if (!isViewAvailable()) return;
                    getView().showRegistrationRequiredAlert(R.string.txt_only_registered_users_can_comment);
                }
            });
        } else {
            getView().showNoInternetMessage();
        }
    }

    @Override
    public void onAddCommentButtonClicked(final String comment) {
        if (Utils.getInstance().hasNetworkConnection()) {
            if (comment != null && comment.trim().length() > 0) {
                getView().hideKeyboard();
                setProgressIndicator(true);
                getView().scrollToBottom();
                getView().clearSendCommentView();
                saveComment(comment);
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
                public void onLoginSuccess(String userId) {
                    FirebaseCrashlytics.getInstance().log("Initiate status change for signal " + signal.getId());
                    signalRepository.updateSignalStatus(signal.getId(), status, commentList, new SignalRepository.UpdateStatusCallback() {
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
                    getView().showRegistrationRequiredAlert(R.string.txt_only_registered_users_can_change_status);
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
    public void onSignalPhotoClicked() {
        getView().openSignalPhotoScreen();
    }

    @Override
    public void onChangeSignalPhotoClicked() {
        if (getView() instanceof SignalPhotoContract.Upload){
            ((SignalPhotoContract.Upload) getView()).showSendPhotoBottomSheet(this, fragmentManager); // TODO
        }
    }

    @Override
    public void onSignalDetailsClosing() {
        getView().closeScreenWithResult(signal);
    }

    @Override
    public void onBottomReached(boolean isBottomReached) {
        getView().setShadowVisibility(!isBottomReached);
    }

    @Override
    public void onCameraOptionSelected() {
        if (getView() instanceof SignalPhotoContract.Upload){
            ((SignalPhotoContract.Upload) getView()).openCamera(fragment); // TODO
        }
    }

    @Override
    public void onGalleryOptionSelected() {
        if (getView() instanceof SignalPhotoContract.Upload){
            ((SignalPhotoContract.Upload) getView()).openGallery(fragment);
        }
    }

    @Override
    public void onSignalPhotoSelected(String photoUri) {
        savePhoto(photoUri, signal);
        signal.setPhotoUrl(photoUri);
    }

    private void savePhoto(final String photoUri, final Signal signal) {
        photoRepository.savePhoto(photoUri, signal.getId(), new PhotoRepository.SavePhotoCallback() {
            @Override
            public void onPhotoSaved() {
                // refresh
                FragmentTransaction fragTransaction = fragmentManager.beginTransaction();
                fragTransaction.detach(fragment);
                fragTransaction.attach(fragment);
                fragTransaction.commit();
            }

            @Override
            public void onPhotoFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    public String getCurrentUserId() {
        return userManager.getUserId();
    }

    public boolean isAnyPhotoUploaded(String signalId) {
        return photoRepository.photoExists(signalId);
    }

    private void saveComment(String comment) {
        FirebaseCrashlytics.getInstance().log("Initiate save new comment for signal" + signal.getId());
        commentRepository.saveComment(comment, signal, commentList, new CommentRepository.SaveCommentCallback() {
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
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    private void setProgressIndicator(boolean active) {
        getView().setProgressIndicator(active);
        this.showProgressBar = active;
    }
}
