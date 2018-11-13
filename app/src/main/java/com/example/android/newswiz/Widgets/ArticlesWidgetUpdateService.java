package com.example.android.newswiz.Widgets;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.example.android.newswiz.BuildConfig;
import com.example.android.newswiz.NewsArticles;
import com.example.android.newswiz.R;
import com.example.android.newswiz.Utilities.GetOkHttpResponse;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class ArticlesWidgetUpdateService extends IntentService {

    private static final String ACTION_UPDATE_NEWS_ARTICLES = "update_news_articles";
    public static final String ACTION_UPDATE_BOOKMARKS = "update_bookmarks";
    private final static String NEWS_API_KEY = BuildConfig.NEWS_API_KEY;

    public ArticlesWidgetUpdateService() {
        super("ArticlesWidgetUpdateService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static void startActionUpdateNewsArticles(Context context, String action){
        if(action.equals(NewsWizWidget.topHeadlinesUpdate)){
            Intent intent = new Intent(context, ArticlesWidgetUpdateService.class);
            intent.setAction(ACTION_UPDATE_NEWS_ARTICLES);
            context.startService(intent);
        }else if(action.equals(BookmarksWidget.bookmarksUpdate)){
            Intent intent = new Intent(context, ArticlesWidgetUpdateService.class);
            intent.setAction(ACTION_UPDATE_BOOKMARKS);
            context.startService(intent);
        }
    }

    public void handleActionUpdateBookmarks() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, BookmarksWidget.class));
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.top_headlines_grid_view);
        BookmarksWidget.updateBookmarksWidgets(this, appWidgetManager, appWidgetIds);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if(intent!=null){
            final String action = intent.getAction();
            if(ACTION_UPDATE_NEWS_ARTICLES.equals(action)){
                handleActionUpdateNewsArticles();
            }else if(ACTION_UPDATE_BOOKMARKS.equals(action)){
                handleActionUpdateBookmarks();
            }
        }

    }

    public void handleActionUpdateNewsArticles() {

        Context context = getApplicationContext();

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String locale = context.getResources().getConfiguration().locale.getCountry();
        String defaultCountry = locale.toLowerCase();

        String jsonTopHeadlines;
        String country = sharedPref.getString(context.getResources().getString(R.string.widgetKey_top_headlines), defaultCountry);

        try{
            String topHeadlinesURL = NewsArticles.newsApiStartPoint + "country=" + country + "&apiKey=" + NEWS_API_KEY;
            Request request = new Request.Builder().url(topHeadlinesURL).build();
            OkHttpClient client = new OkHttpClient();
            GetOkHttpResponse getOkHttpResponse = new GetOkHttpResponse(client, request);
            jsonTopHeadlines = getOkHttpResponse.run();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.json_topheadlines_key), jsonTopHeadlines);
        editor.apply();


            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, NewsWizWidget.class));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.top_headlines_grid_view);
            NewsWizWidget.updateNewsWidgets(this, appWidgetManager, appWidgetIds);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
