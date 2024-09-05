package com.SmartScan.API;

import androidx.room.Query;
import androidx.room.Update;

import com.SmartScan.ApiClasses.*;
import com.SmartScan.Tables.Item;
import com.SmartScan.Tables.Users;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface APIService {
    @GET("api/Connection/TestConnection")
    Call<String> testConnection();

    @GET("api/DownloadData/GetAllItems")
    Call<List<ItemResponse>> getItems();

    @GET("api/Connection/GetAllUsers")
    Call<List<Users>> getUsers();

    @GET("api/DownloadData/GetCategories")
    Call<List<CategoryResponse>> getCategories();

    @GET("api/DownloadData/GetLocation")
    Call<List<LocationResponse>> getLocation();

    @GET("api/DownloadData/GetStatusList")
    Call<List<StatusResponse>> getStatus();

    @GET("api/DownloadData/GetAll_Inventory_H")
    Call<List<InventoryH_Response>> getInventoryH();

    @POST("api/Upload/UploadAssignedAssetsTag")
    Call<String> uploadAssignedAssetsTag(@Body List<Item> items);
}
