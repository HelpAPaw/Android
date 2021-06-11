package org.helpapaw.helpapaw.photo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.helpapaw.helpapaw.sendsignal.SendPhotoBottomSheet;
import org.helpapaw.helpapaw.utils.images.ImageUtils;

import java.io.File;
import java.io.FileDescriptor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by milen on 05/03/18.
 *
 */

public interface UploadPhotoContract {
    interface View {

        int REQUEST_CAMERA = 2;
        int REQUEST_GALLERY = 3;
        int READ_WRITE_EXTERNAL_STORAGE_FOR_GALLERY = 5;
        String IMAGE_FILENAME = "NewSignalPhoto.jpg";

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

        default void openCamera() {
            Context context = getFragment().getContext();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, ImageUtils.getInstance().getPhotoFileUri(context, IMAGE_FILENAME));
                getFragment().startActivityForResult(intent, REQUEST_CAMERA);
            }
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
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    getFragment().startActivityForResult(intent, REQUEST_GALLERY);
                }
            }
        }

        default void saveImageFromURI(UserActionsListener actionsListener, Uri photoUri) {
            Context context = getFragment().getContext();
            Activity activity = getFragment().getActivity();

            // This segment works once the permission is handled
            try {
                String path;
                ParcelFileDescriptor parcelFileDesc = activity.getContentResolver().openFileDescriptor(photoUri, "r");
                FileDescriptor fileDesc = parcelFileDesc.getFileDescriptor();
                Bitmap photo = BitmapFactory.decodeFileDescriptor(fileDesc);
                path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "temp", null);
                File photoFile = ImageUtils.getInstance().getFromMediaUri(context, context.getContentResolver(), Uri.parse(path));

                if (photoFile != null) {
                    actionsListener.onSignalPhotoSelected(Uri.fromFile(photoFile).getPath());
                }

                parcelFileDesc.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Fragment getFragment();

        FragmentManager getFragmentManager();
    }

    interface UserActionsListener {

        void onCameraOptionSelected();

        void onGalleryOptionSelected();

        void onSignalPhotoSelected(String photoUri);
    }
}
