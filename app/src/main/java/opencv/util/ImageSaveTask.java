package opencv.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.io.IOException;

@SuppressLint("StaticFieldLeak")
public class ImageSaveTask extends AsyncTask<Void, Void, String> {
    Bitmap image;
    int rotation;
    Point[] points;
    Context mContext;
    SaveCallback mCallback;

    public ImageSaveTask(Context context, Bitmap image, int rotation, Point[] points, SaveCallback callback) {
        this.image = image;
        this.rotation = rotation;
        this.points = points;
        this.mContext = context;
        this.mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        mCallback.onSaveTaskStarted();
    }

    @Override
    protected String doInBackground(Void... params) {
        Size imageSize = new Size(image.getWidth(), image.getHeight());
        Mat imageMat = new Mat(imageSize, CvType.CV_8UC4);
        Utils.bitmapToMat(image, imageMat);

        image.recycle();

        Mat croppedImage = CVProcessor.fourPointTransform(imageMat, points);
        imageMat.release();

        Mat enhancedImage = CVProcessor.adjustBrightnessAndContrast(croppedImage, 1);
        croppedImage.release();

        enhancedImage = CVProcessor.sharpenImage(enhancedImage);

        String imagePath = null;
        try {
            imagePath = Util.saveImage(mContext, "IMG_CVScanner_" + System.currentTimeMillis(), enhancedImage, false);
            enhancedImage.release();
            Util.setExifRotation(mContext, Util.getUriFromPath(imagePath), rotation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return imagePath;
    }

    @Override
    protected void onPostExecute(String path) {
        if (path != null) mCallback.onSaved(path);
        else mCallback.onSaveFailed(new Exception("could not save image"));
    }

    public interface SaveCallback {
        void onSaveTaskStarted();

        void onSaved(String savedPath);

        void onSaveFailed(Exception error);
    }
}