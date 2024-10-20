package com.AssetTrckingRFID.Dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.AssetTrckingRFID.Tables.Users;

import java.util.List;

@Dao
public interface UsersDao {
    @Insert
    void insert(Users user);

    @Insert
    void insertAll(List<Users> users);

    @Query("SELECT * FROM users")
    List<Users> getAllUsers();

    @Query("SELECT * FROM users WHERE UserName = :username AND Password = :password LIMIT 1")
    Users getUserByUsernameAndPassword(String username, String password);

    @Query("DELETE FROM users")
    void deleteAll();

    @Query("DELETE FROM sqlite_sequence WHERE name = 'users'")
    void resetPrimaryKey();
}