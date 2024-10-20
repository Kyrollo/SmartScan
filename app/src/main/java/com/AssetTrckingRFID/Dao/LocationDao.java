package com.AssetTrckingRFID.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.AssetTrckingRFID.Tables.Location;

import java.util.List;

@Dao
public interface LocationDao {
    @Insert
    void insert(Location location);

    @Query("SELECT * FROM location")
    List<Location> getAllLocations();

    @Query("SELECT * FROM location where has_parent = false")
    List<Location> getAllParents();

    @Query("SELECT * FROM location where LocationID = :parentId")
    Location getAllLocationByLocationID(String parentId);

    @Query("SELECT * FROM location where location_parent_id = :parentId")
    List<Location> getAllLocationByParentID(String parentId);

    @Query("DELETE FROM location")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'location'")
    void resetPrimaryKey();

    @Query("SELECT * FROM Location WHERE full_location_desc = :locDesc ")
    Location findLocationByDesc(String locDesc);

    @Query("SELECT * FROM Location WHERE location_parent_id = :locationID limit 1 ")
    Location checkChildLocation(String locationID);

    @Query("SELECT * FROM Location WHERE location_barcode = :locBarcode ")
    Location findLocationByBarcode(String locBarcode);

}
