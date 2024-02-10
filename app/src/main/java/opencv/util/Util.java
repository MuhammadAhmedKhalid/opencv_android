package opencv.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import opencv.CVScanner;

public final class Util {
    private static final int SIZE_DEFAULT = 2048;
    private static final int SIZE_LIMIT = 4096;

    public static void closeSilently(Closeable c) {
        if (c == null) return;
        try {
            c.close();
        } catch (Throwable ignored) {
        }
    }

    public static Uri getUriForFile(Context context, File file) {
        return CVFileProvider.getUriForFile(context, CVScanner.getFileProviderName(context), file);
    }

    public static Uri getUriFromPath(String path) {
        File file = new File(path);
        return Uri.fromFile(file);
    }

    public static String saveImage(Context context, String imageName, @NonNull Mat img, boolean useExternalStorage) throws IOException {
        String imagePath;

        File dir;
        if (useExternalStorage) {
            dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        } else {
            dir = new File(context.getCacheDir(), "/CVScanner/");
        }

        if (!Objects.requireNonNull(dir).exists()) {
            dir.mkdirs();
        }

        File imageFile = File.createTempFile(imageName, ".jpg", dir);

        Bitmap bitmap = Bitmap.createBitmap((int) img.size().width, (int) img.size().height, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(img, bitmap);

        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();

            imagePath = imageFile.getAbsolutePath();
        } finally {
            closeSilently(fOut);
        }

        return imagePath;
    }

    public static int calculateBitmapSampleSize(Context context, Uri bitmapUri) throws IOException {

        BitmapFactory.Options options = decodeImageForSize(context, bitmapUri);

        int maxSize = getMaxImageSize();
        int sampleSize = 1;
        while (options.outHeight / sampleSize > maxSize || options.outWidth / sampleSize > maxSize) {
            sampleSize = sampleSize << 1;
        }

        return sampleSize;
    }

    private static BitmapFactory.Options decodeImageForSize(Context context, @NonNull Uri imageUri) throws FileNotFoundException {
        InputStream is = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            is = context.getContentResolver().openInputStream(imageUri);
            BitmapFactory.decodeStream(is, null, options); // Just get image size
        } finally {
            Util.closeSilently(is);
        }

        return options;
    }

    public static Bitmap loadBitmapFromUri(Context context, int sampleSize, Uri uri) throws FileNotFoundException {
        InputStream is = null;
        Bitmap out;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inSampleSize = sampleSize;

        try {
            is = context.getContentResolver().openInputStream(uri);
            out = BitmapFactory.decodeStream(is, null, options);
        } finally {
            Util.closeSilently(is);
        }

        return out;
    }

    public static int getExifRotation(Context context, Uri imageUri) throws IOException {
        if (imageUri == null) return 0;
        InputStream inputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(imageUri);
            ExifInterface exifInterface = new ExifInterface(Objects.requireNonNull(inputStream));
            // We only recognize a subset of orientation tag values
            int orientationValue = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            int orientation;
            switch (orientationValue) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;
                    break;
                default:
                    orientation = ExifInterface.ORIENTATION_UNDEFINED;
                    break;
            }
            return orientation;

        } finally {
            closeSilently(inputStream);
        }
    }

    public static void setExifRotation(Context context, Uri imageUri, int rotation) throws IOException {
        if (imageUri == null) return;

        InputStream destStream = null;
        try {
            destStream = context.getContentResolver().openInputStream(imageUri);

            ExifInterface exif = new ExifInterface(Objects.requireNonNull(destStream));

            exif.setAttribute("UserComment", "Generated using CVScanner");

            int orientation;
            switch (rotation) {
                case 1:
                    orientation = ExifInterface.ORIENTATION_ROTATE_90;
                    break;
                case 2:
                    orientation = ExifInterface.ORIENTATION_ROTATE_180;
                    break;
                case 3:
                    orientation = ExifInterface.ORIENTATION_ROTATE_270;
                    break;
                default:
                    orientation = ExifInterface.ORIENTATION_NORMAL;
                    break;
            }

            exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(orientation));
            exif.saveAttributes();
        } finally {
            closeSilently(destStream);
        }
    }

    private static int getMaxImageSize() {
        int textureLimit = getMaxTextureSize();
        if (textureLimit == 0) {
            return SIZE_DEFAULT;
        } else {
            return Math.min(textureLimit, SIZE_LIMIT);
        }
    }

    private static int getMaxTextureSize() {
        // The OpenGL texture size is the maximum size that can be drawn in an ImageView
        int[] maxSize = new int[1];
        GLES10.glGetIntegerv(GLES10.GL_MAX_TEXTURE_SIZE, maxSize, 0);
        return maxSize[0];
    }
}

