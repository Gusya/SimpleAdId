package com.gusya.mv.simpleadid;

import android.content.Context;

/**
 *<p>
 *     Defines a way to execute {@link com.gusya.mv.simpleadid.SimpleAdId.SimpleAdListener} callback methods.
 *     Provide it's implementation to {@link com.gusya.mv.simpleadid.SimpleAdId#getAdInfo(Context, IExecutor, SimpleAdId.SimpleAdListener)}
 *</p>
 */
public interface IExecutor {
    void post(Runnable runnable);
}
