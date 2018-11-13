package com.example.android.newswiz;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;

import com.example.android.newswiz.Adapters.SectionsPageAdapter;
import com.example.android.newswiz.Fragments.ArticleContentRHSFragment;
import com.example.android.newswiz.Fragments.NewsSourceFragment;
import com.example.android.newswiz.Fragments.NewsSourceFragmentTabletLand;
import com.example.android.newswiz.Sources.ArticleItem;
import com.example.android.newswiz.Sources.SourcesInfo;
import com.example.android.newswiz.Utilities.GetOkHttpResponse;
import com.example.android.newswiz.Utilities.MySuggestionProvider;
import com.example.android.newswiz.Utilities.TranslateSources;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
This NewsArticles Activity will display the news articles from the news sources that the user has selected
in the MainActivity. There are two loaders to load up the JSON results: one for the publishers and one for
the country and categories selected. Once the JSON results are collected, the tabs will be created along with the
fragments that will display the news articles.
*/


public class NewsArticles extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<String>>,
        NewsSourceFragment.FragmentListener, NewsSourceFragmentTabletLand.TabletRefreshSwipeListener,
        ArticleContentRHSFragment.OnBookmarkSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener
{

    private SourcesInfo mSourcesInfo;
    public final static String newsApiStartPoint = "https://newsapi.org/v2/top-headlines?";
    private final static String NEWS_API_KEY = BuildConfig.NEWS_API_KEY;
    private final static String publisherArticlesKey = "PublisherArticles";
    private final static String countriesArticlesKey = "countriesArticles";
    public final static String bookmarksKey = "bookmarkedArticlesKey";
    public final static String fontSizeKey = "fontSizeKey";
    private static final String TAG = NewsArticles.class.getSimpleName();
    public final static String clickedArticleURLTAG = "Clicked on Article to open URL";
    public final static String clickedArticleBookmarkTAG = "Clicked on Article Bookmark";
    public final static String clickedSwipeToRefreshTAG = "Clicked SwipeToRefresh";
    public final static String publishersJSONStringKey = "publishersKey";
    public final static String countriesAndCategoriesJSONStringKey = "countriesCategoriesKey";

    private ArrayList<String> mPublisherJSONList;
    private ArrayList<String> mCountryCategoryJSONList;
    private LoaderManager.LoaderCallbacks<ArrayList<String>> callback;

    private ArrayList<ArticleItem> bookmarkedArticles;
    private SearchRecentSuggestions suggestions;

    //Internet connection - are we connected?
    private boolean connected = false;

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef;

    @BindView(R.id.error_msg) TextView mErrorMsg;
    @BindView(R.id.progress_bar_articles) ProgressBar mProgressBar;
    @BindView(R.id.news_articles_viewPager) ViewPager mNewsViewPager;
    @BindView(R.id.sources_tabs) TabLayout tabLayoutSources;
    @BindView(R.id.news_articles_toolbar) Toolbar mToolbar;

    private Context context;
    private SharedPreferences pref;
    private Resources res;
    private BroadcastReceiver broadcastReceiver;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        res = getResources();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        String themeName = pref.getString(res.getString(R.string.theme_key), res.getString(R.string.theme_default));

        if(themeName.equals(res.getString(R.string.lightLabel))){
            setTheme(R.style.LightTheme);
        } else if (themeName.equals(res.getString(R.string.darkLabel))) {
            setTheme(R.style.DarkTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_articles);
        ButterKnife.bind(this);

        context = getApplicationContext();


        setSupportActionBar(mToolbar);

        ActionBar ab = getSupportActionBar();

        if(ab != null)
        ab.setDisplayHomeAsUpEnabled(true);

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if(connectivityManager!=null)
            //connected to a network
            connected = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState() == NetworkInfo.State.CONNECTED ||
                    connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED;

        mFirebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference(res.getString(R.string.users_label));
        bookmarkedArticles = new ArrayList<>();

        suggestions = new SearchRecentSuggestions(this,
                MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);

        getListOfBookmarkedArticles();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                finish();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(res.getString(R.string.logout_action));
        registerReceiver(broadcastReceiver, intentFilter);

        Intent intent = getIntent();
        mSourcesInfo = intent.getParcelableExtra(MainActivity.SOURCES);

        client = new OkHttpClient();

        callback = NewsArticles.this;

        if(savedInstanceState == null) {
            getSupportLoaderManager().initLoader(0, null, callback); //This is for loading the news publishers json string
            getSupportLoaderManager().initLoader(1, null, callback); //This is for loading the categories and countries json string
        } else {
            mPublisherJSONList = savedInstanceState.getStringArrayList(publisherArticlesKey);
            mCountryCategoryJSONList = savedInstanceState.getStringArrayList(countriesArticlesKey);
            setUpViewPager(mNewsViewPager);
            tabLayoutSources.setupWithViewPager(mNewsViewPager);
        }

        pref.registerOnSharedPreferenceChangeListener(this);
    }

    private void getListOfBookmarkedArticles() {
        if(mFirebaseAuth.getCurrentUser()!=null)
        databaseRef.child(mFirebaseAuth.getCurrentUser().getUid()).child(res.getString(R.string.bookmarks_label)).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Clear the previous list
                bookmarkedArticles.clear();

                //Iterating through all nodes
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    ArticleItem articleItem = dataSnapshot1.getValue(ArticleItem.class);
                    bookmarkedArticles.add(articleItem);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void setUpViewPager(ViewPager mNewsViewPager) {

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());

        //Generating tabs for publishers selected
        for(int i = 0; i< mPublisherJSONList.size(); i++){
            String publisher = mSourcesInfo.getmPublishersSelected().get(i);
            Bundle publisherBundle = new Bundle();
            publisherBundle.putString(publishersJSONStringKey, mPublisherJSONList.get(i));
            publisherBundle.putParcelableArrayList(bookmarksKey, bookmarkedArticles);

            //Fontsize that user has chosen in preferences
            String fontSize = pref.getString(res.getString(R.string.pref_fontSize_key), res.getString(R.string.fontSize_default));
            publisherBundle.putString(fontSizeKey, fontSize);


            if(!getResources().getBoolean(R.bool.isTablet)){
                //Not in tablet mode - phone view
                NewsSourceFragment mNewsSourceFragment = new NewsSourceFragment();
                mNewsSourceFragment.setArguments(publisherBundle); //The fragment will check whether it has publisher or country JSON string
                adapter.addFragment(mNewsSourceFragment, publisher);
            }else{
                //Tablet mode - use master detail flow layout

                //The NewsSourceFragmentTabletLand is a parent fragment that will contain 2 child fragments.
                NewsSourceFragmentTabletLand mNewsSourceFragmentTabletLand = new NewsSourceFragmentTabletLand();
                mNewsSourceFragmentTabletLand.setArguments(publisherBundle);
                adapter.addFragment(mNewsSourceFragmentTabletLand, publisher);
            }
        }

        //Generating tabs for countries selected with sub-categories

        int b = 0;
        while ( b < mCountryCategoryJSONList.size()) {
            for (int y = 0; y < mSourcesInfo.getmCountriesSelected().size(); y++) {
                String country = mSourcesInfo.getmCountriesSelected().get(y);
                for (int a = 0; a < mSourcesInfo.getmCategoriesSelected().size(); a++) {
                    String category = mSourcesInfo.getmCategoriesSelected().get(a);
                    Bundle countryCategoryBundle = new Bundle();
                    //Fontsize that user has chosen in preferences
                    String fontSize = pref.getString(res.getString(R.string.pref_fontSize_key), res.getString(R.string.fontSize_default));
                    countryCategoryBundle.putString(fontSizeKey, fontSize);
                    countryCategoryBundle.putParcelableArrayList(bookmarksKey, bookmarkedArticles);
                    try{
                        countryCategoryBundle.putString(countriesAndCategoriesJSONStringKey, mCountryCategoryJSONList.get(b));
                    } catch (IndexOutOfBoundsException e){
                        e.printStackTrace();
                    }

                    if(!getResources().getBoolean(R.bool.isTablet)){
                        //Not in tablet mode - phone view
                        NewsSourceFragment mCountryCategorySourceFragment = new NewsSourceFragment();
                        mCountryCategorySourceFragment.setArguments(countryCategoryBundle);
                        String countryAndCategoryLabelTab = country + " (" + category + ")";
                        adapter.addFragment(mCountryCategorySourceFragment, countryAndCategoryLabelTab);
                    } else{

                        NewsSourceFragmentTabletLand mNewsSourceFragmentTabletLand2 = new NewsSourceFragmentTabletLand();
                        mNewsSourceFragmentTabletLand2.setArguments(countryCategoryBundle);
                        String countryAndCategoryLabelTab = country + " (" + category + ")";
                        adapter.addFragment(mNewsSourceFragmentTabletLand2, countryAndCategoryLabelTab);
                    }

                    b++;
                }
            }
        }

        mProgressBar.setVisibility(View.GONE);
        mNewsViewPager.setAdapter(adapter);
    }

    @SuppressLint("StaticFieldLeak")
    @NonNull
    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, @Nullable Bundle args) {
        if(id == 0) {
            return new AsyncTaskLoader<ArrayList<String>>(this) {

                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Nullable
                @Override
                public ArrayList<String> loadInBackground() {

                    //You can combine countries with categories but not with news publishers
                    //The news publishers articles will have to be displayed separately from the country and categories articles

                    mPublisherJSONList = new ArrayList<>();
                    try {
                        for (int i = 0; i < mSourcesInfo.getmPublishersSelected().size(); i++) {
                            String newsPublisher = mSourcesInfo.getmPublishersSelected().get(i);
                            String newsPublisherForUrl = TranslateSources.translateNewsPublisher(newsPublisher);

                            String publisherURL = newsApiStartPoint + "sources=" + newsPublisherForUrl + "&apiKey=" + NEWS_API_KEY;
                            Request request = new Request.Builder().url(publisherURL).build();
                            GetOkHttpResponse getOkHttpResponse = new GetOkHttpResponse(client, request);
                            String jsonPublisherDataResponse = getOkHttpResponse.run();

                            mPublisherJSONList.add(jsonPublisherDataResponse);
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return mPublisherJSONList;
                }

                @Override
                public void deliverResult(@Nullable ArrayList<String> data) {
                    mPublisherJSONList = data;
                    super.deliverResult(data);
                }
            };
        } else {
            return new AsyncTaskLoader<ArrayList<String>>(this) {

                @Override
                protected void onStartLoading() {
                    forceLoad();
                }

                @Nullable
                @Override
                public ArrayList<String> loadInBackground() {

                    mCountryCategoryJSONList = new ArrayList<>();
                    try{
                        for(int i = 0; i < mSourcesInfo.getmCountriesSelected().size(); i++){
                            String country = mSourcesInfo.getmCountriesSelected().get(i);
                            String countryForUrl = TranslateSources.translateCountry(country);

                            for(int x=0; x< mSourcesInfo.getmCategoriesSelected().size(); x++){
                                String category = mSourcesInfo.getmCategoriesSelected().get(x);
                                String categoryForUrl = TranslateSources.translateCategory(category);

                                String countryAndCategoryURL = newsApiStartPoint + "country=" + countryForUrl + "&category=" + categoryForUrl + "&apiKey=" + NEWS_API_KEY;
                                Request request = new Request.Builder().url(countryAndCategoryURL).build();
                                GetOkHttpResponse getOkHttpResponse = new GetOkHttpResponse(client, request);
                                String jsonCountryAndCategoryDataResponse = getOkHttpResponse.run();
                                mCountryCategoryJSONList.add(jsonCountryAndCategoryDataResponse);
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return mCountryCategoryJSONList;
                }

                @Override
                public void deliverResult(@Nullable ArrayList<String> data) {
                    mCountryCategoryJSONList = data;
                    super.deliverResult(data);
                }
            };
        }

    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<String>> loader, ArrayList<String> data) {
        if(data == null || !connected){
            showErrorMessage();
        }else {
            //Making ViewPager with the no. of tabs dependent on what news sources user selected.
            try {
                checkArticlesForErrors(mPublisherJSONList);
                checkArticlesForErrors(mCountryCategoryJSONList);
            } catch (JSONException e) {
                e.printStackTrace();
            }

                setUpViewPager(mNewsViewPager);
                tabLayoutSources.setupWithViewPager(mNewsViewPager);



        }
    }

    private void checkArticlesForErrors(ArrayList<String> mJSONStringList) throws JSONException {

        for(int i=0; i<mJSONStringList.size(); i++){
            String mJSONString = mJSONStringList.get(i);

            JSONObject newsArticlesJSONresponse = new JSONObject(mJSONString);

            String status = newsArticlesJSONresponse.getString(res.getString(R.string.status_key));

            if(status.equals(res.getString(R.string.error_label))){
                String errorCode = newsArticlesJSONresponse.getString(res.getString(R.string.code_key));
                String errorMessage = newsArticlesJSONresponse.getString(res.getString(R.string.message_key));
                String errorTitle = "Error: " + errorCode;

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(errorMessage)
                        .setTitle(errorTitle)
                        .setPositiveButton(R.string.ok_confirm, (dialogInterface, a) -> dialogInterface.dismiss());

                builder.show();
                break;
            }

        }
    }

    private void showErrorMessage() {
        mProgressBar.setVisibility(View.GONE);
        mErrorMsg.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<String>> loader) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();

        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_refresh:
                updateNewsData();
                return true;
            case R.id.menu_settings:
                //Start a new Settings activity where user can change size of text and themes/colors.
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.menu_bookmarks:
                Intent bookmarksIntent = new Intent(getApplicationContext(), BookmarksActivity.class);
                bookmarksIntent.putParcelableArrayListExtra(bookmarksKey, bookmarkedArticles);
                startActivity(bookmarksIntent);
                return true;
            case R.id.menu_clear_search_history:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.dialog_clear_history)
                        .setPositiveButton(R.string.ok_confirm, (dialogInterface, i) -> {
                            //Clear Search history
                            suggestions.clearHistory();
                            recreate();
                        })
                        .setNegativeButton(R.string.cancel_dialog, (dialogInterface, i) -> {
                            //Cancel dialogue
                            dialogInterface.cancel();
                        });

                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(context);
                Intent broadcastIntent = new Intent();
                broadcastIntent.setAction(res.getString(R.string.logout_action));
                sendBroadcast(broadcastIntent);
                return true;
            case R.id.menu_about:
                Intent aboutIntent = new Intent(getApplicationContext(), AboutActivity.class);
                startActivity(aboutIntent);
                return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(publisherArticlesKey, mPublisherJSONList);
        outState.putStringArrayList(countriesArticlesKey, mCountryCategoryJSONList);
    }

    //This onSwipeToRefresh() method is the same for phone and tablet mode


    @Override
    public void onSwipeToRefresh() { //Tablet swipe to refresh method
        updateNewsData();
    }

    @Override
    public void onFragmentClick(String TAG, ArticleItem articleItem) {
        switch (TAG) {
            case NewsArticles.clickedSwipeToRefreshTAG:
                updateNewsData();
                break;
            case NewsArticles.clickedArticleURLTAG:
                Uri webpage = Uri.parse(articleItem.getUrl());
                Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    startActivity(intent);
                }
                break;
            case NewsArticles.clickedArticleBookmarkTAG:
                //Bookmark article here.

                if (bookmarkedArticles != null) {
                    if (bookmarkedArticles.contains(articleItem)) {
                        bookmarkedArticles.remove(articleItem);
                        String message = "You have removed " + "\"" + articleItem.getTitle() + "\"";
                        showSnackBar(message);
                    } else {
                        bookmarkedArticles.add(articleItem);
                        String message = "You have added " + "\"" + articleItem.getTitle() + "\"";
                        showSnackBar(message);
                    }
                }
                databaseRef.child(mFirebaseAuth.getCurrentUser().getUid()).child("bookmarks").setValue(bookmarkedArticles);

                if (!res.getBoolean(R.bool.isTablet)) {
                    //If in phone view then refresh view
                    if (mNewsViewPager.getAdapter() != null)
                        mNewsViewPager.getAdapter().notifyDataSetChanged();
                }

                break;
        }
    }

    public void showSnackBar(String message){
        View view = findViewById(R.id.news_articles_coordinatorLayout);
        int duration = Snackbar.LENGTH_SHORT;
        Snackbar.make(view, message, duration).show();
    }

    private void updateNewsData(){
        getSupportLoaderManager().restartLoader(1,null, callback);
        getSupportLoaderManager().restartLoader(0, null, callback);
        if(mNewsViewPager.getAdapter()!=null)
        mNewsViewPager.getAdapter().notifyDataSetChanged();
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(res.getString(R.string.theme_key))){
            //The onCreate() method will set the theme from the sharedPreferences.
            recreate();
        } else if (key.equals(res.getString(R.string.pref_fontSize_key))){
            if(res.getBoolean(R.bool.isTablet)){
                recreate();
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        pref.unregisterOnSharedPreferenceChangeListener(this);
        unregisterReceiver(broadcastReceiver);
    }


}
