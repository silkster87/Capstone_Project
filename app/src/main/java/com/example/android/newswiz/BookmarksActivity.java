package com.example.android.newswiz;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.MenuItem;

import com.example.android.newswiz.Adapters.BookmarkedArticlesAdapter;
import com.example.android.newswiz.Adapters.NewsArticlesAdapter;
import com.example.android.newswiz.Sources.ArticleItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This Bookmarks Activity will display all the news articles that the user has bookmarked. The
 * activity will retrieve the user's bookmarks from Firebase DB and then display them in a recycler view.
 * The user will have the option to delete/remove the bookmark using a swipe gesture. This will in
 * turn update the Firebase DB list of bookmarks.
 *
* */


public class BookmarksActivity extends AppCompatActivity {

    @BindView(R.id.bookmarks_activity_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.bookmarks_recyclerView)
    RecyclerView mRecyclerView;

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference databaseRef;
    private ArrayList<ArticleItem> bookmarkedArticles;
    private Resources res;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);

        res = getResources();
        mFirebaseAuth = FirebaseAuth.getInstance();
        databaseRef = FirebaseDatabase.getInstance().getReference(res.getString(R.string.users_label));
        bookmarkedArticles = new ArrayList<>();

        getListOfBookmarkedArticles();

        ActionBar ab = getSupportActionBar();
        if(ab!=null){
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(res.getString(R.string.bookmarked_articles));
        }
        setUpBookmarkedArticles();
    }

    private void getListOfBookmarkedArticles() { //bookmarkedArticles will only update if there is a data change the initial bookmarks we get from the intent.
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

    private void setUpBookmarkedArticles() {
        bookmarkedArticles = getIntent().getParcelableArrayListExtra(NewsArticles.bookmarksKey);

        BookmarkedArticlesAdapter bookmarksAdapter = new BookmarkedArticlesAdapter(this, bookmarkedArticles, new NewsArticlesAdapter.OnArticleClickListener() {
            @Override
            public void onArticleItemClick(String TAG, ArticleItem articleItem) {
                if(TAG.equals(NewsArticles.clickedArticleURLTAG)){
                    Uri webpage = Uri.parse(articleItem.getUrl());
                    Intent intent = new Intent(Intent.ACTION_VIEW, webpage);
                    if(intent.resolveActivity(getApplicationContext().getPackageManager())!=null){
                        startActivity(intent);
                    }
                }
            }
        });
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(bookmarksAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                    ArticleItem deleteArticleItem = (ArticleItem) viewHolder.itemView.getTag();
                    bookmarkedArticles.remove(deleteArticleItem);
                    databaseRef.child(mFirebaseAuth.getCurrentUser().getUid()).child(res.getString(R.string.bookmarks_label)).setValue(bookmarkedArticles);
                    mRecyclerView.getAdapter().notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });
        itemTouchHelper.attachToRecyclerView(mRecyclerView);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
