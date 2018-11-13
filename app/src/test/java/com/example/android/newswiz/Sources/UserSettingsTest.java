package com.example.android.newswiz.Sources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class UserSettingsTest {

    private UserSettings mUserSettings;

    @Before
    public void setUpUserSettings(){
        String fontSize = "Medium";
        String theme = "Dark";
        String topHeadlinesCountry = "U.K";

        mUserSettings = new UserSettings(fontSize, theme, topHeadlinesCountry);

    }

    @Test
    public void testUserSettingsReturnsFontSize(){
        assertEquals("Medium", mUserSettings.getFontSize());
    }

    @Test
    public void testUserSettingsReturnsTheme(){
        assertEquals("Dark", mUserSettings.getTheme());
    }

    @Test
    public void testUserSettingsReturnsTopHeadlinesCountry(){
        assertEquals("U.K", mUserSettings.getTopHeadlinesCountry());
    }

}