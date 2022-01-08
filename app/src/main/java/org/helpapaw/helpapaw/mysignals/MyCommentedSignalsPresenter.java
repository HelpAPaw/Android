package org.helpapaw.helpapaw.mysignals;

import android.view.View;

import org.helpapaw.helpapaw.base.Presenter;
import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;
import org.helpapaw.helpapaw.data.repositories.CommentRepository;
import org.helpapaw.helpapaw.data.repositories.PhotoRepository;
import org.helpapaw.helpapaw.data.repositories.SignalRepository;
import org.helpapaw.helpapaw.data.user.UserManager;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.Utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MyCommentedSignalsPresenter extends Presenter<MySignalsContract.View> implements MySignalsContract.UserActionsListener {

    private SignalRepository signalRepository;
    private CommentRepository commentRepository;
    private PhotoRepository photoRepository;
    private UserManager userManager;

    private Set<String> commentedSignalsIds;;

    MyCommentedSignalsPresenter(MySignalsContract.View view) {
        super(view);
        signalRepository = Injection.getSignalRepositoryInstance();
        commentRepository = Injection.getCommentRepositoryInstance();
        photoRepository = Injection.getPhotoRepositoryInstance();
        userManager = Injection.getUserManagerInstance();

        commentedSignalsIds = new HashSet<>();
    }

    @Override
    public void onViewResume() {
        if (userManager.isLoggedIn()) {
            String loggedUserId = userManager.getLoggedUserId();

            getCommentedSignalsfromDb(loggedUserId);
        }
    }

    private void getCommentedSignalsfromDb(String ownerId) {
        if (Utils.getInstance().hasNetworkConnection()) {

            if (userManager.isLoggedIn()) {
                getView().setProgressVisibility(View.VISIBLE);
            }

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

                            if (signals.size() != 0) {
                                getView().displaySignals(signals);
                                getView().setProgressVisibility(View.GONE);
                                getView().onNoSignalsToBeListed(false);
                            } else {
                                getView().onNoSignalsToBeListed(true);
                            }
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
