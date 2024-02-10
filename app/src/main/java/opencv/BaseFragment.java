package opencv;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Point;

import opencv.util.ImageSaveTask;

public abstract class BaseFragment extends Fragment implements ImageSaveTask.SaveCallback {

    protected boolean isBusy = false;
    protected CVScanner.ImageProcessorCallback mCallback = null;
    private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS) {
                onOpenCVConnected();
            } else {
                onOpenCVConnectionFailed();
            }
        }
    };

    protected void loadOpenCV() {
        if (OpenCVLoader.initDebug()) {
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    protected abstract void onOpenCVConnected();

    protected abstract void onOpenCVConnectionFailed();

    protected abstract void onAfterViewCreated();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        onAfterViewCreated();
        loadOpenCV();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if (context instanceof CVScanner.ImageProcessorCallback) {
            mCallback = (CVScanner.ImageProcessorCallback) context;
        }
    }

    @Override
    public void onSaveTaskStarted() {
        isBusy = true;
    }

    @Override
    public void onSaved(String path) {
        Log.d("BASE", "saved at: " + path);
        if (mCallback != null) mCallback.onImageProcessed(path);
        isBusy = false;
    }

    @Override
    public void onSaveFailed(Exception error) {
        if (mCallback != null) mCallback.onImageProcessingFailed("Failed to save image", error);
        isBusy = false;
    }

    protected synchronized void saveCroppedImage(Bitmap bitmap, int rotation, Point[] quadPoints) {
        if (!isBusy) {
            new ImageSaveTask(getContext(), bitmap, rotation, quadPoints, this)
                    .execute();
        }
    }
}
