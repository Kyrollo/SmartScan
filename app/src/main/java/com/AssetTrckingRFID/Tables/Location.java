package com.AssetTrckingRFID.Tables;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "location")
public class Location {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String LocationID;

    @ColumnInfo(name = "location_barcode")
    private String LocationBarCode;

    @ColumnInfo(name = "location_desc")
    private String LocationDesc;

    @ColumnInfo(name = "has_parent")
    private boolean HasParent;

    @ColumnInfo(name = "location_parent_id")
    private String LocationParentID;

    @ColumnInfo(name = "full_location_desc")
    private String FullLocationDesc;

    public Location() {
    }

    public Location(String locationID, String locationBarCode, String locationDesc, boolean hasParent, String locationParentID, String fullLocationDesc) {
        LocationID = locationID;
        LocationBarCode = locationBarCode;
        LocationDesc = locationDesc;
        HasParent = hasParent;
        LocationParentID = locationParentID;
        FullLocationDesc = fullLocationDesc;
    }

    // Getters and Setters

    public String getLocationID() {
        return LocationID;
    }

    public void setLocationID(String locationID) {
        LocationID = locationID;
    }

    public String getLocationBarCode() {
        return LocationBarCode;
    }

    public void setLocationBarCode(String locationBarCode) {
        LocationBarCode = locationBarCode;
    }

    public String getLocationDesc() {
        return LocationDesc;
    }

    public void setLocationDesc(String locationDesc) {
        LocationDesc = locationDesc;
    }

    public boolean isHasParent() {
        return HasParent;
    }

    public void setHasParent(boolean hasParent) {
        HasParent = hasParent;
    }

    public String getLocationParentID() {
        return LocationParentID;
    }

    public void setLocationParentID(String locationParentID) {
        LocationParentID = locationParentID;
    }

    public String getFullLocationDesc() {
        return FullLocationDesc;
    }

    public void setFullLocationDesc(String fullLocationDesc) {
        FullLocationDesc = fullLocationDesc;
    }
}
