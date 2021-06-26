package org.helpapaw.helpapaw.data.repositories;

import android.graphics.Bitmap;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.utils.images.ImageUtils;

import java.io.File;

/**
 * Created by iliyan on 8/1/16
 */
public class BackendlessPhotoRepository implements PhotoRepository {

    private static final String BACKENDLESS_API_DOMAIN = "https://backendlessappcontent.com/";
    private static final String FILES_FOLDER = "files";
    private final static String PHOTOS_DIRECTORY = "signal_photos";
    private final static String COMMENT_PHOTOS_DIRECTORY = "comment_photos";
    private final static String PHOTO_EXTENSION = ".jpg";

    private final static int PHOTO_QUALITY = 60;

    @Override
    public void saveSignalPhoto(String photoUri, String photoName, final SavePhotoCallback callback) {
        savePhoto(photoUri, photoName, PHOTOS_DIRECTORY, callback);
    }

    @Override
    public void saveCommentPhoto(String photoUri, String photoName, final SavePhotoCallback callback) {
        savePhoto(photoUri, photoName, COMMENT_PHOTOS_DIRECTORY, callback);
    }

    private void savePhoto(String photoUri, String photoName, String directory, final SavePhotoCallback callback) {
        Bitmap photo = ImageUtils.getInstance().getRotatedBitmap(new File(photoUri));
        Backendless.Files.Android.upload(photo,
                Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, photoName + PHOTO_EXTENSION,
                directory, true, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(final BackendlessFile backendlessFile) {
                        callback.onPhotoSaved(backendlessFile.getFileURL());
                    }

                    @Override
                    public void handleFault(BackendlessFault backendlessFault) {
                        callback.onPhotoFailure(backendlessFault.getMessage());
                    }
                });
    }

    @Override
    public String getSignalPhotoUrl(String signalId) {
        return getPhotoUrl(signalId, PHOTOS_DIRECTORY);
    }

    @Override
    public String getCommentPhotoUrl(String commentId) {
        return getPhotoUrl(commentId, COMMENT_PHOTOS_DIRECTORY);
    }

    private String getPhotoUrl(String id, String directory) {
        if (id != null) {
            //https://api.backendless.com/<application id>/<REST-api-key>/files/<path>/<file name>
            return BACKENDLESS_API_DOMAIN +
                    PawApplication.getContext().getResources().getString(R.string.BACKENDLESS_APP_ID) + "/" +
                    PawApplication.getContext().getResources().getString(R.string.BACKENDLESS_REST_API_KEY) + "/" +
                    FILES_FOLDER + "/" +
                    directory + "/" +
                    id +
                    PHOTO_EXTENSION;
        } else {
            return null;
        }
    }

    @Override
    public void signalPhotoExists(String signalId, PhotoExistsCallback callback) {
        photoExists(signalId, PHOTOS_DIRECTORY, callback);
    }

    @Override
    public void commentPhotoExists(String commentId, PhotoExistsCallback callback) {
        photoExists(commentId, COMMENT_PHOTOS_DIRECTORY, callback);
    }

    private void photoExists(String id, String directory, PhotoExistsCallback callback) {

        String pattern = id + PHOTO_EXTENSION;

        Backendless.Files.getFileCount(directory, pattern, new AsyncCallback<Integer>() {
            @Override
            public void handleResponse(Integer response) {
                callback.onPhotoExistsSuccess(response > 0);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                callback.onPhotoExistsFailure(fault.getMessage());
            }
        });
    }
}
