package ru.mail.auth.sdk;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;

public class MRExecutors {

    private final Executor mNetworkIO;

    private final Executor mMainThread;

    private final Executor mIPC;

    private final Executor mSingleThreadExecutor;

    private static final MRExecutors sAppExecutors = new MRExecutors();

    private MRExecutors(Executor networkIO,
                        Executor ipc,
                        Executor mainThread,
                        Executor singleThreadExecutor) {
        mNetworkIO = networkIO;
        mIPC = ipc;
        mMainThread = mainThread;
        mSingleThreadExecutor = singleThreadExecutor;
    }

    private MRExecutors() {
        this(Executors.newCachedThreadPool(),
                Executors.newCachedThreadPool(),
                new MainThreadExecutor(),
                Executors.newSingleThreadExecutor());
    }

    private static MRExecutors getInstance() {
        return sAppExecutors;
    }

    public static Executor networkIO() {
        return getInstance().mNetworkIO;
    }

    public static Executor mainThread() {
        return getInstance().mMainThread;
    }

    public static Executor singleThread() {
        return getInstance().mSingleThreadExecutor;
    }

    public static Executor IPC() {
        return getInstance().mIPC;
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
