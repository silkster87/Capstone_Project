package com.example.android.newswiz;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.newswiz.Adapters.SectionsPageAdapter;
import com.example.android.newswiz.Fragments.CategoriesSlidePageFragment;
import com.example.android.newswiz.Fragments.CountriesSlidePageFragment;
import com.example.android.newswiz.Fragments.PublishersSlidePageFragment;
import com.example.android.newswiz.Sources.SourcesInfo;
import com.example.android.newswiz.Sources.UserSettings;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements PublishersSlidePageFragment.OnItemClickListener,
        CategoriesSlidePageFragment.OnItemClickListener, CountriesSlidePageFragment.OnItemClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int RC_SIGN_IN = 1;
    public static final String SOURCES = "sources";
    private static final String TAG = MainActivity.class.getSimpleName();

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private DatabaseReference sourcesDB;
    private DatabaseReference settingsDB;


    private Context context;
    private SharedPreferences pref;
    private Resources res;
    private Bundle mSavedInstanceState;
    public static SourcesInfo mFirebaseSourcesInfo;
    private UserSettings mUserSettings;

    private InterstitialAd mInterstitialAd;

    @BindView(R.id.textView_no_items_selected)
    TextView noOfItemsSelected;
    @BindView(R.id.sources_container)
    ViewPager mPager;
    @BindView(R.id.tabs)
    TabLayout tabLayout;
    @BindView(R.id.choose_sources_toolbar)
    Toolbar mToolbar;

    @BindString(R.string.publishers_label) String publishersLabel;
    @BindString(R.string.categories_label) String categoriesLabel;
    @BindString(R.string.countries_label) String countriesLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        res = getResources();
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref.registerOnSharedPreferenceChangeListener(this);
        String themeName = pref.getString(res.getString(R.string.theme_key), res.getString(R.string.theme_default));
        if(themeName.equals(res.getString(R.string.lightLabel))){
            setTheme(R.style.LightTheme);
        } else if (themeName.equals(res.getString(R.string.darkLabel))) {
            setTheme(R.style.DarkTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        sourcesDB = FirebaseDatabase.getInstance().getReference(res.getString(R.string.users_label));
        settingsDB = FirebaseDatabase.getInstance().getReference(res.getString(R.string.users_label));
        mFirebaseAuth = FirebaseAuth.getInstance();

        context = getApplicationContext();

        if(savedInstanceState!=null){
            if(savedInstanceState.containsKey(SOURCES)){
                mFirebaseSourcesInfo = savedInstanceState.getParcelable(SOURCES);
                updateUI(savedInstanceState, mFirebaseSourcesInfo);
            }
        }
        //AdMob - ca-app-pub-3940256099942544~3347511713 is a sample ID for testing. In production use the actual app ID
        MobileAds.initialize(this, res.getString(R.string.Admob_ID));
        mInterstitialAd = new InterstitialAd(this);

        //AdMob "ca-app-pub-3940256099942544/1033173712" is a test ad unit ID. In production use the add ad unit ID.
        mInterstitialAd.setAdUnitId(res.getString(R.string.AdUnit_ID));
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener(){
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                sourcesDB.child(mFirebaseAuth.getCurrentUser().getUid()).child(res.getString(R.string.sources_label)).setValue(mFirebaseSourcesInfo);

                Intent newsArticlesActivity = new Intent(getApplicationContext(), NewsArticles.class);
                newsArticlesActivity.putExtra(SOURCES, mFirebaseSourcesInfo);
                startActivity(newsArticlesActivity);
            }
        });

        mAuthStateListener = firebaseAuth -> {
            FirebaseUser user = firebaseAuth.getCurrentUser();
            if(user != null){
                //Get sourcesInfo object from Firebase DB

                    sourcesDB.child(user.getUid()).child(res.getString(R.string.sources_label)).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            mFirebaseSourcesInfo = null;
                            mFirebaseSourcesInfo = dataSnapshot.getValue(SourcesInfo.class);

                            if(mFirebaseSourcesInfo==null){
                                ArrayList<String> publishersSelected = new ArrayList<>();
                                ArrayList<String> categoriesSelected = new ArrayList<>();
                                ArrayList<String> countriesSelected = new ArrayList<>();
                                mFirebaseSourcesInfo = new SourcesInfo(publishersSelected, categoriesSelected, countriesSelected);
                            }
                            updateUI(savedInstanceState, mFirebaseSourcesInfo);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });


                //Get Settings object from Firebase DB
                settingsDB.child(user.getUid()).child(res.getString(R.string.settings_label)).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUserSettings = null;
                        mUserSettings = dataSnapshot.getValue(UserSettings.class);

                        SharedPreferences.Editor editor = pref.edit();
                        if(mUserSettings!=null){
                            //We can get User settings to write to Shared Preferences

                            String fontSize = mUserSettings.getFontSize();
                            String theme = mUserSettings.getTheme();
                            String topHeadlinesCountry = mUserSettings.getTopHeadlinesCountry();

                            editor.putString(res.getString(R.string.pref_fontSize_key), fontSize);
                            editor.putString(res.getString(R.string.theme_key), theme);
                            editor.putString(res.getString(R.string.widgetKey_top_headlines), topHeadlinesCountry);
                            editor.apply();

                        } else {
                            //mUserSettings is null so we can make default object and save it to Firebase DB
                            String locale = context.getResources().getConfiguration().locale.getCountry();
                            String defaultCountry = locale.toLowerCase();

                            UserSettings newUserSettings = new UserSettings(res.getString(R.string.menu_medium_label), res.getString(R.string.lightLabel), defaultCountry);

                            editor.putString(res.getString(R.string.pref_fontSize_key), newUserSettings.getFontSize());
                            editor.putString(res.getString(R.string.theme_key), newUserSettings.getTheme());
                            editor.putString(res.getString(R.string.widgetKey_top_headlines), newUserSettings.getTopHeadlinesCountry());
                            editor.apply();

                            settingsDB.setValue(newUserSettings);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            } else {
                // user is signed out
                AuthUI.IdpConfig googleIdp = new AuthUI.IdpConfig.GoogleBuilder().build();
                AuthUI.IdpConfig facebookIdp = new AuthUI.IdpConfig.FacebookBuilder().build();
                AuthUI.IdpConfig emailIdp = new AuthUI.IdpConfig.EmailBuilder().build();

                startActivityForResult(
                        AuthUI.getInstance()
                            .createSignInIntentBuilder()
                                .setTheme(R.style.FirebaseLoginTheme)
                            .setIsSmartLockEnabled(false)
                            .setAvailableProviders(Arrays.asList(googleIdp,facebookIdp,emailIdp))
                                .setLogo(R.drawable.news_wiz_logo)
                                .setTosAndPrivacyPolicyUrls(getApplicationContext().getResources().getString(R.string.terms_of_service),
                                        getApplicationContext().getResources().getString(R.string.privacy_policy))
                            .build(),
                        RC_SIGN_IN);
            }
        };

    }

    public static SourcesInfo getSourcesInfo(){
        return mFirebaseSourcesInfo;
    }


    private void updateUI(Bundle savedInstanceState, SourcesInfo mFirebaseSourcesInfo){

        SourcesInfo mSourcesInfo;

        if(mFirebaseSourcesInfo != null){ //User may start app first time but may have account on NewsWiz on another device.
            mSourcesInfo = mFirebaseSourcesInfo;
        }else{
            //Couldn't find user's mSourcesInfo in DB so make a new one
            ArrayList<String> mPublishersSelected = new ArrayList<>();
            ArrayList<String> mCategoriesSelected = new ArrayList<>();
            ArrayList<String> mCountriesSelected = new ArrayList<>();
            mSourcesInfo = new SourcesInfo(mPublishersSelected, mCategoriesSelected, mCountriesSelected);
        }
        mSavedInstanceState = savedInstanceState;
        setupViewPager(mPager, mSourcesInfo, savedInstanceState);
        tabLayout.setupWithViewPager(mPager);
        updateNoOfItemsSelected();
    }

    private void setupViewPager(ViewPager mPager, SourcesInfo mSourcesInfo, Bundle mSavedInstanceState) {

        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());

        Bundle bundle;
        if(mSavedInstanceState == null) {
            bundle = new Bundle();
            bundle.putParcelable(SOURCES, mSourcesInfo);
        } else {
            bundle = mSavedInstanceState;
        }

        PublishersSlidePageFragment mPublishersFragment = new PublishersSlidePageFragment();
        CategoriesSlidePageFragment mCategoriesFragment = new CategoriesSlidePageFragment();
        CountriesSlidePageFragment mCountriesFragment = new CountriesSlidePageFragment();

        mPublishersFragment.setArguments(bundle);
        mCategoriesFragment.setArguments(bundle);
        mCountriesFragment.setArguments(bundle);

        adapter.addFragment(mPublishersFragment, publishersLabel);
        adapter.addFragment(mCountriesFragment, countriesLabel);
        adapter.addFragment(mCategoriesFragment, categoriesLabel);

        mPager.setAdapter(adapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == RC_SIGN_IN){
            if(resultCode == RESULT_OK){
                String message = "Welcome " + mFirebaseAuth.getCurrentUser().getDisplayName() + "!";
                showSnackbar(message);
            }else if (resultCode == RESULT_CANCELED){
                Toast.makeText(this, "Sign in cancelled!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.sign_out_menu:
                AuthUI.getInstance().signOut(this);
                return true;
                default:
                    return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mAuthStateListener != null)
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }


    @Override
    public void onBackPressed() {
        if(mPager.getCurrentItem() == 0){
            super.onBackPressed();
        } else{
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    public void showSnackbar(String message){
        View view = findViewById(R.id.mainCoordinatorLayout);
        int duration = Snackbar.LENGTH_SHORT;
        Snackbar.make(view, message, duration).show();
    }

    @Override
    public void onItemClickPublishers(String item) {
        if(mFirebaseSourcesInfo==null){
            mFirebaseSourcesInfo = new SourcesInfo();
        }
        ArrayList<String> mPublishersSelected = mFirebaseSourcesInfo.getmPublishersSelected();

            if(mPublishersSelected != null){
                if(mPublishersSelected.contains(item)){
                    mPublishersSelected.remove(item);
                    String message = "You have removed " + item;
                    showSnackbar(message);
                } else {
                    mPublishersSelected.add(item);
                    showSnackbar("You have added " + item);
                }
            }

            updateNoOfItemsSelected();
    }

    @Override
    public void onItemClickCategories(String item) {
        if(mFirebaseSourcesInfo==null){
            mFirebaseSourcesInfo = new SourcesInfo();
        }
            ArrayList<String> mCategoriesSelected = mFirebaseSourcesInfo.getmCategoriesSelected();

        if(mCategoriesSelected != null){
            if(mCategoriesSelected.contains(item)){
                mCategoriesSelected.remove(item);
                String message = "You have removed " + item;
                showSnackbar(message);
            } else {
                mCategoriesSelected.add(item);
                String message = "You have added " + item;
                showSnackbar(message);
            }
        }
        updateNoOfItemsSelected();
    }

    @Override
    public void onItemClickCountries(String item) {
        if(mFirebaseSourcesInfo==null){
            mFirebaseSourcesInfo = new SourcesInfo();
        }
        ArrayList<String> mCountriesSelected = mFirebaseSourcesInfo.getmCountriesSelected();

        if(mCountriesSelected != null){
            if(mCountriesSelected.contains(item)){
                mCountriesSelected.remove(item);
                String message = "You have removed " + item;
                showSnackbar(message);
            } else {
                mCountriesSelected.add(item);
                String message = "You have added " + item;
                showSnackbar(message);
            }
        }
        updateNoOfItemsSelected();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SOURCES, mFirebaseSourcesInfo);
    }

    public void updateNoOfItemsSelected(){

        if(mFirebaseSourcesInfo.getmPublishersSelected() != null || mFirebaseSourcesInfo.getmCategoriesSelected() != null || mFirebaseSourcesInfo.getmCountriesSelected() != null) {
            int numberSelected = mFirebaseSourcesInfo.getmPublishersSelected().size() + mFirebaseSourcesInfo.getmCategoriesSelected().size() + mFirebaseSourcesInfo.getmCountriesSelected().size();
            String selectedLabel = Integer.toString(numberSelected) + " selected";
            noOfItemsSelected.setText(selectedLabel);
        } else {
            noOfItemsSelected.setText(context.getResources().getString(R.string.selected_label));
        }
    }

    @OnClick(R.id.clear_all_label)
    public void clear_all_label(){

        sourcesDB.child(mFirebaseAuth.getCurrentUser().getUid()).child(res.getString(R.string.sources_label)).setValue(null);
        mFirebaseSourcesInfo.getmPublishersSelected().clear();
        mFirebaseSourcesInfo.getmCountriesSelected().clear();
        mFirebaseSourcesInfo.getmCategoriesSelected().clear();
        updateNoOfItemsSelected();
        updateUI(mSavedInstanceState, mFirebaseSourcesInfo);
    }

    @OnClick(R.id.done_label)
    public void done_label(){
        if(mFirebaseSourcesInfo.getmCountriesSelected().size()==0 && mFirebaseSourcesInfo.getmCategoriesSelected().size()==0 && mFirebaseSourcesInfo.getmPublishersSelected().size()==0){
            String msg = "Please select news sources";
            showSnackbar(msg);
        }else if(mFirebaseSourcesInfo.getmPublishersSelected().size()==0){
            String msg = "Please select a publisher";
            showSnackbar(msg);
        }else if(mFirebaseSourcesInfo.getmCategoriesSelected().size()==0){
            String msg = "Please select a category";
            showSnackbar(msg);
        }else if(mFirebaseSourcesInfo.getmCountriesSelected().size()==0){
            String msg = "Please select a country";
            showSnackbar(msg);
        }else {
            if(mInterstitialAd.isLoaded()){
                mInterstitialAd.show();
            }else{
                Log.d(TAG, "The interstitial wasn't loaded yet.");
            }

        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(res.getString(R.string.theme_key))){
           recreate();
        }
    }

    @Override
    protected void onDestroy() {
        pref.unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }
}
