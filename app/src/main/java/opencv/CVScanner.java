package opencv;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;

import opencv.crop.CropImageActivity;

public final class CVScanner {
    public static String RESULT_IMAGE_PATH = "result_image_path";

    public static String getFileProviderName(Context context) {
        return context.getPackageName() + ".cvscanner.fileprovider";
    }

    public static void startManualCropper(Activity activity, Uri inputImageUri, int reqCode) {
        Intent intent = new Intent(activity, CropImageActivity.class);

        intent.putExtra(CropImageActivity.EXTRA_IMAGE_URI, inputImageUri.toString());
        activity.startActivityForResult(intent, reqCode);
    }

    public interface ImageProcessorCallback {
        void onImageProcessingFailed(String reason, @Nullable Exception error);

        void onImageProcessed(String imagePath);
    }
}
