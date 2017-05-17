package com.gusya.mv.simpleadid;

/**
 * Executes Runnable on current thread
 */
public class CurrentThreadExecutor implements IExecutor {

    public CurrentThreadExecutor() {
        //nothing special to construct
    }

    @Override
    public void post(Runnable runnable) {
        runnable.run();
    }
}
