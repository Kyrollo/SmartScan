package com.SmartScan.API;

import com.SmartScan.App;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.converter.gson.GsonConverterFactory;

public class Retrofit {
    private static retrofit2.Retrofit retrofit = null;
    public static retrofit2.Retrofit getRetrofit() {
        String baseUrl = "http://" + App.get().getServerIP()+ ":"+ App.get().getPortNo();
        OkHttpClient client = new OkHttpClient.Builder().retryOnConnectionFailure(true)
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60,TimeUnit.SECONDS)
                .build();

        retrofit = new retrofit2.Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
        return retrofit;
    }
}