package com.example.android.newswiz.Sources;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class SourcesInfo implements Parcelable{


    private ArrayList<String> mPublishersSelected;
    private ArrayList<String> mCategoriesSelected;
    private ArrayList<String> mCountriesSelected;

    public SourcesInfo(){}

    public SourcesInfo(ArrayList<String> mPublishersSelected, ArrayList<String> mCategoriesSelected, ArrayList<String> mCountriesSelected) {
        this.mPublishersSelected = mPublishersSelected;
        this.mCategoriesSelected = mCategoriesSelected;
        this.mCountriesSelected = mCountriesSelected;
    }

    private SourcesInfo(Parcel in) {
        mPublishersSelected = in.createStringArrayList();
        mCategoriesSelected = in.createStringArrayList();
        mCountriesSelected = in.createStringArrayList();
    }

    public static final Creator<SourcesInfo> CREATOR = new Creator<SourcesInfo>() {
        @Override
        public SourcesInfo createFromParcel(Parcel in) {
            return new SourcesInfo(in);
        }

        @Override
        public SourcesInfo[] newArray(int size) {
            return new SourcesInfo[size];
        }
    };

    public ArrayList<String> getmPublishersSelected() {
        return mPublishersSelected;
    }

    public ArrayList<String> getmCategoriesSelected() {
        return mCategoriesSelected;
    }

    public ArrayList<String> getmCountriesSelected() {
        return mCountriesSelected;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringList(mPublishersSelected);
        parcel.writeStringList(mCategoriesSelected);
        parcel.writeStringList(mCountriesSelected);
    }
}
