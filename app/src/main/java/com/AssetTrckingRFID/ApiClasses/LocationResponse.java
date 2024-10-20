package com.AssetTrckingRFID.ApiClasses;

public class LocationResponse {
    private String LocationID;
    private String LocationBarCode;
    private String LocationDesc;
    private boolean HasParent;
    private String LocationParentID;
    private String FullLocationDesc;

    public LocationResponse() {
    }

    public LocationResponse(String locationID, String locationBarCode, String locationDesc, boolean hasParent, String locationParentID, String fullLocationDesc) {
        LocationID = locationID;
        LocationBarCode = locationBarCode;
        LocationDesc = locationDesc;
        HasParent = hasParent;
        LocationParentID = locationParentID;
        FullLocationDesc = fullLocationDesc;
    }

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
