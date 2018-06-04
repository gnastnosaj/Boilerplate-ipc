package com.github.gnastnosaj.boilerplate.ipc.middleware;

import android.content.Context;

/**
 * Created by jasontsang on 1/17/18.
 */

public interface IPCMiddleware {
    void initialize(Context context, IPCEventBus eventBus);

    boolean accept(String scheme);

    void exec(String scheme, String data, IPCMiddlewareCallback callback);
}
