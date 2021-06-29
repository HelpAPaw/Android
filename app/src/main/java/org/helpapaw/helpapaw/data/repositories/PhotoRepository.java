package org.helpapaw.helpapaw.data.repositories;

/**
 * Created by iliyan on 8/1/16
 */
public interface PhotoRepository {

    void saveSignalPhoto(String photoUri, String photoName, SavePhotoCallback callback);

    void saveCommentPhoto(String commentId, String photoUri, String photoName, SavePhotoCallback callback);

    String getSignalPhotoUrl(String signalId);

    String getCommentPhotoUrl(String commentId);

    void signalPhotoExists(String signalId, PhotoExistsCallback callback);

    void commentPhotoExists(String commentId, PhotoExistsCallback callback);


    interface SavePhotoCallback {

        void onPhotoSaved(String photoUrl);

        void onPhotoFailure(String message);
    }

    interface PhotoExistsCallback {

        void onPhotoExistsSuccess(boolean photoExists);
        
        void onPhotoExistsFailure(String message);
    }
}
