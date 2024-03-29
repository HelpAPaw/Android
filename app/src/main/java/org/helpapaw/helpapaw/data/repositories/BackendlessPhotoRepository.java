package org.helpapaw.helpapaw.data.repositories;

import android.graphics.Bitmap;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.images.ImageUtils;

import java.io.File;

/**
 * Created by iliyan on 8/1/16
 */
public class BackendlessPhotoRepository implements PhotoRepository {

    private static final String BACKENDLESS_API_DOMAIN = "https://backendlessappcontent.com/";
    private static final String FILES_FOLDER = "files";
    private final static String SIGNAL_PHOTOS_DIRECTORY = "signal_photos";
    private final static String COMMENT_PHOTOS_DIRECTORY = "comment_photos";
    private final static String PHOTO_EXTENSION = ".jpg";

    private final static int PHOTO_QUALITY = 60;

    @Override
    public void saveSignalPhoto(File photoFile, String photoName, final SavePhotoCallback callback) {
        Bitmap photo = ImageUtils.getInstance().getRotatedBitmap(photoFile);
        Backendless.Files.Android.upload(photo,
                Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, photoName + PHOTO_EXTENSION,
                SIGNAL_PHOTOS_DIRECTORY, true, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(final BackendlessFile backendlessFile) {
                        callback.onPhotoSaved(backendlessFile.getFileURL());
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                        callback.onPhotoFailure(fault.getMessage());
                    }
                });
    }

    @Override
    public void saveCommentPhoto(File photoFile, String photoName, final SavePhotoCallback callback) {
        Bitmap photo = ImageUtils.getInstance().getRotatedBitmap(photoFile);
        Backendless.Files.Android.upload(photo,
                Bitmap.CompressFormat.JPEG, PHOTO_QUALITY, photoName + PHOTO_EXTENSION,
                COMMENT_PHOTOS_DIRECTORY, true, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(final BackendlessFile backendlessFile) {
                        callback.onPhotoSaved(backendlessFile.getFileURL());
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                        callback.onPhotoFailure(fault.getMessage());
                    }
                });
    }

    @Override
    public String getSignalPhotoUrl(String signalId) {
        if (signalId != null) {
            //https://api.backendless.com/<application id>/<REST-api-key>/files/<path>/<file name>
            return BACKENDLESS_API_DOMAIN +
                    PawApplication.getContext().getResources().getString(R.string.BACKENDLESS_APP_ID) + "/" +
                    PawApplication.getContext().getResources().getString(R.string.BACKENDLESS_REST_API_KEY) + "/" +
                    FILES_FOLDER + "/" +
                    SIGNAL_PHOTOS_DIRECTORY + "/" +
                    signalId +
                    PHOTO_EXTENSION;
        } else {
            return null;
        }
    }

    @Override
    public void signalPhotoExists(String signalId, PhotoExistsCallback callback) {

        String pattern = signalId + PHOTO_EXTENSION;

        Backendless.Files.getFileCount(SIGNAL_PHOTOS_DIRECTORY, pattern, new AsyncCallback<Integer>() {
            @Override
            public void handleResponse(Integer response) {
                callback.onPhotoExistsSuccess(response > 0);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Injection.getCrashLogger().recordException(new Throwable(fault.toString()));
                callback.onPhotoExistsFailure(fault.getMessage());
            }
        });
    }
}
