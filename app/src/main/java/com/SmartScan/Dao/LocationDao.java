package com.SmartScan.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.SmartScan.ApiClasses.LocationResponse;
import com.SmartScan.Tables.Location;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    void insert(Location location);

    @Query("SELECT * FROM location")
    List<Location> getAllLocations();

    @Query("SELECT * FROM location where has_parent = false")
    List<Location> getAllParents();

    @Query("SELECT * FROM location where location_parent_id = :parentId")
    List<Location> getAllLocationByParentID(String parentId);

    @Query("DELETE FROM location")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'location'")
    void resetPrimaryKey();
}
