package org.helpapaw.helpapaw.mysignals;

import android.app.ProgressDialog;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.privacypolicy.PrivacyPolicyActivity;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MySignalsPresenter extends Presenter<MySignalsContract.View> implements MySignalsContract.UserActionsListener {

    private SignalRepository signalRepository;
    private CommentRepository commentRepository;
    private PhotoRepository photoRepository;
    private UserManager userManager;

    private Set<String> commentedSignalsIds;;

    MySignalsPresenter(MySignalsContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        commentRepository = Injection.getCommentRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        userManager = Injection.getUserManagerInstance();

        commentedSignalsIds = new HashSet<>();
    }

    @Override
    public void onOpenMySignalsScreen() {
        if (!userManager.isLoggedIn()) {
            getView().showRegistrationRequiredAlert();
        }
    }

    @Override
    public void onLoadMySignals() {
        if (userManager.isLoggedIn()) {
            String loggedUserId = userManager.getLoggedUserId();
            getSubmittedSignalsfromDb(loggedUserId);
            getCommentedSignalsfromDb(loggedUserId);
        }
    }

    private void getSubmittedSignalsfromDb(String ownerId) {
        if (Utils.getInstance().hasNetworkConnection()) {

            signalRepository.getSignalsByOwnerId(ownerId,
                    new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            if (!isViewAvailable()) return;

                            for (Signal signal : signals) {
                                signal.setPhotoUrl(photoRepository.getSignalPhotoUrl(signal.getId()));
                            }

                            getView().displaySubmittedSignals(signals);
                        }

                        @Override
                        public void onSignalsFailure(String message) {
                            if (!isViewAvailable()) return;
                            getView().showMessage(message);
                        }
                    });

        } else {
            getView().showNoInternetMessage();
        }
    }

    private void getCommentedSignalsfromDb(String ownerId) {
        if (Utils.getInstance().hasNetworkConnection()) {

            commentRepository.getCommentsByAuthorId(ownerId, new CommentRepository.LoadCommentsCallback() {
                @Override
                public void onCommentsLoaded(List<Comment> comments) {
                    if (!isViewAvailable()) return;

                    for (Comment comment : comments) {
                        commentedSignalsIds.add(comment.getSignalId());
                    }

                    signalRepository.getSignalsByListOfIdsExcludingCurrentUser(commentedSignalsIds, new SignalRepository.LoadSignalsCallback() {
                        @Override
                        public void onSignalsLoaded(List<Signal> signals) {
                            if (!isViewAvailable()) return;

                            for (Signal signal : signals) {
                                signal.setPhotoUrl(photoRepository.getSignalPhotoUrl(signal.getId()));
                            }

                            getView().displayCommentedSignals(signals);
                        }

                        @Override
                        public void onSignalsFailure(String message) {
                            if (!isViewAvailable()) return;
                            getView().showMessage(message);
                        }
                    });
                }

                @Override
                public void onCommentsFailure(String message) {
                  getView().showMessage(message);
                }
            });

        } else {
            getView().showNoInternetMessage();
        }
    }

    private boolean isViewAvailable() {
        return getView() != null;
    }
}
