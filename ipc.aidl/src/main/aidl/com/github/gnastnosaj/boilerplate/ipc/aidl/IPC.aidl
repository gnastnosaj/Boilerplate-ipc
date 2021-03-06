package com.github.gnastnosaj.boilerplate.ipc.aidl;

import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCRequest;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCCallback;

interface IPC {
    void exec(String scheme, in IPCRequest data, IPCCallback callback);

    void subscribe(IPCCallback callback);

    void dispose(IPCCallback callback);

    void register(String tag, IPCCallback callback);

    void unregister(String tag, IPCCallback callback);
}
