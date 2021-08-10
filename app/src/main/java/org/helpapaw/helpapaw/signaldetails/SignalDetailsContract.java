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

        void setCommentsProgressIndicator(boolean active);

        void hideKeyboard();

        void showSignalDetails(Signal signal);

        void showUploadPhotoButton();

        void hideUploadPhotoButton();

        void showEditSignalDescriptionButton();

        void showSignalPhoto(Signal signal);

        void displayComments(List<Comment> comments);

        void showCommentErrorMessage();

        void clearSendCommentView();

        void showRegistrationRequiredAlert(int messageId);

        void openLoginScreen();

        void setNoCommentsTextVisibility(boolean visibility);

        void openNumberDialer(String phoneNumber);

        void showNoInternetMessage();

        void scrollToBottom();

        void showStatusUpdatedMessage();

        void closeScreenWithResult(Signal signal);

        void setShadowVisibility(boolean visibility);

        boolean isActive();

        void onStatusChangeRequestFinished(boolean success, int newStatus);

        void openSignalPhotoScreen();

        void editSignalDescription();

        void deleteSignal();

        void openCommentPhotoScreen(String photoUrl);

        void setThumbnailToCommentPhotoButton(String photoUri);
    }

    interface UserActionsListener {

        void onInitDetailsScreen(Signal signal);

        void loadCommentsForSignal(String signalId);

        void onChooseCommentPhotoIconClicked();

        void onTryToAddComment();

        void onAddCommentButtonClicked(String comment);

        void onRequestStatusChange(int status);

        void onUpdateTitle(String title);

        void onDeleteSignal();

        void onCallButtonClicked();

        void onSignalDetailsClosing();

        void onBottomReached(boolean isBottomReached);

        void onSignalPhotoClicked();

        void onCommentPhotoClicked(String photoUrl);

        void onUploadSignalPhotoClicked();

        void onChangeSignalDescriptionClicked();

        void onDeleteSignalClicked();
    }
}
