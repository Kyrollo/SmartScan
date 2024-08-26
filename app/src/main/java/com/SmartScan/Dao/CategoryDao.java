package com.SmartScan.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.SmartScan.Tables.Category;

import java.util.List;

@Dao
public interface CategoryDao {
    @Insert
    void insert(Category category);

    @Query("SELECT category_id, category_desc, id FROM category WHERE category_id = :categoryID")
    Category getCategoryByID(String categoryID);

    @Query("SELECT * FROM category")
    List<Category> getAllCategories();

    @Query("DELETE FROM category")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'category'")
    void resetPrimaryKey();
}