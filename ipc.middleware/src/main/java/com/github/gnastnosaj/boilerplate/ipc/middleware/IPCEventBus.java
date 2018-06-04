package com.github.gnastnosaj.boilerplate.ipc.middleware;

/**
 * Created by jasontsang on 1/17/18.
 */

public interface IPCEventBus {
    void send(IPCEvent event);

    void post(String tag, IPCEvent event);
}
