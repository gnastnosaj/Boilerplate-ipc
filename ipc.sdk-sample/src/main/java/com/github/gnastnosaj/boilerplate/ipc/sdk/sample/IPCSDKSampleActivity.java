package com.github.gnastnosaj.boilerplate.ipc.sdk.sample;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCCallback;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCException;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCRequest;
import com.github.gnastnosaj.boilerplate.ipc.aidl.IPCResponse;
import com.github.gnastnosaj.boilerplate.ipc.sdk.IPCSDK;
import com.github.gnastnosaj.boilerplate.rxbus.RxHelper;
import com.github.gnastnosaj.boilerplate.ui.activity.BaseActivity;
import com.trello.rxlifecycle2.android.ActivityEvent;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

/**
 * Created by jasontsang on 1/17/18.
 */

public class IPCSDKSampleActivity extends BaseActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ipc_sdk_sample);

        Observable.create(subscriber -> {
            IPCSDK.getInstance().subscribe("sample", new IPCCallback.Stub() {
                @Override
                public void onNext(IPCResponse next) throws RemoteException {
                    Observable.just(next)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    tick -> Toast.makeText(IPCSDKSampleActivity.this, "register 1:" + tick.getResults()[0], Toast.LENGTH_SHORT).show(),
                                    throwable -> Toast.makeText(IPCSDKSampleActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show()
                            );
                }

                @Override
                public void onComplete() throws RemoteException {

                }

                @Override
                public void onError(IPCException e) throws RemoteException {

                }
            });

            subscriber.onNext(true);
            subscriber.onComplete();
        }).compose(RxHelper.rxSchedulerHelper()).subscribe((aBoolean) -> {
        }, throwable -> Toast.makeText(IPCSDKSampleActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show());

        IPCSDK.getInstance().register("sample", new IPCSDK.Callback() {
            @Override
            public void onNext(IPCResponse next) throws RemoteException {
                Toast.makeText(IPCSDKSampleActivity.this, "register 2:" + next.getResults()[0], Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() throws RemoteException {

            }

            @Override
            public void onError(IPCException e) throws RemoteException {

            }
        }.scheduler(AndroidSchedulers.mainThread())).subscribe(callback -> callback.onNext(new IPCResponse("hehe")), throwable -> Toast.makeText(IPCSDKSampleActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show());

        findViewById(R.id.exec).setOnClickListener(v ->
                IPCSDK.getInstance()
                        .exec("com.github.gnastnosaj.boilerplate.ipc.middleware.sample", new IPCRequest("ipc sample command"))
                        .compose(bindUntilEvent(ActivityEvent.DESTROY))
                        .subscribe(
                                tick -> Toast.makeText(IPCSDKSampleActivity.this, tick.getResults()[0].toString(), Toast.LENGTH_SHORT).show(),
                                throwable -> Toast.makeText(IPCSDKSampleActivity.this, throwable.getMessage(), Toast.LENGTH_SHORT).show(),
                                () -> Toast.makeText(IPCSDKSampleActivity.this, "complete", Toast.LENGTH_SHORT).show()
                        )
        );
    }
}
