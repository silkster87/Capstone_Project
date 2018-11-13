package com.example.android.newswiz.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.newswiz.NewsArticles;
import com.example.android.newswiz.Adapters.NewsArticlesLHSAdapter;
import com.example.android.newswiz.R;
import com.example.android.newswiz.Utilities.SharedViewModel;

/**
 * This ArticleHeadingsLHSFragment is a child fragment of the NewsSourceFragmentTabletLand. It uses the NewsArticlesLHSAdapter
 * to display the list of article headings. This class uses a ViewModel to help communicate to the RHS fragment to update it.
 * */


public class ArticleHeadingsLHSFragment extends Fragment {

    private String mArrayListOfJSONResults;
    private SharedViewModel model;
    private int positionSelected = 0;



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        model = ViewModelProviders.of(getParentFragment()).get(SharedViewModel.class);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_lhs_tablet, container, false);

        RecyclerView mRecyclerView = rootView.findViewById(R.id.LHS_fragment_recyclerView);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
        mRecyclerView.setHasFixedSize(true);


        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey(NewsArticles.publishersJSONStringKey)) {
                mArrayListOfJSONResults = bundle.getString(NewsArticles.publishersJSONStringKey);
            }else {
                mArrayListOfJSONResults = bundle.getString(NewsArticles.countriesAndCategoriesJSONStringKey);
            }
        }

        model.getSelected().observe(this, positionSelectedUpdate -> {
                mRecyclerView.getAdapter().notifyDataSetChanged();
                positionSelected = positionSelectedUpdate;
        });

        if(model.getSelected().getValue()!=null)
        positionSelected = model.getSelected().getValue();

        Context context = getContext();

        NewsArticlesLHSAdapter mArticlesLHSAdapter = new NewsArticlesLHSAdapter(context, mArrayListOfJSONResults, position -> model.select(position), positionSelected);
        mRecyclerView.setAdapter(mArticlesLHSAdapter);


        return rootView;
    }

}
