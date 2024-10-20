package com.AssetTrckingRFID.ApiClasses;

public class InventoryH_Response {
    private int inventoryID;
    private String inventoryName;
    private String startdate;
    private String enddate;
    private boolean closed;

    public InventoryH_Response() {
    }

    public InventoryH_Response(int inventoryID, String inventoryName, String startdate, String enddate, boolean closed) {
        this.inventoryID = inventoryID;
        this.inventoryName = inventoryName;
        this.startdate = startdate;
        this.enddate = enddate;
        this.closed = closed;
    }

    // Getters and Setters
    public int getInventoryID() {
        return inventoryID;
    }

    public void setInventoryID(int inventoryID) {
        this.inventoryID = inventoryID;
    }

    public String getInventoryName() {
        return inventoryName;
    }

    public void setInventoryName(String inventoryName) {
        this.inventoryName = inventoryName;
    }

    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
