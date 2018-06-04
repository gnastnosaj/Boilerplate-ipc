package com.github.gnastnosaj.boilerplate.ipc.middleware;

/**
 * Created by jasontsang on 1/17/18.
 */

public interface IPCMiddlewareCallback {
    void perform(String data);
    void end();
}
