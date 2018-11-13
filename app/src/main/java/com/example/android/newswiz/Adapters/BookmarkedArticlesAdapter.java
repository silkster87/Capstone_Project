package com.example.android.newswiz.Adapters;

import android.content.Context;
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
import com.example.android.newswiz.Utilities.TranslateSources;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BookmarkedArticlesAdapter extends RecyclerView.Adapter<BookmarkedArticlesAdapter.ArticleViewHolder> implements SharedPreferences.OnSharedPreferenceChangeListener{

    private Context context;
    private List<ArticleItem> bookmarkedArticles;
    private Resources res;
    private SharedPreferences sharedPreferences;
    private final NewsArticlesAdapter.OnArticleClickListener listener;

    public BookmarkedArticlesAdapter(Context context, ArrayList<ArticleItem> bookmarkedArticles, NewsArticlesAdapter.OnArticleClickListener listener){
        this.context = context;
        this.listener = listener;
        res = context.getResources();
        this.bookmarkedArticles = bookmarkedArticles;
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }
    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForArticleItem = R.layout.news_article_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(layoutIdForArticleItem, parent, false);
        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        holder.bind(bookmarkedArticles.get(position), listener, bookmarkedArticles.get(position).getTitle());
        String articleImage = bookmarkedArticles.get(position).getUrlToImage();
        String publishedAtDate = bookmarkedArticles.get(position).getPublishedAt();
        String formattedDate = TranslateSources.formatPublishedDate(publishedAtDate);

        String mAuthor = bookmarkedArticles.get(position).getAuthor();
        if(mAuthor != null){
            if(!mAuthor.contains("http")){
                holder.mArticleAuthorTextView.setText(mAuthor);
            } else {
                holder.mArticleAuthorTextView.setPadding(0,0,0,0);
                holder.mArticleAuthorTextView.setText("");
            }
        } else {
            holder.mArticleAuthorTextView.setPadding(0,0,0,0);
            holder.mArticleAuthorTextView.setText("");
        }

        if(TextUtils.isEmpty(articleImage)){
            holder.mArticleImageView.setVisibility(View.GONE);
        } else{
            holder.mArticleImageView.setVisibility(View.VISIBLE);
            Picasso.get().load(articleImage).into(holder.mArticleImageView);
        }
        holder.mArticleTitleTextView.setText(bookmarkedArticles.get(position).getTitle());
        holder.mArticlePublishedTextView.setText(formattedDate);
        holder.mArticleDescTextView.setText(bookmarkedArticles.get(position).getDescription());
        holder.itemView.setTag(bookmarkedArticles.get(position));
    }

    @Override
    public int getItemCount() {
        if(bookmarkedArticles == null) return 0;
        return bookmarkedArticles.size();
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
            mArticleDescTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, convertFontSize());
            mBookmarkImageView.setVisibility(View.INVISIBLE); //We are already showing bookmarks so no need to bookmark it again

            theme = sharedPreferences.getString(res.getString(R.string.theme_key), res.getString(R.string.theme_default));

            if(theme.equals(res.getString(R.string.lightLabel))){
                mShareImageView.setImageDrawable(res.getDrawable(R.drawable.share_black));
            }else{
                mShareImageView.setImageDrawable(res.getDrawable(R.drawable.share_white));
            }

        }

        public void bind(ArticleItem articleItem, NewsArticlesAdapter.OnArticleClickListener listener, String title) {
            itemView.setOnClickListener(vew -> {
                listener.onArticleItemClick(NewsArticles.clickedArticleURLTAG, articleItem);
            });

            mShareImageView.setOnClickListener(view -> {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, articleItem.getUrl());
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, title);
                sendIntent.setType("text/plain");
                context.startActivity(Intent.createChooser(sendIntent, context.getResources().getText(R.string.send_to)));
            });
        }
    }
}
