package com.example.android.newswiz.Utilities;


import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GetOkHttpResponse {

    private OkHttpClient client;
    private Request request;

    public GetOkHttpResponse(OkHttpClient client, Request request){
        this.client = client;
        this.request = request;
    }

    public String run() throws IOException{
       // Request request = new Request.Builder().url(url).build();

        try {
            Response response = client.newCall(request).execute();
            return response.body().string();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
