package com.qonak.app.utils;

import android.os.Handler;
import android.os.Looper;

import androidx.fragment.app.Fragment;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Shared executors for the whole app.
 *
 * All database work runs on ONE background thread, so tasks run strictly in the
 * order they were submitted (e.g. seeding always finishes before the first query).
 */
public final class AppExecutors {

    private static final ExecutorService DB = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private AppExecutors() {
    }

    /** Background executor for Room queries. */
    public static Executor db() {
        return DB;
    }

    /** Runs the action on the main thread. */
    public static void main(Runnable action) {
        MAIN.post(action);
    }

    /**
     * Runs the action on the main thread only if the fragment still has a view.
     * Prevents crashes when the user leaves the screen before a query finishes.
     */
    public static void onUi(Fragment fragment, Runnable action) {
        MAIN.post(() -> {
            if (fragment.isAdded() && fragment.getView() != null) {
                action.run();
            }
        });
    }
}
