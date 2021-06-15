package org.helpapaw.helpapaw.data.repositories;

/**
 * Created by iliyan on 8/1/16
 */
public interface PhotoRepository {

    void savePhoto(String photoUri, String photoName, SavePhotoCallback callback);

    String getPhotoUrl(String signalId);

    void photoExists(String signalId, PhotoExistsCallback callback);


    interface SavePhotoCallback {

        void onPhotoSaved(String photoUrl);

        void onPhotoFailure(String message);
    }

    interface PhotoExistsCallback {

        void onPhotoExistsSuccess(boolean photoExists);
        
        void onPhotoExistsFailure(String message);
    }
}
