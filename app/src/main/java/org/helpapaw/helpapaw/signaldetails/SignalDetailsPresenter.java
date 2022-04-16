package org.helpapaw.helpapaw.signaldetails;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
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

import java.io.File;
import java.util.List;

import io.branch.indexing.BranchUniversalObject;
import io.branch.referral.util.ContentMetadata;
import io.branch.referral.util.LinkProperties;

/**
 * Created by iliyan on 7/25/16
 */
public class SignalDetailsPresenter extends Presenter<SignalDetailsContract.View>
        implements SignalDetailsContract.UserActionsListener, UploadPhotoContract.UserActionsListener {

    private enum PhotoDestination {
        SIGNAL, COMMENT
    }

    private boolean showProgressBar;
    private List<Comment> commentList;
    private Signal signal;
    private File photoFile;
    private PhotoDestination photoDestination = PhotoDestination.SIGNAL;

    private final CommentRepository commentRepository;
    private final PhotoRepository photoRepository;
    private final SignalRepository signalRepository;
    private final UserManager userManager;

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
        setCommentsProgressIndicator(showProgressBar);

        if (signal != null) {
            Injection.getCrashLogger().log("Show signal details for " + signal.getId());

            this.signal = signal;
            getView().showSignalDetails(signal);
            showAuthorActionsIfNeeded(signal);

            if (commentList != null) {
                setCommentsProgressIndicator(false);

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

    private void showAuthorActionsIfNeeded(Signal signal) {
        if (userManager.getLoggedUserId().equals(signal.getAuthorId())) {
            if (signal.getIsDeleted()) {
                getView().hideSignalAuthorActions();
            } else {
                getView().showSignalAuthorActions();
            }
            photoRepository.signalPhotoExists(signal.getId(), new PhotoRepository.PhotoExistsCallback() {
                @Override
                public void onPhotoExistsSuccess(boolean photoExists) {
                    if (!isViewAvailable()) return;

                if (photoExists || signal.getIsDeleted()) {
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
        else {
            getView().hideSignalAuthorActions();
        }
    }

    public void loadCommentsForSignal(String signalId) {
        if(Utils.getInstance().hasNetworkConnection()) {
            commentRepository.getAllCommentsBySignalId(signalId, new CommentRepository.LoadCommentsCallback() {
                @Override
                public void onCommentsLoaded(List<Comment> comments) {
                    if (!isViewAvailable()) return;
                    commentList = comments;
                    setCommentsProgressIndicator(false);

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
        getView().scrollToBottom();
        setCommentsProgressIndicator(true);

        if (Utils.getInstance().hasNetworkConnection()) {
            userManager.isLoggedIn(new UserManager.LoginCallback() {
                @Override
                public void onLoginSuccess(String userId) {
                    if (!isViewAvailable()) return;
                    setCommentsProgressIndicator(false);

                    if (getView() instanceof UploadPhotoContract.View) {
                        photoDestination = PhotoDestination.COMMENT;
                        ((UploadPhotoContract.View)getView()).showSendPhotoBottomSheet(SignalDetailsPresenter.this);
                    }
                }

                @Override
                public void onLoginFailure(String message) {
                    if (!isViewAvailable()) return;
                    setCommentsProgressIndicator(false);
                    getView().showRegistrationRequiredAlert(R.string.txt_only_registered_users_can_comment);
                }
            });
        } else {
            setCommentsProgressIndicator(false);
            getView().showNoInternetMessage();
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
                getView().scrollToBottom();
                setCommentsProgressIndicator(true);
                saveComment(comment, photoFile);
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
                    Injection.getCrashLogger().log("Initiate status change for signal " + signal.getId());
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
    public void onUpdateTitle(final String newTitle) {
        Injection.getCrashLogger().log("Initiate title change for signal " + signal.getId());
        signalRepository.updateSignalTitle(signal.getId(), newTitle, new SignalRepository.UpdateTitleCallback() {
            @Override
            public void onTitleUpdated(String title) {
                if(!isViewAvailable()) return;
                signal.setTitle(title);
                getView().showSignalDetails(signal);
                showAuthorActionsIfNeeded(signal);
            }

            @Override
            public void onTitleFailure(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
                getView().editSignalTitle();
            }
        });
    }

    @Override
    public void onDeleteSignal() {
        Injection.getCrashLogger().log("Initiate delete for signal " + signal.getId());
        signalRepository.deleteSignal(signal.getId(), new SignalRepository.DeleteSignalCallback() {
            @Override
            public void onSignalDeleted() {
                if(!isViewAvailable()) return;
                signal.setIsDeleted(true);
                getView().closeScreenWithResult(signal);
            }

            @Override
            public void onSignalDeletedFailed(String message) {
                if (!isViewAvailable()) return;
                getView().showMessage(message);
            }
        });
    }

    @Override
    public void onNavigateButtonClicked() {
        double latitude = signal.getLatitude();
        double longitude = signal.getLongitude();
        getView().openNavigation(latitude, longitude);
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
    public void onCommentPhotoClicked(String photoUrl) {
        getView().openCommentPhotoScreen(photoUrl);
    }

    @Override
    public void onUploadSignalPhotoClicked() {
        if (getView() instanceof UploadPhotoContract.View){
            photoDestination = PhotoDestination.SIGNAL;
            ((UploadPhotoContract.View) getView()).showSendPhotoBottomSheet(this);
        }
    }

    @Override
    public void onEditSignalTitleClicked() {
        getView().editSignalTitle();
    }

    @Override
    public void onDeleteSignalClicked() {
        getView().deleteSignal();
    }

    @Override
    public void onSaveEditSignalTitleClicked() {
        getView().saveEditSignalTitle();
    }

    @Override
    public void onCancelEditSignalTitleClicked(String originalTitle) {
        getView().cancelEditSignalTitle(originalTitle);
    }

    @Override
    public void onShareSignalClicked() {

        BranchUniversalObject buo = new BranchUniversalObject()
                .setCanonicalIdentifier("signal/" + signal.getId())
                .setTitle(signal.getTitle())
                .setContentImageUrl(photoRepository.getSignalPhotoUrl(signal.getId()))
                .setContentIndexingMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setLocalIndexMode(BranchUniversalObject.CONTENT_INDEX_MODE.PUBLIC)
                .setContentMetadata(new ContentMetadata().addCustomMetadata("signalId", signal.getId()));

        LinkProperties linkProperties = new LinkProperties();

        buo.generateShortUrl(PawApplication.getContext(), linkProperties, (url, error) -> {
            if (error == null) {
                // Set original short url as parameter of desktop_url - this way it can be used to
                // generate a QR which can be scanned with a smartphone
                linkProperties.addControlParameter("$desktop_url", "https://www.helpapaw.org/signal.html?link=" + url);
                buo.generateShortUrl(PawApplication.getContext(), linkProperties, (url2, error2) -> {
                    if (error2 == null) {
                        getView().shareSignalLink(url2);
                    }
                });
            }
        });
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
        if (getView() instanceof UploadPhotoContract.View) {
            photoFile = ((UploadPhotoContract.View) getView()).openCamera();
        }
    }

    @Override
    public void onGalleryOptionSelected() {
        if (getView() instanceof UploadPhotoContract.View){
            ((UploadPhotoContract.View) getView()).openGallery();
        }
    }

    @Override
    public void onSignalPhotoSelected(File photoFile) {
        if (photoFile != null) {
            this.photoFile = photoFile;
        }

        if (photoDestination == PhotoDestination.SIGNAL) {
            saveSignalPhoto(this.photoFile, signal);
        }
        else if (photoDestination == PhotoDestination.COMMENT) {
            getView().setThumbnailToCommentPhotoButton(this.photoFile.getPath());
        }
    }

    private void saveSignalPhoto(final File photoFile, final Signal signal) {
        photoRepository.saveSignalPhoto(photoFile, signal.getId(), new PhotoRepository.SavePhotoCallback() {
            @Override
            public void onPhotoSaved(String photoUrl) {
                signal.setPhotoUrl(photoUrl);
                // Show new photo
                if (!isViewAvailable()) return;
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

    private void saveComment(String comment, File photoFile) {
        Injection.getCrashLogger().log("Initiate save new comment for signal" + signal.getId());
        commentRepository.saveComment(comment, signal, commentList, photoRepository, photoFile, new CommentRepository.SaveCommentCallback() {
            @Override
            public void onCommentSaved(Comment comment) {
                if (!isViewAvailable()) return;

                setCommentsProgressIndicator(false);
                getView().clearSendCommentView();
                getView().setNoCommentsTextVisibility(false);
                commentList.add(comment);
                getView().displayComments(commentList);
                getView().scrollToBottom();
            }

            @Override
            public void onCommentFailure(String message) {
                if (!isViewAvailable()) return;
                // Refresh comments in case a comment was submitted but its photo failed
                loadCommentsForSignal(signal.getId());
                getView().showMessage(message);
            }
        });
        this.photoFile = null;
    }

    private void setSignalStatus(int status) {
        this.signal.setStatus(status);
    }

    private boolean isViewAvailable() {
        return getView() != null && getView().isActive();
    }

    private void setCommentsProgressIndicator(boolean active) {
        getView().setCommentsProgressIndicator(active);
        this.showProgressBar = active;
    }
}
