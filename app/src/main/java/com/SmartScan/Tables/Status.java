package com.SmartScan.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "status")
public class Status {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "status_id")
    private int StatusID;

    @ColumnInfo(name = "status_desc")
    private String StatusDesc;

    public Status() {
    }

    public Status(int statusID, String statusDesc) {
        StatusID = statusID;
        StatusDesc = statusDesc;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getStatusID() {
        return StatusID;
    }

    public void setStatusID(int statusID) {
        StatusID = statusID;
    }

    public String getStatusDesc() {
        return StatusDesc;
    }

    public void setStatusDesc(String statusDesc) {
        StatusDesc = statusDesc;
    }
}
