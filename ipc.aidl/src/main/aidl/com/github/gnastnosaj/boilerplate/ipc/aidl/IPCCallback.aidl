package com.github.gnastnosaj.boilerplate.ipc.aidl;

import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCException;

interface IPCCallback {
    void onNext(String next);
    void onComplete();
    void onError(in IPCException e);
}
