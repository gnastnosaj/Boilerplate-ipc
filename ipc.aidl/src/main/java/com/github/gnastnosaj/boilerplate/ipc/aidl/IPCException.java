package com.github.gnastnosaj.boilerplate.ipc.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jasontsang on 1/17/18.
 */

public class IPCException extends Exception implements Parcelable {
    private String message;
    private Throwable cause;

    public IPCException() {
        super();
    }

    public IPCException(String message) {
        super(message);
        this.message = message;
    }

    public IPCException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.cause = cause;
    }

    public IPCException(Throwable cause) {
        super(cause);
        this.cause = cause;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public synchronized Throwable getCause() {
        return cause;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.message);
        dest.writeSerializable(this.cause);
    }

    protected IPCException(Parcel in) {
        this.message = in.readString();
        this.cause = (Throwable) in.readSerializable();
    }

    public static final Creator<IPCException> CREATOR = new Creator<IPCException>() {
        @Override
        public IPCException createFromParcel(Parcel source) {
            return new IPCException(source);
        }

        @Override
        public IPCException[] newArray(int size) {
            return new IPCException[size];
        }
    };
}
