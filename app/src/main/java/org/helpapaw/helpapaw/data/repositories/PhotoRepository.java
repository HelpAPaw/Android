package org.helpapaw.helpapaw.data.repositories;

import java.io.File;

/**
 * Created by iliyan on 8/1/16
 */
public interface PhotoRepository {

    void saveSignalPhoto(File photoFile, String photoName, SavePhotoCallback callback);

    void saveCommentPhoto(File photoFile, String photoName, SavePhotoCallback callback);

    String getSignalPhotoUrl(String signalId);

    void signalPhotoExists(String signalId, PhotoExistsCallback callback);


    interface SavePhotoCallback {

        void onPhotoSaved(String photoUrl);

        void onPhotoFailure(String message);
    }

    interface PhotoExistsCallback {

        void onPhotoExistsSuccess(boolean photoExists);
        
        void onPhotoExistsFailure(String message);
    }
}
