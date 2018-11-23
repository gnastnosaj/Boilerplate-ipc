package com.github.gnastnosaj.boilerplate.ipc.middleware;

import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCResponse;

/**
 * Created by jasontsang on 1/17/18.
 */

public interface IPCMiddlewareCallback {
    void onNext(IPCResponse data);

    void onError(Throwable throwable);

    void onComplete();
}
