package org.helpapaw.helpapaw.photo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import org.helpapaw.helpapaw.base.PawApplication;
import org.helpapaw.helpapaw.sendsignal.SendPhotoBottomSheet;
import org.helpapaw.helpapaw.utils.images.ImageUtils;

import java.io.File;
import java.io.FileDescriptor;

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
            Context context = getFragment().getContext();

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                File photoFile = ImageUtils.getInstance().createPhotoFile(PawApplication.getContext());
                Uri photoUri = FileProvider.getUriForFile(PawApplication.getContext(),
                        "org.helpapaw.helpapaw.fileprovider",
                        photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                getFragment().startActivityForResult(intent, REQUEST_CAMERA);
                return photoFile;
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
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    getFragment().startActivityForResult(intent, REQUEST_GALLERY);
                }
            }
        }

        default void saveImageFromUri(UserActionsListener actionsListener, Uri photoUri) {
            Context context = getFragment().getActivity();

            // This segment works once the permission is handled
            try {
                String path;
                ParcelFileDescriptor parcelFileDesc = context.getContentResolver().openFileDescriptor(photoUri, "r");
                FileDescriptor fileDesc = parcelFileDesc.getFileDescriptor();
                Bitmap photo = BitmapFactory.decodeFileDescriptor(fileDesc);

                int rotation = ImageUtils.getInstance().getRotationFromMediaUri(context, photoUri);
                photo = ImageUtils.getInstance().getRotatedBitmap(photo, rotation);
                path = MediaStore.Images.Media.insertImage(context.getContentResolver(), photo, "temp", null);
                File photoFile = ImageUtils.getInstance().getFileFromMediaUri(context, context.getContentResolver(), Uri.parse(path));

                if (photoFile != null) {
                    actionsListener.onSignalPhotoSelected(photoFile);
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

        void onSignalPhotoSelected(File photoFile);
    }
}
