package com.gusya.mv.simpleadid;

import android.os.Handler;
import android.os.Looper;

/**
 * Executes Runnable on UI Thread
 */
public class UiThreadExecutor implements IExecutor {

    private final Handler mHandler;
    public UiThreadExecutor() {
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void post(Runnable runnable) {
        mHandler.post(runnable);
    }
}
