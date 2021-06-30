package org.helpapaw.helpapaw.data.repositories;

import java.io.File;

/**
 * Created by iliyan on 8/1/16
 */
public interface PhotoRepository {

    void savePhoto(File photoFile, String photoName, SavePhotoCallback callback);

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
