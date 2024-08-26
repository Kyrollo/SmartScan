package com.SmartScan.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.SmartScan.Tables.Status;

import java.util.List;

@Dao
public interface StatusDao {
    @Insert
    void insert(Status status);

    @Query("SELECT status_id, status_desc, id FROM status WHERE status_id = :statusID")
    Status getStatusByID(String statusID);

    @Query("SELECT * FROM status")
    List<Status> getAllStatuses();

    @Query("DELETE FROM status")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'status'")
    void resetPrimaryKey();
}
