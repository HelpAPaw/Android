package org.helpapaw.helpapaw.data.repositories;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import org.helpapaw.helpapaw.base.PawApplication;

import java.io.IOException;

/**
 * Created by iliyan on 8/1/16
 */
public class BackendlessPhotoRepository implements PhotoRepository {

    private final static String PHOTO_EXTENSION = ".jpg";
    private final static String PHOTOS_DIRECTORY = "signal_photos";

    @Override
    public void savePhoto(String photoUri, String photoName, final SavePhotoCallback callback) {
        Bitmap photo = null;
        try {
            photo = MediaStore.Images.Media.getBitmap(PawApplication.getContext().getContentResolver(), Uri.parse(photoUri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Backendless.Files.Android.upload(photo,
                Bitmap.CompressFormat.JPEG, 30, photoName + PHOTO_EXTENSION, PHOTOS_DIRECTORY, true, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(final BackendlessFile backendlessFile) {
                        callback.onPhotoSaved();
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        callback.onPhotoFailure(backendlessFault.getMessage());
                    }
                });
    }

    @Override
    public String getPhotoUrl(String signalId) {
        return "https://api.backendless.com/" +
                PawApplication.YOUR_APP_ID + "/" +
                PawApplication.YOUR_APP_VERSION + "/files/" +
                PHOTOS_DIRECTORY + "/" + signalId + PHOTO_EXTENSION;
    }


}
