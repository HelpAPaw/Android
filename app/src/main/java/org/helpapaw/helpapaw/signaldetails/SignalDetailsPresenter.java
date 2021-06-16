package org.helpapaw.helpapaw.signaldetails;

import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.photo.UploadPhotoContract;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.List;

/**
 * Created by iliyan on 7/25/16
 */
public class SignalDetailsPresenter extends Presenter<SignalDetailsContract.View>
        implements SignalDetailsContract.UserActionsListener, UploadPhotoContract.UserActionsListener {

    private boolean showProgressBar;
    private List<Comment> commentList;
    private Signal signal;
    private String photoUri;

    private CommentRepository commentRepository;
    private PhotoRepository photoRepository;
    private SignalRepository signalRepository;
    private UserManager userManager;

    public SignalDetailsPresenter(SignalDetailsContract.View view) {
        super(view);

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
            showUploadButtonIfNeeded(signal);

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

    private void showUploadButtonIfNeeded(Signal signal) {
        if (userManager.getLoggedUserId().equals(signal.getAuthorId())) {
            photoRepository.photoExists(signal.getId(), new PhotoRepository.PhotoExistsCallback() {
                @Override
                public void onPhotoExistsSuccess(boolean photoExists) {
                    if (!isViewAvailable()) return;

                    if (photoExists) {
                        getView().hideUploadPhotoButton();
                    }
                    else {
                        getView().showUploadPhotoButton();
                    }
                }

                @Override
                public void onPhotoExistsFailure(String message) {
                    // Don't show an error because user doesn't have any action and will be confused
                }
            });
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
    public void onChooseCommentPhotoIconClicked() {
        getView().hideKeyboard();
        if (getView() instanceof UploadPhotoContract.View) {
            ((UploadPhotoContract.View)getView()).showSendPhotoBottomSheet(this);
        }
    }

    @Override
    public void onPhotoSelected(String photoUri) {
        this.photoUri = photoUri;
        getView().setThumbnailImage(photoUri);
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
    public void onUploadSignalPhotoClicked() {
        if (getView() instanceof UploadPhotoContract.View){
            ((UploadPhotoContract.View) getView()).showSendPhotoBottomSheet(this);
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
        if (getView() instanceof UploadPhotoContract.View){
            ((UploadPhotoContract.View) getView()).openCamera();
        }
    }

    @Override
    public void onGalleryOptionSelected() {
        if (getView() instanceof UploadPhotoContract.View){
            ((UploadPhotoContract.View) getView()).openGallery();
        }
    }

    @Override
    public void onSignalPhotoSelected(String photoUri) {
        savePhoto(photoUri, signal);
    }

    private void savePhoto(final String photoUri, final Signal signal) {
        photoRepository.savePhoto(photoUri, signal.getId(), new PhotoRepository.SavePhotoCallback() {
            @Override
            public void onPhotoSaved(String photoUrl) {
                signal.setPhotoUrl(photoUrl);
                // Show new photo
                getView().hideUploadPhotoButton();
                getView().showSignalPhoto(signal);
            }

            @Override
            public void onPhotoFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
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
