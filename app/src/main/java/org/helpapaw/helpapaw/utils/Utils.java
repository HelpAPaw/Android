package org.helpapaw.helpapaw.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.location.Location;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import org.helpapaw.helpapaw.R;
import org.helpapaw.helpapaw.base.PawApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by iliyan on 7/25/16
 */
public class Utils {

    private static Utils instance;

    public synchronized static Utils getInstance() {
        if (instance == null) {
            instance = new Utils();
        }
        return instance;
    }

    //Network
    public boolean hasNetworkConnection() {
        ConnectivityManager connectivity = (ConnectivityManager) PawApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    //Files
    public Uri getPhotoFileUri(Context context, String fileName) {
        String APP_TAG = "HelpAPaw";
        // Only continue if the SD Card is mounted
        if (isExternalStorageAvailable()) {
            // Get safe storage directory for photos
            // Use `getExternalFilesDir` on Context to access package-specific directories.
            // This way, we don't need to request external read/write runtime permissions.
            File mediaStorageDir = new File(
                    context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), APP_TAG);

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists() && !mediaStorageDir.mkdirs()) {
                Log.d(APP_TAG, "failed to create directory");
            }

            // Return the file target for the photo based on filename
            return Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator + fileName));
        }
        return null;
    }

    private int getEXIFOrientation(String imageUri) {
        ExifInterface ei = null;
        try {
            ei = new ExifInterface(imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        int orientation = 0;
        if (ei != null) {
            orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        }

        return orientation;
    }

    public boolean isRotationNeeded(String imageUri) {
        return getEXIFOrientation(imageUri) != ExifInterface.ORIENTATION_NORMAL;
    }

    public Bitmap fixBitmapOrientation(Bitmap bitmap, String selectedImage) {
        switch (getEXIFOrientation(selectedImage)) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateImage(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateImage(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateImage(bitmap, 270);
            default:
                return bitmap;
        }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }

    public File saveImageToExternalStorage(Bitmap bitmap, Uri fileUri) {
        File file = new File(fileUri.toString());
        if (file.exists())
            file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public Bitmap getBitmapFromUri(Context context, String photoUri) {
        Bitmap selectedImage = null;
        if (photoUri != null) {
            try {
                selectedImage = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(photoUri));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return selectedImage;
    }

    //Location
    public float getDistanceBetween(double latitudePointOne, double longitudePointOne,
                                    double latitudePointTwo, double longitudePointTwo) {
        Location pointOne = new Location("");
        pointOne.setLatitude(latitudePointOne);
        pointOne.setLongitude(longitudePointOne);

        Location pointTwo = new Location("");
        pointTwo.setLatitude(latitudePointTwo);
        pointTwo.setLongitude(longitudePointTwo);

        return pointOne.distanceTo(pointTwo);
    }


    //Permissions
    public void showPermissionDialog(Activity activity, String permission, int permissionCode) {
        if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                Toast.makeText(activity, R.string.permission_not_granted, Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(activity, new String[]{permission},
                        permissionCode);
            }
        }
    }
}
