package com.AssetTrckingRFID.DataBase;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.AssetTrckingRFID.Dao.*;
import com.AssetTrckingRFID.Tables.*;

@Database(entities = {Item.class, Users.class, Location.class, Category.class, Status.class, InventoryH.class, Inventory.class}, exportSchema = false,version = 10)
public abstract class AppDataBase extends RoomDatabase {
    public abstract ItemDao itemDao();
    public abstract UsersDao usersDao();
    public abstract LocationDao locationDao();
    public abstract CategoryDao categoryDao();
    public abstract StatusDao statusDao();
    public abstract InventoryH_Dao inventoryH_dao();
    public abstract InventoryDao inventoryDao();

   // private static volatile AppDataBase INSTANCE;

//    public static AppDataBase getDatabase(final Context context) {
//        if (INSTANCE == null) {
//            synchronized (AppDataBase.class) {
//                if (INSTANCE == null) {
//                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
//                                    AppDataBase.class, "app_database")
//                            .allowMainThreadQueries()
//                            .fallbackToDestructiveMigration()
//                            .build();
//                }
//            }
//        }
//        return INSTANCE;
 //   }
}