package org.helpapaw.helpapaw.photo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.sendsignal.SendPhotoBottomSheet;
import org.helpapaw.helpapaw.utils.Injection;
import org.helpapaw.helpapaw.utils.images.ImageUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;

/**
 * Created by milen on 05/03/18.
 *
 */

public interface UploadPhotoContract {
    interface View {

        int REQUEST_CAMERA = 2;
        int REQUEST_GALLERY = 3;
        int READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY = 5;

        default void showSendPhotoBottomSheet(UserActionsListener actionsListener) {
            SendPhotoBottomSheet sendPhotoBottomSheet = new SendPhotoBottomSheet();
            sendPhotoBottomSheet.setListener(new SendPhotoBottomSheet.PhotoTypeSelectListener() {
                @Override
                public void onPhotoTypeSelected(@SendPhotoBottomSheet.PhotoType int photoType) {
                    if (photoType == SendPhotoBottomSheet.PhotoType.CAMERA) {
                        actionsListener.onCameraOptionSelected();
                    } else if (photoType == SendPhotoBottomSheet.PhotoType.GALLERY) {
                        actionsListener.onGalleryOptionSelected();
                    }
                }
            });
            sendPhotoBottomSheet.show(getFragmentManager(), SendPhotoBottomSheet.TAG);
        }

        default File openCamera() {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                File photoFile = ImageUtils.getInstance().createPhotoFile(PawApplication.getContext());
                Uri photoUri = FileProvider.getUriForFile(PawApplication.getContext(),
                        "org.helpapaw.helpapaw.fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                getFragment().startActivityForResult(intent, REQUEST_CAMERA);
                return photoFile;
            } catch (Exception e) {
                Injection.getCrashLogger().recordException(e);
            }

            return null;
        }

        default void openGallery() {
            Context context = getFragment().getContext();

            String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                getFragment().requestPermissions(permissions, READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY);
            }
            else{
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                try {
                    getFragment().startActivityForResult(intent, REQUEST_GALLERY);
                } catch (Exception e) {
                    Injection.getCrashLogger().recordException(e);
                }
            }
        }

        default void saveImageFromUri(UploadPhotoContract.UserActionsListener actionsListener, Uri photoUri) {
            Injection.getCrashLogger().log("Entering saveImageFromUri, photoUri is: " + photoUri.toString());

            Context context = getFragment().getActivity();

            // This segment works once the permission is handled
            try {
                Bitmap photo = MediaStore.Images.Media.getBitmap(context.getContentResolver(), photoUri);

                int rotation = ImageUtils.getInstance().getRotationFromMediaUri(context, photoUri);
                photo = ImageUtils.getInstance().getRotatedBitmap(photo, rotation);

                //https://stackoverflow.com/questions/58539583/android-q-get-image-from-gallery-and-process-it
                // Weird decoding and reparsing because the path is sometimes encoded like this:
                // content://com.miui.gallery.open/raw/%2Fstorage%2Femulated%2F0%2FDCIM%2FCamera%2FIMG_20220316_225127.jpg
                String lastSegment = photoUri.getLastPathSegment();
                String filename = Uri.parse(Uri.decode(lastSegment)).getLastPathSegment();
                File dir = context.getCacheDir();
                File dest = new File(dir, filename);
                Injection.getCrashLogger().log("destination is: " + dest);
                FileOutputStream out = new FileOutputStream(dest);
                photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                actionsListener.onSignalPhotoSelected(dest);
            }
            catch (Exception e) {
                Injection.getCrashLogger().recordException(e);
            }
        }

        Fragment getFragment();

        FragmentManager getFragmentManager();
    }

    interface UserActionsListener {

        void onCameraOptionSelected();

        void onGalleryOptionSelected();

        void onSignalPhotoSelected(File photoFile);
    }
}
