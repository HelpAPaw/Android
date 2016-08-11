package org.helpapaw.helpapaw.signaldetails;

import org.helpapaw.helpapaw.data.models.Comment;
import org.helpapaw.helpapaw.data.models.Signal;

import java.util.List;

/**
 * Created by iliyan on 7/25/16
 */
public interface SignalDetailsContract {

    interface View {

        void showMessage(String message);

        void setProgressIndicator(boolean active);

        void hideKeyboard();

        void showSignalDetails(Signal signal);

        void displayComments(List<Comment> comments);

        void showCommentErrorMessage();

        void clearSendCommentView();

        void openLoginScreen();

        void setNoCommentsTextVisibility(boolean visibility);

        void openNumberDialer(String phoneNumber);

        void showNoInternetMessage();

        void scrollToBottom();

        boolean isActive();
    }

    interface UserActionsListener {

        void onInitDetailsScreen(Signal signal);

        void onAddCommentButtonClicked(String comment);

        void onStatusChanged(int status);

        void onCallButtonClicked();
    }
}
