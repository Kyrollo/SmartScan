package com.AssetTrckingRFID;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;

import androidx.annotation.NonNull;
import androidx.room.Room;

import com.AssetTrckingRFID.DataBase.AppDataBase;
import com.zebra.rfid.api3.RFIDReader;

public class App extends Application {
    public static App INSTANCE;
    private static final String DATABASE_NAME = "AppDataBase";
    private static final String PREFERENCES = "RoomDemo.preferences";
    private static final String SERVER_IP = "SERVER_IP";
    private static final String PORT_NO = "PORT_NO";
    private AppDataBase database;
    private RFIDReader rfidReader;

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = App.this;
        database = Room.databaseBuilder(getApplicationContext(), AppDataBase.class, DATABASE_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    public static App get() {
        return INSTANCE;
    }

    public AppDataBase getDB() {
        return database;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private SharedPreferences getSP() {
        return getSharedPreferences(PREFERENCES, MODE_PRIVATE);
    }

    public void setServerCredentials(String serverIP, String portNo) {
        SharedPreferences.Editor edit = getSP().edit();
        edit.putString(SERVER_IP, serverIP);
        edit.putString(PORT_NO, portNo);
        edit.apply();
    }

    public String getServerIP() {
        return getSP().getString(SERVER_IP, "");
    }

    public String getPortNo() {
        return getSP().getString(PORT_NO, "");
    }

    public RFIDReader getRfidReader() {
        return rfidReader;
    }

    public void setRfidReader(RFIDReader rfidReader) {
        this.rfidReader = rfidReader;
    }

}