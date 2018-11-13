package com.example.android.newswiz.Fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.newswiz.Sources.ArticleItem;
import com.example.android.newswiz.NewsArticles;
import com.example.android.newswiz.R;
import com.example.android.newswiz.Utilities.BookmarksViewModel;
import com.example.android.newswiz.Utilities.SharedViewModel;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This is a fragment used in the tablet mode master detail layout on the RHS. It will upload the selected
 * article's images and description selected. Initially it will upload the first article (position 0). It
 * makes use of a ViewModel to observe any changes that the user has clicked on the LHS fragment to update itself.
 * */
public class ArticleContentRHSFragment extends Fragment {

    private String mArrayListOfJSONResults;
    private List<ArticleItem> listOfArticles;
    private ArrayList<ArticleItem> mBookmarkedArticles;
    private final static String mArticles = "articles";
    private BookmarksViewModel bookmarksViewModel;

    private OnBookmarkSelectedListener listener;


    @BindView(R.id.article_image_rhs) ImageView articleImage;
    @BindView(R.id.article_desc_rhs) TextView articleTextViewDesc;
    @BindView(R.id.article_bookmark_tablet) ImageView articleBookmark;
    @BindView(R.id.article_share_tablet) ImageView articleShare;
    @BindView(R.id.rhs_fragment_coordinatorLayout) ConstraintLayout rhsFragmentConstraintLayout;

    private SharedViewModel model;

    private SharedPreferences sharedPreferences;
    private Resources res;
    private float fontSize;
    private int position = 0;

    public interface OnBookmarkSelectedListener{
        void onFragmentClick(String TAG, ArticleItem articleItem);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            listener = (OnBookmarkSelectedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
            + " must implement OnBookmarkSelectedListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(getParentFragment()).get(SharedViewModel.class);
        bookmarksViewModel = ViewModelProviders.of(getParentFragment()).get(BookmarksViewModel.class);
        mBookmarkedArticles = new ArrayList<>();
        mBookmarkedArticles = bookmarksViewModel.getBookmarks().getValue();
        res = getResources();
        setUpSharedPreferences();
    }

    private void setUpSharedPreferences() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getParentFragment().getContext());
        fontSize = convertFontSize();
    }

    private float convertFontSize() {

        String fontSizeString = sharedPreferences.getString(res.getString(R.string.pref_fontSize_key),
                res.getString(R.string.fontSize_default));

        switch(fontSizeString){
            case "Small":
                return 12;
            case "Medium":
                return 18;
            case "Large":
                return 24;
            default:
                return 18;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_rhs_tablet, container,false);

        ButterKnife.bind(this, rootView);

        String theme = sharedPreferences.getString(res.getString(R.string.theme_key), res.getString(R.string.theme_default));

        if(theme.equals(res.getString(R.string.lightLabel))){
            articleShare.setImageDrawable(res.getDrawable(R.drawable.share_black));
        }else{
            articleShare.setImageDrawable(res.getDrawable(R.drawable.share_white));
        }

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.containsKey(NewsArticles.publishersJSONStringKey)) {
                mArrayListOfJSONResults = bundle.getString(NewsArticles.publishersJSONStringKey);
            }else {
                mArrayListOfJSONResults = bundle.getString(NewsArticles.countriesAndCategoriesJSONStringKey);
            }
        }

        try {
            makeListOfArticles();
        }catch (JSONException e){
            e.printStackTrace();
        }

        return rootView;

    }

    private void makeListOfArticles() throws JSONException {

        if(mArrayListOfJSONResults != null){
            JSONObject newsArticlesJSONresponse = new JSONObject(mArrayListOfJSONResults);
            JSONArray newsArticlesJSONArray = newsArticlesJSONresponse.getJSONArray(mArticles);

            if(newsArticlesJSONArray == null) throw new AssertionError();
            listOfArticles = new ArrayList<>();

            for(int i = 0; i < newsArticlesJSONArray.length(); i++){
                JSONObject jsonArticle = (JSONObject) newsArticlesJSONArray.get(i);
                String jsonString = jsonArticle.toString();
                Gson gson = new Gson();
                ArticleItem articleItem = gson.fromJson(jsonString, ArticleItem.class);

                //For the source JSON Object
                JSONObject newsArticleSourceJSONObject = jsonArticle.getJSONObject(res.getString(R.string.source_jsonkey));
                String source_id = newsArticleSourceJSONObject.getString(res.getString(R.string.id_jsonkey));
                String source_name = newsArticleSourceJSONObject.getString(res.getString(R.string.name_jsonkey));

                articleItem.setSource_id(source_id);
                articleItem.setSource_name(source_name);
                listOfArticles.add(articleItem);
            }
        }

        Picasso.get().load(listOfArticles.get(0).getUrlToImage()).into(articleImage);
        articleTextViewDesc.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);
        articleTextViewDesc.setText(listOfArticles.get(0).getDescription());

        model.getSelected().observe(this, position ->{
            Picasso.get().load(listOfArticles.get(position).getUrlToImage()).into(articleImage);
            articleTextViewDesc.setText(listOfArticles.get(position).getDescription());
            setBookmarkIcon(mBookmarkedArticles, position);
            this.position = position;
        } );

        articleBookmark.setOnClickListener(view ->
            listener.onFragmentClick(NewsArticles.clickedArticleBookmarkTAG, listOfArticles.get(position))
        );

        bookmarksViewModel.getBookmarks().observe(this, bookmarkedArticles -> {
            setBookmarkIcon(bookmarkedArticles, position);
        });

        rhsFragmentConstraintLayout.setOnClickListener(view -> {
            Uri webPage = Uri.parse(listOfArticles.get(position).getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
            if(intent.resolveActivity(getActivity().getPackageManager())!=null){
                startActivity(intent);
            }
        });

    }

    public void setBookmarkIcon(ArrayList<ArticleItem> bookmarks, int position){
        String theme = sharedPreferences.getString(res.getString(R.string.theme_key), res.getString(R.string.theme_default));
        if(bookmarks!=null){
            if(bookmarks.contains(listOfArticles.get(position))){
                //Item has already been bookmarked
                if(theme.equals(res.getString(R.string.darkLabel))){
                    articleBookmark.setImageDrawable(res.getDrawable(R.drawable.baseline_bookmark_white_18dp));
                }else {
                    articleBookmark.setImageDrawable(res.getDrawable(R.drawable.baseline_bookmark_black_18dp));
                }
            } else {
                //Item has not been bookmarked
                if(theme.equals(res.getString(R.string.darkLabel))){
                    articleBookmark.setImageDrawable(res.getDrawable(R.drawable.baseline_bookmark_border_white_18dp));
                }else{
                    articleBookmark.setImageDrawable(res.getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                }
            }
            mBookmarkedArticles = bookmarks;
        }

    }
}
