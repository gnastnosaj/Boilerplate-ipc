package com.github.gnastnosaj.boilerplate.ipc.sample;

import com.github.gnastnosaj.boilerplate.ipc.IPCService;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCMiddleware;
import com.github.gnastnosaj.boilerplate.ipc.middleware.sample.SampleMiddleware;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jasontsang on 1/17/18.
 */

public class IPCSampleMiddlewareProvider extends IPCService.IPCMiddlewareProvider {
    @Override
    protected List<IPCMiddleware> getIPCMiddlewares() {
        List<IPCMiddleware> middlewares = new ArrayList<>();
        middlewares.add(new SampleMiddleware());
        return middlewares;
    }
}
