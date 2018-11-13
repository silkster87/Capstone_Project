package com.example.android.newswiz.Widgets;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.example.android.newswiz.R;
import com.example.android.newswiz.Sources.ArticleItem;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * GridWidgetService to update the Top headlines widget by updating the Grid Remote View
* */

public class GridWidgetService extends RemoteViewsService {

    public static final String articleWidgetItemURL = "articleItemURL";

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new GridRemoteViewsFactory(this.getApplicationContext());
    }
}


 class GridRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory{

    private Context mContext;
    private ArrayList<ArticleItem> listOfTopHeadlinesArticles;
    private final static String mArticles = "articles";
    private Resources res;

    public GridRemoteViewsFactory(Context applicationContext) {
        mContext = applicationContext;
        res = mContext.getResources();
    }

    @Override
    public void onCreate() {}

    @Override
    public void onDataSetChanged() { //This method is called by appWidgetManager.notifyAppWidgetViewDataChanged() or first time setting this up.
        // It is also called every hour as determined by the news_wiz_widget_info.xml in the updatePeriodMillis attribute
        //Here we get the top headlines or bookmarked articles data
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext.getApplicationContext());
        String mTopHeadlinesJSONString = sharedPrefs.getString(res.getString(R.string.json_topheadlines_key), null);

        try {
            makeListOfArticles(mTopHeadlinesJSONString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

     private void makeListOfArticles(String mTopHeadlinesJSONString) throws JSONException {
         if(mTopHeadlinesJSONString!=null){
             JSONObject newsArticlesJSONresponse = new JSONObject(mTopHeadlinesJSONString);
             JSONArray newsArticlesJSONArray = newsArticlesJSONresponse.getJSONArray(mArticles);

             if(newsArticlesJSONArray==null) throw new AssertionError();
             listOfTopHeadlinesArticles = new ArrayList<>();

             for(int i = 0; i < newsArticlesJSONArray.length(); i++){
                 JSONObject jsonArticle = (JSONObject) newsArticlesJSONArray.get(i);
                 String jsonString = jsonArticle.toString();
                 Gson gson = new Gson();
                 ArticleItem articleItem = gson.fromJson(jsonString, ArticleItem.class);

                 JSONObject newsArticleJSONObject = jsonArticle.getJSONObject("source");
                 String source_id = newsArticleJSONObject.getString("id");
                 String source_name = newsArticleJSONObject.getString("name");

                 articleItem.setSource_id(source_id);
                 articleItem.setSource_name(source_name);
                 listOfTopHeadlinesArticles.add(articleItem);
             }
         }
     }

    @Override
    public void onDestroy() {}

    @Override
    public int getCount() {
        if(listOfTopHeadlinesArticles==null) return 0;
        return listOfTopHeadlinesArticles.size();
    }

    @Override
    public RemoteViews getViewAt(int position) { //This is similar to onBindView() in a typical adapter
        //Here once we get the data from onDataSetChanged() we put the data into the view

        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.news_article_item_widget);
        ArticleItem articleItem = listOfTopHeadlinesArticles.get(position);
        String articleImage = articleItem.getUrlToImage();
        String articleTitle = articleItem.getTitle();
        String articleURL = articleItem.getUrl();

        try{
            Bitmap b = Picasso.get().load(articleImage).get();
            views.setImageViewBitmap(R.id.article_image_widget, b);
        } catch (IOException e){
            e.printStackTrace();
        }

        views.setTextViewText(R.id.article_titletext_widget, articleTitle);

        //When user clicks on an individual news article we want to launch a webpage from the article url
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
