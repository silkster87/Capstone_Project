package com.example.android.newswiz.Sources;

import android.os.Parcel;
import android.os.Parcelable;

public class ArticleItem implements Parcelable {

    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;

    private String source_id;
    private String source_name;

    public ArticleItem(){}

    public ArticleItem(String author, String title, String description, String url, String urlToImage, String publishedAt) {
        this.author = author;
        this.title = title;
        this.description = description;
        this.url = url;
        this.urlToImage = urlToImage;
        this.publishedAt = publishedAt;
    }

    private ArticleItem(Parcel in){
        author = in.readString();
        title = in.readString();
        description = in.readString();
        url = in.readString();
        urlToImage = in.readString();
        publishedAt = in.readString();
        source_id = in.readString();
        source_name = in.readString();
    }

    public static final Creator<ArticleItem> CREATOR = new Creator<ArticleItem>() {
        @Override
        public ArticleItem createFromParcel(Parcel in) {
            return new ArticleItem(in);
        }

        @Override
        public ArticleItem[] newArray(int size) {
            return new ArticleItem[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setSource_id(String source_id){
        this.source_id = source_id;
    }

    public void setSource_name(String source_name){
        this.source_name = source_name;
    }

    public String getSource_id(){
        return source_id;
    }

    public String getSource_name(){
        return source_name;
    }


    //This overriding of the equals method is necessary to compare article item objects for FirebaseDB
    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof ArticleItem)){
            return false;
        }
        return this.getTitle().equals(((ArticleItem) obj).getTitle());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(author);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeString(url);
        parcel.writeString(urlToImage);
        parcel.writeString(publishedAt);
        parcel.writeString(source_id);
        parcel.writeString(source_name);
    }
}
