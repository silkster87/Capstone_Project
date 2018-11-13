package com.example.android.newswiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.example.android.newswiz.Sources.UserSettings;
import com.example.android.newswiz.Widgets.ArticlesWidgetUpdateService;
import com.example.android.newswiz.Utilities.DeleteAccountPreference;
import com.example.android.newswiz.Widgets.NewsWizWidget;
import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * This Settings Activity is where the user can choose what size text to display as well as set the
 * color themes for the app. The details in the settings will be saved in the firebase database instance
 * for the user.
* */

public class SettingsActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener,
        DeleteAccountPreference.deleteAccountListener {

    private SharedPreferences pref;
    private Resources res;
    private DatabaseReference settingsDB;
    private UserSettings mUserSettings;

    @BindView(R.id.settings_activity_toolbar)
    Toolbar mToolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        res = getResources();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        settingsDB = FirebaseDatabase.getInstance().getReference(res.getString(R.string.users_label)).child(mFirebaseAuth.getCurrentUser().getUid()).child(res.getString(R.string.settings_label));

        getUserSettings();

        String themeName = pref.getString(res.getString(R.string.theme_key), res.getString(R.string.theme_default));
        if(themeName.equals(res.getString(R.string.lightLabel))){
            setTheme(R.style.LightTheme);
        } else if (themeName.equals(res.getString(R.string.darkLabel))) {
            setTheme(R.style.DarkTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        ActionBar actionBar = this.getSupportActionBar();

        if(actionBar!= null)
        actionBar.setDisplayHomeAsUpEnabled(true);


    }

    private void getUserSettings(){
        String fontSize = pref.getString(res.getString(R.string.pref_fontSize_key), res.getString(R.string.menu_medium_label));
        String theme = pref.getString(res.getString(R.string.theme_key), res.getString(R.string.darkLabel));

        String locale = getResources().getConfiguration().locale.getCountry();
        String defaultCountry = locale.toLowerCase();
        String topHeadlinesCountry = pref.getString(res.getString(R.string.widgetKey_top_headlines), defaultCountry );

        mUserSettings = new UserSettings(fontSize, theme, topHeadlinesCountry);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                return true;
        }
            return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(res.getString(R.string.theme_key))){
            String theme = pref.getString(key, res.getString(R.string.darkLabel));
            mUserSettings.setTheme(theme);
            recreate();
        }else if(key.equals(res.getString(R.string.widgetKey_top_headlines))){
            ArticlesWidgetUpdateService.startActionUpdateNewsArticles(this, NewsWizWidget.topHeadlinesUpdate);
            String locale = getResources().getConfiguration().locale.getCountry();
            String defaultCountry = locale.toLowerCase();
            String topHeadlinesCountry = pref.getString(key, defaultCountry);
            mUserSettings.setTopHeadlinesCountry(topHeadlinesCountry);

        }else if(key.equals(res.getString(R.string.pref_fontSize_key))){
            String fontSize = pref.getString(key, res.getString(R.string.menu_medium_label));
            mUserSettings.setFontSize(fontSize);
        }

        settingsDB.setValue(mUserSettings);
    }

    @Override
    protected void onDestroy() {
        pref.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

    @Override
    public void deleteAccount() {

        //We sign user out and take user to login activity ensuring that it is at bottom of stack
        AuthUI.getInstance().signOut(getApplicationContext());
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(res.getString(R.string.logout_action));
        sendBroadcast(broadcastIntent);
        finish();

    }
}
