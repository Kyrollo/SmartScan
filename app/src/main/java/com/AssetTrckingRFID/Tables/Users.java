package com.AssetTrckingRFID.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class Users {
    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "UserID")
    private int UserID;

    @ColumnInfo(name = "UserName")
    private String UserName;

    @ColumnInfo(name = "Password")
    private String Password;

    public Users() {
    }

    public Users(String username, String password) {
        this.UserName = username;
        this.Password = password;
    }

    // Getters and Setters
    public int getUserID() {return UserID;}

    public void setUserID(int id) {
        this.UserID = id;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String username) {
        this.UserName = username;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        this.Password = password;
    }
}