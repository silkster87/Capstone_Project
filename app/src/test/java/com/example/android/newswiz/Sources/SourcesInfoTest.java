package com.example.android.newswiz.Sources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.ArrayList;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class SourcesInfoTest {

    private ArrayList<String> mPublishersSelected;
    private ArrayList<String> mCategoriesSelected;
    private ArrayList<String> mCountriesSelected;

    private SourcesInfo mSourcesInfo;

    @Before
    public void setUpSourcesInfo(){
        mPublishersSelected = new ArrayList<>();
        mCategoriesSelected = new ArrayList<>();
        mCountriesSelected = new ArrayList<>();

        mPublishersSelected.add("BBC");
        mCategoriesSelected.add("Science");
        mCountriesSelected.add("U.K");

        mSourcesInfo = new SourcesInfo(mPublishersSelected, mCategoriesSelected, mCountriesSelected);

    }

    @Test
    public void testSourcesInfoReturnsPublisherArrayList(){
        assertEquals(mPublishersSelected, mSourcesInfo.getmPublishersSelected());
    }

    @Test
    public void testSourcesInfoReturnsCategoriesArrayList(){
        assertEquals(mCategoriesSelected, mSourcesInfo.getmCategoriesSelected());
    }

    @Test
    public void testSourcesInfoReturnsCountriesArrayList(){
        assertEquals(mCountriesSelected, mSourcesInfo.getmCountriesSelected());
    }
}