package com.example.android.newswiz;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.app.SearchManager;
import android.content.Intent;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.example.android.newswiz.Fragments.NewsSourceFragment;
import com.example.android.newswiz.Sources.ArticleItem;
import com.example.android.newswiz.Utilities.BookmarksViewModel;
import com.example.android.newswiz.Utilities.GetOkHttpResponse;
import com.example.android.newswiz.Utilities.MySuggestionProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * This Search Activity will display the search results the the user has entered in the search dialog.
 * It received the intent and then gets the query.
* */


public class SearchActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>, SharedPreferences.OnSharedPreferenceChangeListener,
NewsSourceFragment.FragmentListener{

    private final static String API_KEY = BuildConfig.NEWS_API_KEY;
    private String queryURL;
    private String jsonStringData;
    private final static String JSON_STRING_KEY = "JSON string key";
    private final static String SEARCH_QUERY = "Search string query";
    public final static String SearchResultsJsonString = "Search results json string";

    //Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef;

    private ArrayList<ArticleItem> bookmarkedArticles;
    private SharedPreferences pref;
    private Resources res;
    private Intent searchIntent;
    private String query = null;
    private ActionBar ab;

   // @BindView(R.id.search_results_recyclerView) RecyclerView mResultsRecyclerView;
    @BindView(R.id.search_results_toolbar) Toolbar mToolbar;


    private Context context;

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

        pref.registerOnSharedPreferenceChangeListener(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ab = getSupportActionBar();

        if(ab != null)
            ab.setDisplayHomeAsUpEnabled(true);

        context = getApplicationContext();
        searchIntent = getIntent();

        mFirebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference(res.getString(R.string.users_label));
        bookmarkedArticles = new ArrayList<>();

        getListOfBookmarkedArticles();

        if(savedInstanceState == null){
            createSearchResults();
        } else {
            jsonStringData = savedInstanceState.getString(JSON_STRING_KEY);
            query = savedInstanceState.getString(SEARCH_QUERY);
            setUpFragment(jsonStringData);
        }

        if(ab!=null)
        ab.setTitle("Search results: " + query);

    }

    private void setUpFragment(String jsonStringData) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        NewsSourceFragment newsSourceFragment = new NewsSourceFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SearchResultsJsonString, jsonStringData);
        bundle.putParcelableArrayList(NewsArticles.bookmarksKey, bookmarkedArticles);
        newsSourceFragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.search_results_fragment, newsSourceFragment);
        fragmentTransaction.commit();
    }

    private void getListOfBookmarkedArticles() {

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

    @SuppressLint("StaticFieldLeak")
    @Override
    public Loader<String> onCreateLoader(int i, Bundle bundle) {
        return new AsyncTaskLoader<String>(this) {

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Nullable
            @Override
            public String loadInBackground() {

                String resultsJSON = null;
                Request request = new Request.Builder().url(queryURL).build();
                OkHttpClient client = new OkHttpClient();
                GetOkHttpResponse getOkHttpResponse = new GetOkHttpResponse(client, request);
                try {
                    resultsJSON = getOkHttpResponse.run();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return resultsJSON;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {

        if(data == null){
            //Show error message
            Log.e(SearchActivity.class.getSimpleName(), res.getString(R.string.error_no_data));
        } else {
            //Set up UI with JSON string
            jsonStringData = data;
            setUpFragment(data);
        }
    }

    @Override
    public void onLoaderReset(Loader<String> loader) {

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(res.getString(R.string.theme_key))){
            //The onCreate() method will set the theme from the sharedPreferences.
            recreate();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(JSON_STRING_KEY, jsonStringData);
        outState.putString(SEARCH_QUERY, query);
    }

    @Override
    protected void onDestroy() {
        pref.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void onFragmentClick(String TAG, ArticleItem articleItem) {
        switch (TAG) {
            case NewsArticles.clickedSwipeToRefreshTAG:
                createSearchResults();
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
                        String message = "You have removed " + articleItem.getTitle();
                        showSnackBar(message);
                    } else {
                        bookmarkedArticles.add(articleItem);
                        String message = "You have added " + articleItem.getTitle();
                        showSnackBar(message);
                    }
                }
                databaseRef.child(mFirebaseAuth.getCurrentUser().getUid()).child(res.getString(R.string.bookmarks_label)).setValue(bookmarkedArticles);

                break;
        }
    }

    private void createSearchResults() {

        if(Intent.ACTION_SEARCH.equals(searchIntent.getAction())){
            query = searchIntent.getStringExtra(SearchManager.QUERY);

            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);

            suggestions.saveRecentQuery(query, null);

            String newsAPIStartPoint = res.getString(R.string.newsapi_startpoint);
            queryURL = newsAPIStartPoint + "q=" + query + "&apiKey=" + API_KEY;
            LoaderManager.LoaderCallbacks<String> callback = SearchActivity.this;
            getSupportLoaderManager().initLoader(100, null, callback);
        }

        if(ab!=null)
            ab.setTitle("Search results: " + query);
    }

    private void showSnackBar(String message) {
        View view = findViewById(R.id.search_results_coord_layout);
        int duration = Snackbar.LENGTH_SHORT;
        Snackbar.make(view, message, duration).show();
    }


}
