package com.example.android.newswiz.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.newswiz.R;

/**
This is the parent News Source Fragment when in tablet landscape mode. It contains 2 child fragments - one for
the RecyclerView of article headings (ArticleHeadingsLHSFragment)and one for the picture and description of a
specific article (ArticleContentRHSFragment).

 The user will see a list of news article headings on the LHS and the article picture and description on the RHS.
As the user clicks on a news article item on the left, the RHS fragment will change picture and description.
*/

public class NewsSourceFragmentTabletLand extends Fragment {

    private TabletRefreshSwipeListener listener;

    public interface TabletRefreshSwipeListener{
        void onSwipeToRefresh();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        listener = (TabletRefreshSwipeListener) context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_news_articles_page_tablet_land, container,false);
        SwipeRefreshLayout tabletSwipeRefreshLayout = rootView.findViewById(R.id.tablet_swipeToRefresh);
        tabletSwipeRefreshLayout.setOnRefreshListener(() -> listener.onSwipeToRefresh());

        return rootView;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Bundle bundle = this.getArguments();

        FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();

        ArticleContentRHSFragment RHSfragment = new ArticleContentRHSFragment();
        ArticleHeadingsLHSFragment LHSfragment = new ArticleHeadingsLHSFragment();

        LHSfragment.setArguments(bundle);

        RHSfragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.LHS_fragment, LHSfragment);
        fragmentTransaction.replace(R.id.RHS_fragment, RHSfragment);

        fragmentTransaction.commit();

        super.onViewCreated(view, savedInstanceState);
    }


}
