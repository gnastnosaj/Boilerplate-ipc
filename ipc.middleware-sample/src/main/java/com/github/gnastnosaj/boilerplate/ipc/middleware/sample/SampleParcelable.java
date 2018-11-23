package com.github.gnastnosaj.boilerplate.ipc.middleware.sample;

import android.os.Parcel;
import android.os.Parcelable;

public class SampleParcelable implements Parcelable {
    private String data;

    public SampleParcelable(String data) {
        this.data = data;
    }

    protected SampleParcelable(Parcel in) {
        this.data = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<SampleParcelable> CREATOR = new Parcelable.Creator<SampleParcelable>() {
        @Override
        public SampleParcelable createFromParcel(Parcel source) {
            return new SampleParcelable(source);
        }

        @Override
        public SampleParcelable[] newArray(int size) {
            return new SampleParcelable[size];
        }
    };
}
