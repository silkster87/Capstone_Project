package com.example.android.newswiz.Widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.example.android.newswiz.R;
import com.neovisionaries.i18n.CountryCode;

/**
 * Implementation of Top Headlines App Widget functionality.
 */

public class NewsWizWidget extends AppWidgetProvider {

    public final static String widget_click = "widgetClick";
    public final static String topHeadlinesUpdate = "topHeadlinesUpdate";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.news_wiz_widget);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        String locale = context.getResources().getConfiguration().locale.getCountry();
        String defaultCountry = locale.toLowerCase();

        String countrySelected = sharedPrefs.getString(context.getResources().getString(R.string.widgetKey_top_headlines), defaultCountry);
        String country = countrySelected.toUpperCase();

        CountryCode cc = CountryCode.getByCode(country);
        String countryName = cc.getName();

        String topHeadlinesTitle = context.getResources().getString(R.string.top_headlines) + ": " + countryName;
        views.setTextViewText(R.id.top_headlines_widget_title, topHeadlinesTitle);

        Intent intent = new Intent(context, GridWidgetService.class);
        views.setRemoteAdapter(R.id.top_headlines_grid_view, intent);

        //Set Pending Intent template for the top headlines grid view

        Intent appIntent = new Intent(context, NewsWizWidget.class);
        appIntent.setAction(widget_click);
        PendingIntent appPendingIntent = PendingIntent.getBroadcast(context, 0, appIntent,0);
        views.setPendingIntentTemplate(R.id.top_headlines_grid_view, appPendingIntent);


        //Set to empty view if no articles chosen in settings
        views.setEmptyView(R.id.top_headlines_grid_view, R.id.empty_view);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ArticlesWidgetUpdateService.startActionUpdateNewsArticles(context, topHeadlinesUpdate);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public static void updateNewsWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds){
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getAction()!=null && intent.getAction().equals(widget_click)){
            try{
                String url = intent.getStringExtra(GridWidgetService.articleWidgetItemURL);
                Intent webIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(webIntent);
            }catch (RuntimeException e){
                e.printStackTrace();
            }
        }
    }
}

