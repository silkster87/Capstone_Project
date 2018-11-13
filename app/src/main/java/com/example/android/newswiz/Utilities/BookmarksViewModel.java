package com.example.android.newswiz.Utilities;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.example.android.newswiz.Sources.ArticleItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * This ViewModel class is to keep track of the articles that the user has bookmarked. When the user has bookmarked/un-bookmarked
 * an articles then changes in the UI will update accordingly in the adapter.
* */

public class BookmarksViewModel extends ViewModel {

    private FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("users");
    private MutableLiveData<ArrayList<ArticleItem>> bookmarkedArticles;

    public LiveData<ArrayList<ArticleItem>> getBookmarks(){

        if(bookmarkedArticles == null){
            bookmarkedArticles = new MutableLiveData<>();
        }
        databaseRef.child(mFirebaseAuth.getCurrentUser().getUid()).child("bookmarks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //Clear the previous list
                ArrayList<ArticleItem> newBookmarks = new ArrayList<>();

                //Iterating through all nodes
                for(DataSnapshot dataSnapshot1: dataSnapshot.getChildren()){
                    ArticleItem articleItem = dataSnapshot1.getValue(ArticleItem.class);
                    newBookmarks.add(articleItem);
                }
                bookmarkedArticles.setValue(newBookmarks);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return bookmarkedArticles;
    }
}
