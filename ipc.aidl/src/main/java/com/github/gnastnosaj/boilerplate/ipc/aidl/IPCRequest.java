package com.github.gnastnosaj.boilerplate.ipc.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class IPCRequest implements Parcelable {
    private String action;
    private Object[] args;

    public IPCRequest(Object... args) {
        this.action = "";
        this.args = args;
    }

    public IPCRequest(String action, Object[] args) {
        this.action = action;
        this.args = args;
    }

    public String getAction() {
        return action;
    }

    public Object[] getArgs() {
        return args;
    }

    protected IPCRequest(Parcel in) {
        action = in.readString();
        args = in.readArray(this.getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(action);
        dest.writeArray(args);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<IPCRequest> CREATOR = new Creator<IPCRequest>() {
        @Override
        public IPCRequest createFromParcel(Parcel in) {
            return new IPCRequest(in);
        }

        @Override
        public IPCRequest[] newArray(int size) {
            return new IPCRequest[size];
        }
    };
}
