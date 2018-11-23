package com.github.gnastnosaj.boilerplate.ipc;

import android.app.Service;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.gnastnosaj.boilerplate.ipc.aidl.IPC;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCCallback;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCException;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCRequest;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCResponse;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCEvent;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCEventBus;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCMiddleware;
import com.github.gnastnosaj.boilerplate.ipc.middleware.IPCMiddlewareCallback;
import com.github.gnastnosaj.boilerplate.rxbus.RxBus;
import com.github.gnastnosaj.boilerplate.rxbus.RxHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

/**
 * Created by jasontsang on 1/17/18.
 */

public class IPCService extends Service {
    public final static List<IPCMiddleware> middlewares = new ArrayList<>();

    public final static Map<IPCCallback, Disposable> subscriptions = new ConcurrentHashMap<>();

    public final static Map<IPCCallback, Observable> observables = new ConcurrentHashMap<>();

    private IBinder binder;

    @Override
    public void onCreate() {
        super.onCreate();

        binder = new IPCBinder(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private final static class IPCBinder extends IPC.Stub {
        private Context context;

        public IPCBinder(Context context) {
            super();

            this.context = context;

            for (IPCMiddleware middleware : middlewares) {
                middleware.initialize(context, new IPCEventBus() {
                    @Override
                    public void send(IPCEvent event) {
                        RxBus.getInstance().send(event);
                    }

                    @Override
                    public void post(String tag, IPCEvent event) {
                        RxBus.getInstance().post(tag, event);
                    }
                });
            }
        }

        @Override
        public void exec(String scheme, IPCRequest data, IPCCallback callback) throws RemoteException {
            List<Observable<IPCResponse>> observables = new ArrayList<>();

            for (IPCMiddleware middleware : middlewares) {
                if (middleware.accept(scheme)) {
                    observables.add(Observable.create(subscriber -> middleware.exec(scheme, data, new IPCMiddlewareCallback() {
                        @Override
                        public void onNext(IPCResponse data) {
                            try {
                                callback.onNext(data);
                            } catch (Throwable throwable) {
                                subscriber.onError(throwable);
                            }
                            subscriber.onNext(data);
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            subscriber.onError(throwable);
                        }

                        @Override
                        public void onComplete() {
                            subscriber.onComplete();
                        }
                    })));
                }
            }

            if (observables.isEmpty()) {
                callback.onError(new IPCException(context.getResources().getString(R.string.unsupported_ipc_scheme)));
            } else {
                Observable.zip(observables, outcomes -> outcomes.length)
                        .compose(RxHelper.rxSchedulerHelper())
                        .subscribe(
                                count -> {
                                },
                                throwable -> callback.onError(new IPCException(throwable.getMessage(), throwable)),
                                () -> callback.onComplete()
                        );
            }
        }

        @Override
        public void subscribe(IPCCallback callback) throws RemoteException {
            Disposable disposable = RxBus.getInstance().toObserverable().subscribe(event -> {
                if (event instanceof IPCEvent) {
                    try {
                        callback.onNext(new IPCResponse(event instanceof Parcelable ? event : event.toString()));
                    } catch (Throwable throwable) {
                        dispose(callback);
                    }
                }
            }, throwable -> callback.onError(new IPCException(throwable.getMessage(), throwable)));
            subscriptions.put(callback, disposable);
        }

        @Override
        public void dispose(IPCCallback callback) throws RemoteException {
            Disposable disposable = subscriptions.get(callback);
            if (disposable != null && !disposable.isDisposed()) {
                disposable.dispose();
            }
            subscriptions.remove(callback);
        }

        @Override
        public void register(String tag, IPCCallback callback) throws RemoteException {
            Observable<IPCEvent> observable = RxBus.getInstance().register(tag, IPCEvent.class);
            observables.put(callback, observable);

            Disposable disposable = observable.subscribe(event -> {
                try {
                    callback.onNext(new IPCResponse(event instanceof Parcelable ? event : event.toString()));
                } catch (Throwable throwable) {
                    unregister(tag, callback);
                }
            }, throwable -> callback.onError(new IPCException(throwable.getMessage(), throwable)));
            subscriptions.put(callback, disposable);
        }

        @Override
        public void unregister(String tag, IPCCallback callback) throws RemoteException {
            dispose(callback);

            Observable observable = observables.get(callback);
            RxBus.getInstance().unregister(tag, observable);
            observables.remove(callback);
        }
    }

    public final static class IPCServiceProvider extends ContentProvider {

        @Override
        public boolean onCreate() {
            getContext().getApplicationContext().startService(new Intent(getContext().getApplicationContext(), IPCService.class));

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

    public static abstract class IPCMiddlewareProvider extends ContentProvider {
        @Override
        public boolean onCreate() {
            middlewares.addAll(getIPCMiddlewares());

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

        protected abstract List<IPCMiddleware> getIPCMiddlewares();
    }
}
