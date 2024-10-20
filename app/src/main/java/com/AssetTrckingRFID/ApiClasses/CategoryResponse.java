package com.AssetTrckingRFID.ApiClasses;

public class CategoryResponse {
    private String CategoryID;
    private String CategoryDesc;

    public CategoryResponse() {
    }

    public CategoryResponse(String categoryID, String categoryDesc) {
        CategoryID = categoryID;
        CategoryDesc = categoryDesc;
    }

    // Getters and Setters
    public String getCategoryID() {
        return CategoryID;
    }

    public void setCategoryID(String categoryID) {
        CategoryID = categoryID;
    }

    public String getCategoryDesc() {
        return CategoryDesc;
    }

    public void setCategoryDesc(String categoryDesc) {
        CategoryDesc = categoryDesc;
    }
}
