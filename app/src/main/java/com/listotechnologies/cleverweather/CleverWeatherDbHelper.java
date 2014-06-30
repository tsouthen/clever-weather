/**********************************************************************************************************************************************************************
****** AUTO GENERATED FILE BY ANDROID SQLITE HELPER SCRIPT BY FEDERICO PAOLINELLI. ANY CHANGE WILL BE WIPED OUT IF THE SCRIPT IS PROCESSED AGAIN. *******
**********************************************************************************************************************************************************************/
package com.listotechnologies.cleverweather;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

import java.util.Date;

public class CleverWeatherDbHelper {
    private static final String TAG = "CleverWeather";

    private static final String DATABASE_NAME = "CleverWeather.db";
    private static final int DATABASE_VERSION = 1;


    // Variable to hold the database instance
    protected SQLiteDatabase mDb;
    // Context of the application using the database.
    private final Context mContext;
    // Database open/upgrade helper
    private DbHelper mDbHelper;
    
    public CleverWeatherDbHelper(Context context) {
        mContext = context;
        mDbHelper = new DbHelper(mContext, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public CleverWeatherDbHelper open() throws SQLException { 
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
                                                     
    public void close() {
        mDb.close();
    }

    public static final String ROW_ID = "_id";

    
    // -------------- CITY DEFINITIONS ------------
    public static final String CITY_TABLE = "City";
    
    public static final String CITY_CODE_COLUMN = "Code";
    public static final int CITY_CODE_COLUMN_POSITION = 1;
    
    public static final String CITY_NAMEEN_COLUMN = "NameEn";
    public static final int CITY_NAMEEN_COLUMN_POSITION = 2;
    
    public static final String CITY_NAMEFR_COLUMN = "NameFr";
    public static final int CITY_NAMEFR_COLUMN_POSITION = 3;
    
    public static final String CITY_PROVINCE_COLUMN = "Province";
    public static final int CITY_PROVINCE_COLUMN_POSITION = 4;
    
    public static final String CITY_LATITUDE_COLUMN = "Latitude";
    public static final int CITY_LATITUDE_COLUMN_POSITION = 5;
    
    public static final String CITY_LONGITUDE_COLUMN = "Longitude";
    public static final int CITY_LONGITUDE_COLUMN_POSITION = 6;
    
    public static final String CITY_ISFAVORITE_COLUMN = "IsFavorite";
    public static final int CITY_ISFAVORITE_COLUMN_POSITION = 7;
    
    
    // -------------- FORECAST DEFINITIONS ------------
    public static final String FORECAST_TABLE = "Forecast";
    
    public static final String FORECAST_CITYCODE_COLUMN = "CityCode";
    public static final int FORECAST_CITYCODE_COLUMN_POSITION = 1;
    
    public static final String FORECAST_UTCISSUETIME_COLUMN = "UTCIssueTime";
    public static final int FORECAST_UTCISSUETIME_COLUMN_POSITION = 2;
    
    public static final String FORECAST_NAME_COLUMN = "Name";
    public static final int FORECAST_NAME_COLUMN_POSITION = 3;
    
    public static final String FORECAST_SUMMARY_COLUMN = "Summary";
    public static final int FORECAST_SUMMARY_COLUMN_POSITION = 4;
    
    public static final String FORECAST_ICONCODE_COLUMN = "IconCode";
    public static final int FORECAST_ICONCODE_COLUMN_POSITION = 5;
    
    public static final String FORECAST_LOWTEMP_COLUMN = "LowTemp";
    public static final int FORECAST_LOWTEMP_COLUMN_POSITION = 6;
    
    public static final String FORECAST_HIGHTEMP_COLUMN = "HighTemp";
    public static final int FORECAST_HIGHTEMP_COLUMN_POSITION = 7;
    
    


    // -------- TABLES CREATION ----------

    
    // City CREATION 
    private static final String DATABASE_CITY_CREATE = "create table " + CITY_TABLE + " (" +
                                "_id integer primary key autoincrement, " +
                                CITY_CODE_COLUMN + " text, " +
                                CITY_NAMEEN_COLUMN + " text, " +
                                CITY_NAMEFR_COLUMN + " text, " +
                                CITY_PROVINCE_COLUMN + " text, " +
                                CITY_LATITUDE_COLUMN + " float, " +
                                CITY_LONGITUDE_COLUMN + " float, " +
                                CITY_ISFAVORITE_COLUMN + " integer" +
                                ")";
    
    // Forecast CREATION 
    private static final String DATABASE_FORECAST_CREATE = "create table " + FORECAST_TABLE + " (" +
                                "_id integer primary key autoincrement, " +
                                FORECAST_CITYCODE_COLUMN + " text, " +
                                FORECAST_UTCISSUETIME_COLUMN + " integer, " +
                                FORECAST_NAME_COLUMN + " text, " +
                                FORECAST_SUMMARY_COLUMN + " text, " +
                                FORECAST_ICONCODE_COLUMN + " integer, " +
                                FORECAST_LOWTEMP_COLUMN + " integer, " +
                                FORECAST_HIGHTEMP_COLUMN + " integer" +
                                ")";
    

    
    // ----------------City HELPERS -------------------- 
    public long addCity (String Code, String NameEn, String NameFr, String Province, float Latitude, float Longitude, int IsFavorite) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CITY_CODE_COLUMN, Code);
        contentValues.put(CITY_NAMEEN_COLUMN, NameEn);
        contentValues.put(CITY_NAMEFR_COLUMN, NameFr);
        contentValues.put(CITY_PROVINCE_COLUMN, Province);
        contentValues.put(CITY_LATITUDE_COLUMN, Latitude);
        contentValues.put(CITY_LONGITUDE_COLUMN, Longitude);
        contentValues.put(CITY_ISFAVORITE_COLUMN, IsFavorite);
        return mDb.insert(CITY_TABLE, null, contentValues);
    }

    public long updateCity (long rowIndex,String Code, String NameEn, String NameFr, String Province, float Latitude, float Longitude, int IsFavorite) {
        String where = ROW_ID + " = " + rowIndex;
        ContentValues contentValues = new ContentValues();
        contentValues.put(CITY_CODE_COLUMN, Code);
        contentValues.put(CITY_NAMEEN_COLUMN, NameEn);
        contentValues.put(CITY_NAMEFR_COLUMN, NameFr);
        contentValues.put(CITY_PROVINCE_COLUMN, Province);
        contentValues.put(CITY_LATITUDE_COLUMN, Latitude);
        contentValues.put(CITY_LONGITUDE_COLUMN, Longitude);
        contentValues.put(CITY_ISFAVORITE_COLUMN, IsFavorite);
        return mDb.update(CITY_TABLE, contentValues, where, null);
    }

    public boolean removeCity(long rowIndex){
        return mDb.delete(CITY_TABLE, ROW_ID + " = " + rowIndex, null) > 0;
    }

    public boolean removeAllCity(){
        return mDb.delete(CITY_TABLE, null, null) > 0;
    }

    public Cursor getAllCity(){
    	return mDb.query(CITY_TABLE, new String[] {
                         ROW_ID,
                         CITY_CODE_COLUMN,
                         CITY_NAMEEN_COLUMN,
                         CITY_NAMEFR_COLUMN,
                         CITY_PROVINCE_COLUMN,
                         CITY_LATITUDE_COLUMN,
                         CITY_LONGITUDE_COLUMN,
                         CITY_ISFAVORITE_COLUMN
                         }, null, null, null, null, null);
    }

    public Cursor getCity(long rowIndex) {
        Cursor res = mDb.query(CITY_TABLE, new String[] {
                                ROW_ID,
                                CITY_CODE_COLUMN,
                                CITY_NAMEEN_COLUMN,
                                CITY_NAMEFR_COLUMN,
                                CITY_PROVINCE_COLUMN,
                                CITY_LATITUDE_COLUMN,
                                CITY_LONGITUDE_COLUMN,
                                CITY_ISFAVORITE_COLUMN
                                }, ROW_ID + " = " + rowIndex, null, null, null, null);

        if(res != null){
            res.moveToFirst();
        }
        return res;
    }
    
    // ----------------Forecast HELPERS -------------------- 
    public long addForecast (String CityCode, Date UTCIssueTime, String Name, String Summary, int IconCode, int LowTemp, int HighTemp) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(FORECAST_CITYCODE_COLUMN, CityCode);
        contentValues.put(FORECAST_UTCISSUETIME_COLUMN, UTCIssueTime.getTime());
        contentValues.put(FORECAST_NAME_COLUMN, Name);
        contentValues.put(FORECAST_SUMMARY_COLUMN, Summary);
        contentValues.put(FORECAST_ICONCODE_COLUMN, IconCode);
        contentValues.put(FORECAST_LOWTEMP_COLUMN, LowTemp);
        contentValues.put(FORECAST_HIGHTEMP_COLUMN, HighTemp);
        return mDb.insert(FORECAST_TABLE, null, contentValues);
    }

    public long updateForecast (long rowIndex,String CityCode, Date UTCIssueTime, String Name, String Summary, int IconCode, int LowTemp, int HighTemp) {
        String where = ROW_ID + " = " + rowIndex;
        ContentValues contentValues = new ContentValues();
        contentValues.put(FORECAST_CITYCODE_COLUMN, CityCode);
        contentValues.put(FORECAST_UTCISSUETIME_COLUMN, UTCIssueTime.getTime());
        contentValues.put(FORECAST_NAME_COLUMN, Name);
        contentValues.put(FORECAST_SUMMARY_COLUMN, Summary);
        contentValues.put(FORECAST_ICONCODE_COLUMN, IconCode);
        contentValues.put(FORECAST_LOWTEMP_COLUMN, LowTemp);
        contentValues.put(FORECAST_HIGHTEMP_COLUMN, HighTemp);
        return mDb.update(FORECAST_TABLE, contentValues, where, null);
    }

    public boolean removeForecast(long rowIndex){
        return mDb.delete(FORECAST_TABLE, ROW_ID + " = " + rowIndex, null) > 0;
    }

    public boolean removeAllForecast(){
        return mDb.delete(FORECAST_TABLE, null, null) > 0;
    }

    public Cursor getAllForecast(){
    	return mDb.query(FORECAST_TABLE, new String[] {
                         ROW_ID,
                         FORECAST_CITYCODE_COLUMN,
                         FORECAST_UTCISSUETIME_COLUMN,
                         FORECAST_NAME_COLUMN,
                         FORECAST_SUMMARY_COLUMN,
                         FORECAST_ICONCODE_COLUMN,
                         FORECAST_LOWTEMP_COLUMN,
                         FORECAST_HIGHTEMP_COLUMN
                         }, null, null, null, null, null);
    }

    public Cursor getForecast(long rowIndex) {
        Cursor res = mDb.query(FORECAST_TABLE, new String[] {
                                ROW_ID,
                                FORECAST_CITYCODE_COLUMN,
                                FORECAST_UTCISSUETIME_COLUMN,
                                FORECAST_NAME_COLUMN,
                                FORECAST_SUMMARY_COLUMN,
                                FORECAST_ICONCODE_COLUMN,
                                FORECAST_LOWTEMP_COLUMN,
                                FORECAST_HIGHTEMP_COLUMN
                                }, ROW_ID + " = " + rowIndex, null, null, null, null);

        if(res != null){
            res.moveToFirst();
        }
        return res;
    }
    

    private static class DbHelper extends SQLiteOpenHelper {
        public DbHelper(Context context, String name, CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // Called when no database exists in disk and the helper class needs
        // to create a new one. 
        @Override
        public void onCreate(SQLiteDatabase db) {      
            db.execSQL(DATABASE_CITY_CREATE);
            db.execSQL(DATABASE_FORECAST_CREATE);
            
        }

        // Called when there is a database version mismatch meaning that the version
        // of the database on disk needs to be upgraded to the current version.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Log the version upgrade.
            Log.w(TAG, "Upgrading from version " + 
                        oldVersion + " to " +
                        newVersion + ", which will destroy all old data");
            
            // Upgrade the existing database to conform to the new version. Multiple 
            // previous versions can be handled by comparing _oldVersion and _newVersion
            // values.

            // The simplest case is to drop the old table and create a new one.
            db.execSQL("DROP TABLE IF EXISTS " + CITY_TABLE + ";");
            db.execSQL("DROP TABLE IF EXISTS " + FORECAST_TABLE + ";");
            
            // Create a new one.
            onCreate(db);
        }
    }
}

