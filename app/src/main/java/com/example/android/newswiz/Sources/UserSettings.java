package com.example.android.newswiz.Sources;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * User Settings class that will contain the users preferences in the settings. This object will be saved in the Firebase DB so
 * that user's settings are retained when on another device.
* */

public class UserSettings implements Parcelable {

    private String fontSize;
    private String theme;
    private String topHeadlinesCountry;

    public UserSettings(){}

    public UserSettings(String fontSize, String theme, String topHeadlinesCountry) {
        this.fontSize = fontSize;
        this.theme = theme;
        this.topHeadlinesCountry = topHeadlinesCountry;
    }

    private UserSettings(Parcel in){
        fontSize = in.readString();
        theme = in.readString();
        topHeadlinesCountry = in.readString();
    }

    public static final Creator<UserSettings> CREATOR = new Creator<UserSettings>() {
        @Override
        public UserSettings createFromParcel(Parcel parcel) {
            return new UserSettings(parcel);
        }

        @Override
        public UserSettings[] newArray(int size) {
            return new UserSettings[size];
        }
    };

    public String getFontSize() {
        return fontSize;
    }

    public String getTheme() {
        return theme;
    }

    public String getTopHeadlinesCountry() {
        return topHeadlinesCountry;
    }

    public void setFontSize(String fontSize) {
        this.fontSize = fontSize;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public void setTopHeadlinesCountry(String topHeadlinesCountry) {
        this.topHeadlinesCountry = topHeadlinesCountry;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(fontSize);
        parcel.writeString(theme);
        parcel.writeString(topHeadlinesCountry);
    }
}
