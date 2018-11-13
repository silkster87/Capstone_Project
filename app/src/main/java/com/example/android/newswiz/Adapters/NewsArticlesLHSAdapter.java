package com.example.android.newswiz.Adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.newswiz.R;
import com.example.android.newswiz.Sources.ArticleItem;
import com.example.android.newswiz.Utilities.TranslateSources;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This Adapter will be specifically be used to display the LHS fragment of News titles only
 *
*
* */


public class NewsArticlesLHSAdapter extends RecyclerView.Adapter<NewsArticlesLHSAdapter.ArticleViewHolder> {

    private String mJSONString;
    private List<ArticleItem> listOfArticles;
    private final static String mArticles = "articles";
    private int row_index;
    private Context context;

    private OnArticleItemSelectedListener listener;

    public interface OnArticleItemSelectedListener{
        void onArticlePositionSelected(int position);
    }


    public NewsArticlesLHSAdapter(Context context, String mArrayListOfJSONResults, OnArticleItemSelectedListener listener, int positionSelected){
        this.context = context;
        this.listener = listener;
        this.row_index = positionSelected;
        this.mJSONString = mArrayListOfJSONResults;
        try{
            makeListOfArticles();
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private void makeListOfArticles() throws JSONException {

        if(mJSONString != null){
            JSONObject newsArticlesJSONresponse = new JSONObject(mJSONString);
            JSONArray newsArticlesJSONArray = newsArticlesJSONresponse.getJSONArray(mArticles);

            if(newsArticlesJSONArray == null) throw new AssertionError();
            listOfArticles = new ArrayList<>();

            for(int i = 0; i < newsArticlesJSONArray.length(); i++){
                JSONObject jsonArticle = (JSONObject) newsArticlesJSONArray.get(i);
                String jsonString = jsonArticle.toString();
                Gson gson = new Gson();
                ArticleItem articleItem = gson.fromJson(jsonString, ArticleItem.class);

                Resources res = context.getResources();

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

    @NonNull
    @Override
    public ArticleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.news_article_lhs_item, parent, false);

        return new ArticleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticleViewHolder holder, int position) {
        holder.bind(position, listener);

        String publishedDate = listOfArticles.get(position).getPublishedAt();
        String formattedDate = TranslateSources.formatPublishedDate(publishedDate);

        holder.mArticleTitleTextView.setText(listOfArticles.get(position).getTitle());
        holder.mArticlePublisherTextView.setText(listOfArticles.get(position).getAuthor());
        holder.mArticleDateTextView.setText(formattedDate);

    }

    @Override
    public int getItemCount() {
        if(listOfArticles == null) return 0;
        return listOfArticles.size();
    }


    public class ArticleViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.LHS_article_constraint_layout)
        ConstraintLayout mArticleConstraintLayout;
        @BindView(R.id.LHS_article_title)
        TextView mArticleTitleTextView;
        @BindView(R.id.LHS_article_publisher)
        TextView mArticlePublisherTextView;
        @BindView(R.id.LHS_article_date)
        TextView mArticleDateTextView;

        public ArticleViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }


        public void bind(final int position,final OnArticleItemSelectedListener listener) {
            itemView.setOnClickListener(view -> {
                listener.onArticlePositionSelected(position);
                row_index = position;
            }
            );

            if(row_index == position){
                //Highlight color if position is selected
                mArticleConstraintLayout.setBackgroundColor(Color.parseColor("#0097A7"));
            } else {
                mArticleConstraintLayout.setBackgroundColor(Color.TRANSPARENT);
            }
        }
    }
}
