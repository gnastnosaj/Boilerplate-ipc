package com.github.gnastnosaj.boilerplate.ipc.sdk;

import android.app.Application;
import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.gnastnosaj.boilerplate.ipc.aidl.IPC;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCCallback;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCException;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCRequest;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCResponse;
import com.github.gnastnosaj.boilerplate.rxbus.RxHelper;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;

/**
 * Created by jasontsang on 1/17/18.
 */

public class IPCSDK {

    private static Application application;

    private static boolean bind;

    private static CountDownLatch instanceLatch;

    private static IPC ipc;

    private static IPCSDK instance;

    private static void initialize(Application application) {
        IPCSDK.application = application;

        bind = bindService();
    }

    public static IPCSDK getInstance() {
        if (instance == null) {
            instance = new IPCSDK();
        }

        return instance;
    }

    public Observable<IPCResponse> exec(String scheme, IPCRequest data) {
        return Observable.<IPCResponse>create(subscriber -> exec(scheme, data, new IPCRxCallback(subscriber))).compose(RxHelper.rxSchedulerHelper());
    }

    public Observable<Callback> register(Callback callback) {
        return Observable.<Callback>create(subscriber -> {
            subscribe(callback);

            subscriber.onNext(callback);
            subscriber.onComplete();
        }).compose(RxHelper.rxSchedulerHelper());
    }

    public Observable<Callback> register(String tag, Callback callback) {
        return Observable.<Callback>create(subscriber -> {
            subscribe(tag, callback);

            subscriber.onNext(callback);
            subscriber.onComplete();
        }).compose(RxHelper.rxSchedulerHelper());
    }

    public Observable<Callback> unregister(String tag, Callback callback) {
        return Observable.<Callback>create(subscriber -> {
            dispose(tag, callback);

            subscriber.onNext(callback);
            subscriber.onComplete();
        }).compose(RxHelper.rxSchedulerHelper());
    }

    public Observable<Callback> unregister(Callback callback) {
        return Observable.<Callback>create(subscriber -> {
            dispose(callback);

            subscriber.onNext(callback);
            subscriber.onComplete();
        }).compose(RxHelper.rxSchedulerHelper());
    }

    @Deprecated
    public void exec(String scheme, IPCRequest data, IPCCallback.Stub callback) throws IPCInMainThreadException, ServiceNotConnectedException, RemoteException {
        ensure();

        ipc.exec(scheme, data, callback);
    }

    @Deprecated
    public IPCCallback.Stub subscribe(IPCCallback.Stub callback) throws IPCInMainThreadException, ServiceNotConnectedException, RemoteException {
        ensure();

        ipc.subscribe(callback);

        return callback;
    }

    @Deprecated
    public IPCCallback.Stub subscribe(String tag, IPCCallback.Stub callback) throws IPCInMainThreadException, ServiceNotConnectedException, RemoteException {
        ensure();

        ipc.register(tag, callback);

        return callback;
    }

    @Deprecated
    public void dispose(IPCCallback.Stub callback) throws IPCInMainThreadException, ServiceNotConnectedException, RemoteException {
        ensure();

        ipc.dispose(callback);
    }

    @Deprecated
    public void dispose(String tag, IPCCallback.Stub callback) throws IPCInMainThreadException, ServiceNotConnectedException, RemoteException {
        ensure();

        ipc.unregister(tag, callback);
    }

    private static void ensure() throws IPCInMainThreadException, ServiceNotConnectedException {
        if (Looper.getMainLooper().getThread() == Thread.currentThread()) {
            throw new IPCInMainThreadException();
        }

        try {
            ensureService();
        } catch (ServiceNotConnectedException e) {
            if (!bind) {
                bind = bindService();
            }

            throw e;
        }
    }

    private static void ensureService() throws ServiceNotConnectedException {
        if (instanceLatch != null) {
            try {
                instanceLatch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (ipc == null) {
            throw new ServiceNotConnectedException();
        }
    }

    private static synchronized boolean bindService() {

        instanceLatch = new CountDownLatch(1);

        Intent intent = new Intent();

        try {
            Resources res = application.getResources();

            intent.setPackage(res.getString(res.getIdentifier("ipc_service_package_name", "string", application.getPackageName())));
            intent.setAction(res.getString(res.getIdentifier("ipc_service_action_name", "string", application.getPackageName())));
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }

        return application.bindService(intent, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                ipc = IPC.Stub.asInterface(iBinder);

                instanceLatch.countDown();
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                ipc = null;

                instanceLatch = new CountDownLatch(1);

                application.startService(intent);
            }
        }, Context.BIND_AUTO_CREATE | Context.BIND_IMPORTANT);
    }

    private final static class IPCInMainThreadException extends Exception {
        public IPCInMainThreadException() {
            super(application.getResources().getString(R.string.ipc_in_main_thread_exception));
        }
    }

    private final static class ServiceNotConnectedException extends Exception {
        public ServiceNotConnectedException() {
            super(application.getResources().getString(R.string.ipc_service_not_connected_exception));
        }
    }

    public static abstract class Observer<T> implements io.reactivex.Observer<T> {
        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(T t) {

        }

        @Override
        public void onComplete() {

        }
    }

    public static abstract class Callback extends IPCCallback.Stub {
        public Callback scheduler(Scheduler scheduler) {
            final Callback origin = this;

            Callback delegate = new Callback() {
                @Override
                public void onNext(IPCResponse next) throws RemoteException {
                    Observable.just(origin).observeOn(scheduler).subscribe(callback -> callback.onNext(next));
                }

                @Override
                public void onComplete() throws RemoteException {
                    Observable.just(origin).observeOn(scheduler).subscribe(callback -> callback.onComplete());
                }

                @Override
                public void onError(IPCException e) throws RemoteException {
                    Observable.just(origin).observeOn(scheduler).subscribe(callback -> callback.onError(e));
                }
            };

            return delegate;
        }
    }

    private final static class IPCRxCallback extends Callback {
        private ObservableEmitter<IPCResponse> emitter;

        public IPCRxCallback() {
            super();
        }

        public IPCRxCallback(ObservableEmitter<IPCResponse> emitter) {
            this();
            this.emitter = emitter;
        }

        @Override
        public void onNext(IPCResponse next) throws RemoteException {
            if (emitter != null) {
                emitter.onNext(next);
            }
        }

        @Override
        public void onComplete() throws RemoteException {
            if (emitter != null) {
                emitter.onComplete();
            }
        }

        @Override
        public void onError(IPCException e) throws RemoteException {
            if (emitter != null) {
                emitter.onError(e);
            }
        }
    }

    public final static class Provider extends ContentProvider {
        @Override
        public boolean onCreate() {
            IPCSDK.initialize((Application) getContext().getApplicationContext());
            return true;
        }

        @Nullable
        @Override
        public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
            return null;
        }

        @Nullable
        @Override
        public String getType(@NonNull Uri uri) {
            return null;
        }

        @Nullable
        @Override
        public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
            return null;
        }

        @Override
        public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
            return 0;
        }
    }
}
