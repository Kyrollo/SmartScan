package com.SmartScan.API;

import com.SmartScan.ApiClasses.ItemResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface APIService {
    @GET("api/Connection/TestConnection")
    Call<String> testConnection();

    @GET("api/DownloadData/GetAllItems")
    Call<List<ItemResponse>> getItems();
}
