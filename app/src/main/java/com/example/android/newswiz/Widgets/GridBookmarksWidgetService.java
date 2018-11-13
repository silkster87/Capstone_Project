package com.example.android.newswiz.Widgets;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.android.newswiz.R;
import com.example.android.newswiz.Sources.ArticleItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;

/**
 * GridBookmarksService to update the Bookmarks widget by updating the Grid Remote View
* */

public class GridBookmarksWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridBookmarksRemoteViewsFactory(this.getApplicationContext());
    }
}

class GridBookmarksRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private Context mContext;
    private ArrayList<ArticleItem> listOfBookmarkedArticles;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef;

    public GridBookmarksRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
        mFirebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference("users");
        listOfBookmarkedArticles = new ArrayList<>();
    }

    @Override
    public void onCreate() {
        databaseRef.child(mFirebaseAuth.getCurrentUser().getUid()).child("bookmarks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listOfBookmarkedArticles.clear();

                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    ArticleItem articleItem = dataSnapshot1.getValue(ArticleItem.class);
                    listOfBookmarkedArticles.add(articleItem);
                }

                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(mContext);
                int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(mContext, BookmarksWidget.class));
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.top_headlines_grid_view);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onDataSetChanged() {}

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        if(listOfBookmarkedArticles==null)return 0;
        return listOfBookmarkedArticles.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.news_article_item_widget);

            ArticleItem articleItem = listOfBookmarkedArticles.get(position);
            String articleImage = articleItem.getUrlToImage();
            String articleTitle = articleItem.getTitle();
            String articleURL = articleItem.getUrl();

            try{
                Bitmap b = Picasso.get().load(articleImage).get();
                views.setImageViewBitmap(R.id.article_image_widget, b);
            } catch (IOException e) {
                e.printStackTrace();
            }

            views.setTextViewText(R.id.article_titletext_widget, articleTitle);

            Bundle extras = new Bundle();
            extras.putString(GridWidgetService.articleWidgetItemURL, articleURL);
            Intent fillInIntent = new Intent();
            fillInIntent.putExtras(extras);
            views.setOnClickFillInIntent(R.id.article_widget_layout, fillInIntent);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
}