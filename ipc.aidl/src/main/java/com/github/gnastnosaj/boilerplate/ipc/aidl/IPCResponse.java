package com.github.gnastnosaj.boilerplate.ipc.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class IPCResponse implements Parcelable {
    private int errorCode;
    private Object[] results;

    public IPCResponse(Object... results) {
        this.errorCode = 0;
        this.results = results;
    }

    public IPCResponse(int errorCode, Object... results) {
        this.errorCode = errorCode;
        this.results = results;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public Object[] getResults() {
        return results;
    }

    protected IPCResponse(Parcel in) {
        errorCode = in.readInt();
        results = in.readArray(this.getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(errorCode);
        dest.writeArray(results);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<IPCResponse> CREATOR = new Parcelable.Creator<IPCResponse>() {
        @Override
        public IPCResponse createFromParcel(Parcel source) {
            return new IPCResponse(source);
        }

        @Override
        public IPCResponse[] newArray(int size) {
            return new IPCResponse[size];
        }
    };
}
