package com.example.android.newswiz.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.newswiz.NewsArticles;
import com.example.android.newswiz.Adapters.NewsArticlesAdapter;
import com.example.android.newswiz.R;
import com.example.android.newswiz.SearchActivity;
import com.example.android.newswiz.Sources.ArticleItem;
import com.example.android.newswiz.Utilities.BookmarksViewModel;
import com.squareup.picasso.Picasso;


/**
This NewsSourceFragment class will upload the news articles from the JSON string passed to it in a Recycler View. This type of fragment
will be used for the phone and tablet portrait mode. For tablet landscape we will use the NewsSourceFragmentTabletLand Fragment which will
incorporate a Master Detail Flow layout to separate the logic.

The JSON string will come from a Bundle object and will either have the publisherJSON key or the countriesAndCategoriesJSON key.
*/

public class NewsSourceFragment extends Fragment{

    private String mArrayListOfJSONResults;
    private FragmentListener listener;
    private BookmarksViewModel bookmarksViewModel;
    private Picasso picasso;

    public interface FragmentListener {
        void onFragmentClick(String TAG, ArticleItem articleItem);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (FragmentListener) context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bookmarksViewModel = ViewModelProviders.of(this).get(BookmarksViewModel.class);
        picasso = Picasso.get();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_news_articles_page, container, false);

        SwipeRefreshLayout mySwipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);

        mySwipeRefreshLayout.setOnRefreshListener(() -> listener.onFragmentClick(NewsArticles.clickedSwipeToRefreshTAG, null));

        RecyclerView mRecyclerView = rootView.findViewById(R.id.news_articles_fragment_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 1));
        mRecyclerView.setHasFixedSize(true);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey(NewsArticles.publishersJSONStringKey)) {
                mArrayListOfJSONResults = bundle.getString(NewsArticles.publishersJSONStringKey);
            }else if(bundle.containsKey(NewsArticles.countriesAndCategoriesJSONStringKey)) {
                mArrayListOfJSONResults = bundle.getString(NewsArticles.countriesAndCategoriesJSONStringKey);
            }

            if(bundle.containsKey(SearchActivity.SearchResultsJsonString)){
                mArrayListOfJSONResults = bundle.getString(SearchActivity.SearchResultsJsonString);
            }
        }

        NewsArticlesAdapter mArticlesAdapter = new NewsArticlesAdapter((TAG, articleItem) ->
                listener.onFragmentClick(TAG, articleItem), mArrayListOfJSONResults, getContext(), bookmarksViewModel, picasso);
        mRecyclerView.setAdapter(mArticlesAdapter);

        return rootView;
    }
}
