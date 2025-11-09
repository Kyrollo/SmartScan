package com.AssetTrckingRFID.Tables;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "category")
public class Category {
    @PrimaryKey(autoGenerate = false)
    @NonNull
    private String CategoryID;

    @ColumnInfo(name = "category_desc")
    private String CategoryDesc;

    public Category() {
    }

    public Category(@NonNull String categoryID, String categoryDesc) {
        CategoryID = categoryID;
        CategoryDesc = categoryDesc;
    }

    // Getters and Setters
    @NonNull
    public String getCategoryID() {
        return CategoryID;
    }

    public void setCategoryID(@NonNull String categoryID) {
        CategoryID = categoryID;
    }

    public String getCategoryDesc() {
        return CategoryDesc;
    }

    public void setCategoryDesc(String categoryDesc) {
        CategoryDesc = categoryDesc;
    }
}
