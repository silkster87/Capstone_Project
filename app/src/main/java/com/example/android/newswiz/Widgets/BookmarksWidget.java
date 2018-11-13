package com.example.android.newswiz.Widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.example.android.newswiz.R;

/**
 * Implementation of BookMarks App Widget functionality.
 */

public class BookmarksWidget extends AppWidgetProvider {

public final static String bookmarksUpdate = "bookmarksUpdate";

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.news_wiz_widget);
        views.setTextViewText(R.id.top_headlines_widget_title, context.getResources().getString(R.string.bookmarks));

        Intent intent = new Intent(context, GridBookmarksWidgetService.class);
        views.setRemoteAdapter(R.id.top_headlines_grid_view, intent);

        Intent appIntent = new Intent(context, BookmarksWidget.class);
        appIntent.setAction(NewsWizWidget.widget_click);
        PendingIntent appPendingIntent = PendingIntent.getBroadcast(context,0,appIntent,0);
        views.setPendingIntentTemplate(R.id.top_headlines_grid_view, appPendingIntent);

        views.setEmptyView(R.id.top_headlines_grid_view, R.id.empty_view);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    public static void updateBookmarksWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for(int appWidgetId : appWidgetIds){
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        ArticlesWidgetUpdateService.startActionUpdateNewsArticles(context, bookmarksUpdate );
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getAction()!=null && intent.getAction().equals(NewsWizWidget.widget_click)){
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

