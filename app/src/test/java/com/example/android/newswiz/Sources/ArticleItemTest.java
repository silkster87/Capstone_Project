package com.example.android.newswiz.Sources;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static org.junit.Assert.*;

@RunWith(JUnit4.class)
public class ArticleItemTest {

    private ArticleItem articleItem;

    @Before
    public void setUpArticleItem(){
        String author = "Jane";
        String title = "Hello World";
        String description = "Article description";
        String url = "Article URL";
        String urlToImage = "URL to image";
        String publishedAt = "published at";

       articleItem = new ArticleItem(author, title, description, url, urlToImage, publishedAt);
    }


    @Test
    public void testArticleItemReturnsAuthor(){
        assertEquals("Jane", articleItem.getAuthor());
    }

    @Test
    public void testArticleItemReturnsTitle(){
        assertEquals("Hello World", articleItem.getTitle());
    }

    @Test
    public void testArticleItemReturnsDescription(){
        assertEquals("Article description", articleItem.getDescription());
    }

    @Test
    public void testArticleItemReturnsURL(){
        assertEquals("Article URL", articleItem.getUrl());
    }

    @Test
    public void testArticleItemReturnsURLToImage(){
        assertEquals("URL to image", articleItem.getUrlToImage());
    }

    @Test
    public void testArticleItemReturnsPublishedAt(){
        assertEquals("published at", articleItem.getPublishedAt());
    }

}