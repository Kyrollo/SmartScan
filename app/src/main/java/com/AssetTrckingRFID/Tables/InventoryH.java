package com.AssetTrckingRFID.Tables;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventoryh")
public class InventoryH {
    @PrimaryKey(autoGenerate = false)
    private int id;

    @ColumnInfo(name = "inventory_id")
    private int inventoryID;

    @ColumnInfo(name = "inventory_name")
    private String inventoryName;

    @ColumnInfo(name = "start_date")
    private String startDate;

    @ColumnInfo(name = "end_date")
    private String endDate;

    @ColumnInfo(name = "closed")
    private boolean closed;

    public InventoryH() {
    }

    public InventoryH(int inventoryID, String inventoryName, String startdate, String enddate, boolean closed) {
        this.inventoryID = inventoryID;
        this.inventoryName = inventoryName;
        this.startDate = startdate;
        this.endDate = enddate;
        this.closed = closed;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

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

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }
}
