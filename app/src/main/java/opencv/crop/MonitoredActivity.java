package opencv.crop;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public abstract class MonitoredActivity extends AppCompatActivity {

    private static final String LOG_TAG = MonitoredActivity.class.getSimpleName();

    private final ArrayList<LifeCycleListener> mListeners = new ArrayList<>();

    @Override
    protected synchronized void onPause() {
        super.onPause();
        final ArrayList<LifeCycleListener> lifeCycleListeners = copyListeners();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onActivityPaused(this);
        }
    }

    private ArrayList<LifeCycleListener> copyListeners() {
        final ArrayList<LifeCycleListener> lifeCycleListeners = new ArrayList<>(mListeners.size());
        lifeCycleListeners.addAll(mListeners);
        return lifeCycleListeners;
    }

    @Override
    protected synchronized void onResume() {
        super.onResume();
        for (LifeCycleListener listener : mListeners) {
            listener.onActivityResumed(this);
        }
    }

    @Override
    protected synchronized void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ArrayList<LifeCycleListener> lifeCycleListeners = copyListeners();

        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onActivityCreated(this);
        }
    }

    @Override
    protected synchronized void onDestroy() {
        super.onDestroy();

        final ArrayList<LifeCycleListener> lifeCycleListeners = copyListeners();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onActivityDestroyed(this);
        }
    }

    @Override
    protected synchronized void onStart() {
        super.onStart();
        final ArrayList<LifeCycleListener> lifeCycleListeners = copyListeners();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onActivityStarted(this);
        }
    }

    @Override
    protected synchronized void onStop() {
        super.onStop();
        final ArrayList<LifeCycleListener> lifeCycleListeners = copyListeners();
        for (LifeCycleListener listener : lifeCycleListeners) {
            listener.onActivityStopped(this);
        }
        Log.i(LOG_TAG, "onStop: " + this.getClass());
    }

    public interface LifeCycleListener {
        void onActivityCreated(MonitoredActivity activity);

        void onActivityDestroyed(MonitoredActivity activity);

        void onActivityPaused(MonitoredActivity activity);

        void onActivityResumed(MonitoredActivity activity);

        void onActivityStarted(MonitoredActivity activity);

        void onActivityStopped(MonitoredActivity activity);
    }

}
