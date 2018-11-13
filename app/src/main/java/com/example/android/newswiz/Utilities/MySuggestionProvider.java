package com.example.android.newswiz.Utilities;

import android.content.SearchRecentSuggestionsProvider;

/**
 * This is a content provider for recent query suggestions whenever the user wants to search for
 * news articles.
*
* */


public class MySuggestionProvider extends SearchRecentSuggestionsProvider {

    public final static String AUTHORITY = "com.example.android.newswiz.Utilities.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider(){
        setupSuggestions(AUTHORITY, MODE);
    }
}
