package com.example.android.newswiz.Adapters;

import android.app.AlertDialog;
import android.arch.lifecycle.LifecycleOwner;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.newswiz.NewsArticles;
import com.example.android.newswiz.R;
import com.example.android.newswiz.Sources.ArticleItem;
import com.example.android.newswiz.Utilities.BookmarksViewModel;
import com.example.android.newswiz.Utilities.TranslateSources;
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
 * This class displays the list of News Articles from the JSON String of results. It also implements a shared preference
 * change listener and ViewModel to change the UI of the articles accordingly.
* */

public class NewsArticlesAdapter extends RecyclerView.Adapter<NewsArticlesAdapter.ArticleViewHolder> implements SharedPreferences.OnSharedPreferenceChangeListener {


    private String mJSONString;
    private List<ArticleItem> listOfArticles;
    private final static String mArticles = "articles";
    private final OnArticleClickListener listener;
    private Context context;
    private SharedPreferences sharedPreferences;
    private float fontSize;
    private Resources res;
    private BookmarksViewModel bookmarksViewModel;
    private Picasso picasso;

    public interface OnArticleClickListener {
        void onArticleItemClick(String TAG, ArticleItem articleItem);
    }

    public NewsArticlesAdapter(OnArticleClickListener listener, String mArrayListOfJSONResults,
                               Context context, BookmarksViewModel bookmarksViewModel, Picasso picasso) {
        this.listener = listener;
        this.mJSONString = mArrayListOfJSONResults;
        this.context = context;
        this.bookmarksViewModel = bookmarksViewModel;
        this.picasso = picasso;
        res = context.getResources();
        setUpSharedPreferences();
        try {
            makeListOfArticles();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void setUpSharedPreferences(){
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        fontSize = convertFontSize();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private float convertFontSize(){

        String fontSizeString = sharedPreferences.getString(res.getString(R.string.pref_fontSize_key), res.getString(R.string.fontSize_default));

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
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if(key.equals(res.getString(R.string.pref_fontSize_key))){
            convertFontSize();
            notifyDataSetChanged();
        }
    }


    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        super.onDetachedFromRecyclerView(recyclerView);
    }

    
    private void makeListOfArticles() throws JSONException {

        if(mJSONString != null){
            JSONObject newsArticlesJSONresponse = new JSONObject(mJSONString);

            String status = newsArticlesJSONresponse.getString(res.getString(R.string.status_key));
            if(status.equals("ok")){
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

           }
    }

    @NonNull
    @Override
    public NewsArticlesAdapter.ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForArticleItem = R.layout.news_article_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForArticleItem, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        holder.bind(listOfArticles.get(position), listener, listOfArticles.get(position).getTitle());
        String articleImage = listOfArticles.get(position).getUrlToImage();
        String publishedAtDate = listOfArticles.get(position).getPublishedAt();
        String formattedDate = TranslateSources.formatPublishedDate(publishedAtDate);

        String mAuthor = listOfArticles.get(position).getAuthor();
        if(mAuthor != null){
            if(!mAuthor.contains("http")) {
                holder.mArticleAuthorTextView.setText(mAuthor);
            }else {
                holder.mArticleAuthorTextView.setPadding(0,0,0,0);
                holder.mArticleAuthorTextView.setText("");
            }
        } else {
            holder.mArticleAuthorTextView.setPadding(0,0,0,0);
            holder.mArticleAuthorTextView.setText("");
        }

        if(TextUtils.isEmpty(articleImage)){
            holder.mArticleImageView.setVisibility(View.GONE);
        }else {
            holder.mArticleImageView.setVisibility(View.VISIBLE);
            picasso.load(articleImage).into(holder.mArticleImageView);
        }
        holder.mArticleTitleTextView.setText(listOfArticles.get(position).getTitle());
        holder.mArticlePublishedTextView.setText(formattedDate);
        holder.mArticleDescTextView.setText(listOfArticles.get(position).getDescription());

            bookmarksViewModel.getBookmarks().observe((LifecycleOwner) context, bookmarkedArticles -> {

                if(bookmarkedArticles!=null){
                    if(bookmarkedArticles.contains(listOfArticles.get(position))){
                        //Item has already been bookmarked
                        if(holder.theme.equals(res.getString(R.string.darkLabel))){
                            holder.mBookmarkImageView.setImageDrawable(res.getDrawable(R.drawable.baseline_bookmark_white_18dp));
                        }else {
                            holder.mBookmarkImageView.setImageDrawable(res.getDrawable(R.drawable.baseline_bookmark_black_18dp));
                        }
                    } else {
                        //Item has not been bookmarked
                        if(holder.theme.equals(res.getString(R.string.darkLabel))){
                            holder.mBookmarkImageView.setImageDrawable(res.getDrawable(R.drawable.baseline_bookmark_border_white_18dp));
                        }else {
                            holder.mBookmarkImageView.setImageDrawable(res.getDrawable(R.drawable.baseline_bookmark_border_black_18dp));
                        }
                    }
                }

            } );


    }

    @Override
    public int getItemCount() { //This will be the sum of all the articles in the JSON string
        if(listOfArticles == null) return 0;
        return listOfArticles.size();
    }

    public class ArticleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.article_title)
        TextView mArticleTitleTextView;
        @BindView(R.id.article_author)
        TextView mArticleAuthorTextView;
        @BindView(R.id.article_published_time)
        TextView mArticlePublishedTextView;
        @BindView(R.id.article_desc)
        TextView mArticleDescTextView;
        @BindView(R.id.article_image)
        ImageView mArticleImageView;
        @BindView(R.id.bookmark_article)
        ImageView mBookmarkImageView;
        @BindView(R.id.share_article)
        ImageView mShareImageView;

        String theme;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mArticleDescTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSize);

            theme = sharedPreferences.getString(res.getString(R.string.theme_key), res.getString(R.string.theme_default));

            if(theme.equals("Light")){
                mShareImageView.setImageDrawable(res.getDrawable(R.drawable.share_black));
            }else{
                mShareImageView.setImageDrawable(res.getDrawable(R.drawable.share_white));
            }

        }

        public void bind(final ArticleItem articleItem, final OnArticleClickListener listener, final String title) {
            itemView.setOnClickListener(view -> {
                listener.onArticleItemClick(NewsArticles.clickedArticleURLTAG, articleItem);
                //When user clicks on article, it will open website for article.
            });

            mShareImageView.setOnClickListener(view -> {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, articleItem.getUrl());
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
            });

            mBookmarkImageView.setOnClickListener(view ->
                    listener.onArticleItemClick(NewsArticles.clickedArticleBookmarkTAG, articleItem));
        }


    }


}
