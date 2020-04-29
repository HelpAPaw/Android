package org.helpapaw.helpapaw.utils.images;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by iliyan on 8/9/16
 */
public class ImageUtils {

    private static final String SCHEME_FILE = "file";
    private static final String SCHEME_CONTENT = "content";

    private static ImageUtils instance;

    public synchronized static ImageUtils getInstance() {
        if (instance == null) {
            instance = new ImageUtils();
        }
        return instance;
    }

    public void closeSilently(@Nullable Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable t) {
            // Do nothing
        }
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

    public int getExifRotation(File imageFile) {
        if (imageFile == null) return 0;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            // We only recognize a subset of orientation tag values
            return exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

        } catch (IOException e) {
            //  Log.e("Error getting Exif data", e);
            return 0;
        }
    }

    public Bitmap getRotatedBitmap(File src) {
        Bitmap bitmap;


        bitmap = decodeFile(src, 500, 500);
        int orientation = getExifRotation(src);

        if (orientation == 1) {
            return bitmap;
        }

        Matrix matrix = new Matrix();
        switch (orientation) {
            case 2:
                matrix.setScale(-1, 1);
                break;
            case 3:
                matrix.setRotate(180);
                break;
            case 4:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case 5:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case 6:
                matrix.setRotate(90);
                break;
            case 7:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case 8:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }

        try {
            Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return oriented;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    public Bitmap decodeFile(File f, int WIDTH, int HEIGHT) {
        try {
            //Decode image size
            BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
            bitmapOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, bitmapOptions);

            int scale = 1;
            while (bitmapOptions.outWidth / scale / 2 >= WIDTH && bitmapOptions.outHeight / scale / 2 >= HEIGHT)
                scale *= 2;

            BitmapFactory.Options bitmapNewOptions = new BitmapFactory.Options();
            bitmapNewOptions.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, bitmapNewOptions);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    @Nullable
    public File getFromMediaUri(Context context, ContentResolver resolver, Uri uri) {
        if (uri == null) return null;

        if (SCHEME_FILE.equals(uri.getScheme())) {
            return new File(uri.getPath());
        } else if (SCHEME_CONTENT.equals(uri.getScheme())) {
            final String[] filePathColumn = {MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME};
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, filePathColumn, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex;
                    if (uri.toString().startsWith("content://com.google.android.gallery3d")) {
                        columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME);
                    } else {
                        columnIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DATA);
                    }
                    if (columnIndex != -1) {
                        String filePath = cursor.getString(columnIndex);
                        if (!TextUtils.isEmpty(filePath)) {
                            return new File(filePath);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // Google Drive images
                return getFromMediaUriPfd(context, resolver, uri);
            } catch (SecurityException ignored) {
                // Nothing we can do
            } finally {
                if (cursor != null) cursor.close();
            }
        }
        return null;
    }

    private String getTempFilename(Context context) throws IOException {
        File outputDir = context.getCacheDir();
        File outputFile = File.createTempFile("image", "tmp", outputDir);
        return outputFile.getAbsolutePath();
    }

    @Nullable
    private File getFromMediaUriPfd(Context context, ContentResolver resolver, Uri uri) {
        if (uri == null) return null;

        FileInputStream input = null;
        FileOutputStream output = null;
        try {
            ParcelFileDescriptor pfd = resolver.openFileDescriptor(uri, "r");
            FileDescriptor fd = pfd.getFileDescriptor();
            input = new FileInputStream(fd);

            String tempFilename = getTempFilename(context);
            output = new FileOutputStream(tempFilename);

            int read;
            byte[] bytes = new byte[4096];
            while ((read = input.read(bytes)) != -1) {
                output.write(bytes, 0, read);
            }
            return new File(tempFilename);
        } catch (IOException ignored) {
            // Nothing we can do
        } finally {
            closeSilently(input);
            closeSilently(output);
        }
        return null;
    }

    // Returns true if external storage for photos is available
    private boolean isExternalStorageAvailable() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }
}
