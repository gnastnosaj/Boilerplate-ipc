package com.github.gnastnosaj.boilerplate.ipc.middleware.sample;

import android.content.Context;

import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCRequest;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCResponse;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCEvent;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCEventBus;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCMiddleware;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCMiddlewareCallback;

/**
 * Created by jasontsang on 1/17/18.
 */

public class SampleMiddleware implements IPCMiddleware {
    private IPCEventBus eventBus;

    @Override
    public void initialize(Context context, IPCEventBus eventBus) {
        this.eventBus = eventBus;
    }

    @Override
    public boolean accept(String scheme) {
        return scheme.equals("com.github.gnastnosaj.boilerplate.ipc.middleware.sample");
    }

    @Override
    public void exec(String scheme, IPCRequest data, IPCMiddlewareCallback callback) {
        callback.onNext(new IPCResponse(scheme));

        new Thread(() -> {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            callback.onNext(new IPCResponse(new SampleParcelable(data.getArgs()[0].toString())));
            callback.onComplete();
        }).start();

        new Thread(() -> {
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            eventBus.post("sample", new IPCEvent() {
                @Override
                public String toString() {
                    return "sample ipc event";
                }
            });
        }).start();
    }
}
