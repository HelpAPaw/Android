package org.helpapaw.helpapaw.data.repositories;

/**
 * Created by iliyan on 8/1/16
 */
public interface PhotoRepository {

    void savePhoto(String photoUri, String photoName, SavePhotoCallback callback);

    String getPhotoUrl(String signalId);


    interface SavePhotoCallback {

        void onPhotoSaved();

        void onPhotoFailure(String message);
    }
}
