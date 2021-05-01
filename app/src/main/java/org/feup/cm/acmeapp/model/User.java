package org.feup.cm.acmeapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class User implements Parcelable {
    private String id;
    private String name;
    private String username;
    private String password;
    private String payment_card;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String name, String username, String password, String payment_card) {
        this.name = name;
        this.username = username;
        this.password = password;
        this.payment_card = payment_card;
    }

    protected User(Parcel in) {
        name = in.readString();
        username = in.readString();
        password = in.readString();
        payment_card = in.readString();
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public String getName() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(payment_card);
    }
}
