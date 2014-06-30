/**********************************************************************************************************************************************************************
****** AUTO GENERATED FILE BY ANDROID SQLITE HELPER SCRIPT BY FEDERICO PAOLINELLI. ANY CHANGE WILL BE WIPED OUT IF THE SCRIPT IS PROCESSED AGAIN. *******
**********************************************************************************************************************************************************************/
package com.listotechnologies.cleverweather;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.Date;

public class CleverWeatherProviderClient{


    // ------------- CITY_HELPERS ------------
    public static Uri addCity (String Code, 
                                String NameEn, 
                                String NameFr, 
                                String Province, 
                                float Latitude, 
                                float Longitude, 
                                int IsFavorite, 
                                Context c) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CleverWeatherProvider.CITY_CODE_COLUMN, Code);
        contentValues.put(CleverWeatherProvider.CITY_NAMEEN_COLUMN, NameEn);
        contentValues.put(CleverWeatherProvider.CITY_NAMEFR_COLUMN, NameFr);
        contentValues.put(CleverWeatherProvider.CITY_PROVINCE_COLUMN, Province);
        contentValues.put(CleverWeatherProvider.CITY_LATITUDE_COLUMN, Latitude);
        contentValues.put(CleverWeatherProvider.CITY_LONGITUDE_COLUMN, Longitude);
        contentValues.put(CleverWeatherProvider.CITY_ISFAVORITE_COLUMN, IsFavorite);
        ContentResolver cr = c.getContentResolver();
        return cr.insert(CleverWeatherProvider.CITY_URI, contentValues);
    }

    public static int removeCity(long rowIndex, Context c){
        ContentResolver cr = c.getContentResolver();
        Uri rowAddress = ContentUris.withAppendedId(CleverWeatherProvider.CITY_URI, rowIndex);
        return cr.delete(rowAddress, null, null);
    }

    public static int removeAllCity(Context c){
        ContentResolver cr = c.getContentResolver();
        return cr.delete(CleverWeatherProvider.CITY_URI, null, null);
    }

    public static Cursor getAllCity(Context c){
    	ContentResolver cr = c.getContentResolver();
        String[] resultColumns = new String[] {
                         CleverWeatherProvider.ROW_ID,
                         CleverWeatherProvider.CITY_CODE_COLUMN,
                         CleverWeatherProvider.CITY_NAMEEN_COLUMN,
                         CleverWeatherProvider.CITY_NAMEFR_COLUMN,
                         CleverWeatherProvider.CITY_PROVINCE_COLUMN,
                         CleverWeatherProvider.CITY_LATITUDE_COLUMN,
                         CleverWeatherProvider.CITY_LONGITUDE_COLUMN,
                         CleverWeatherProvider.CITY_ISFAVORITE_COLUMN
                         };

        Cursor resultCursor = cr.query(CleverWeatherProvider.CITY_URI, resultColumns, null, null, null);
        return resultCursor;
    }

    public static Cursor getCity(long rowId, Context c){
    	ContentResolver cr = c.getContentResolver();
        String[] resultColumns = new String[] {
                         CleverWeatherProvider.ROW_ID,
                         CleverWeatherProvider.CITY_CODE_COLUMN,
                         CleverWeatherProvider.CITY_NAMEEN_COLUMN,
                         CleverWeatherProvider.CITY_NAMEFR_COLUMN,
                         CleverWeatherProvider.CITY_PROVINCE_COLUMN,
                         CleverWeatherProvider.CITY_LATITUDE_COLUMN,
                         CleverWeatherProvider.CITY_LONGITUDE_COLUMN,
                         CleverWeatherProvider.CITY_ISFAVORITE_COLUMN
                         };

        Uri rowAddress = ContentUris.withAppendedId(CleverWeatherProvider.CITY_URI, rowId);
        String where = null;    
        String whereArgs[] = null;
        String order = null;
    
        Cursor resultCursor = cr.query(rowAddress, resultColumns, where, whereArgs, order);
        return resultCursor;
    }

    public static int updateCity (int rowId, 
                                   String Code,
                                   String NameEn,
                                   String NameFr,
                                   String Province,
                                   float Latitude,
                                   float Longitude,
                                   int IsFavorite,
                                   Context c) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CleverWeatherProvider.CITY_CODE_COLUMN, Code);
        contentValues.put(CleverWeatherProvider.CITY_NAMEEN_COLUMN, NameEn);
        contentValues.put(CleverWeatherProvider.CITY_NAMEFR_COLUMN, NameFr);
        contentValues.put(CleverWeatherProvider.CITY_PROVINCE_COLUMN, Province);
        contentValues.put(CleverWeatherProvider.CITY_LATITUDE_COLUMN, Latitude);
        contentValues.put(CleverWeatherProvider.CITY_LONGITUDE_COLUMN, Longitude);
        contentValues.put(CleverWeatherProvider.CITY_ISFAVORITE_COLUMN, IsFavorite);
        Uri rowAddress = ContentUris.withAppendedId(CleverWeatherProvider.CITY_URI, rowId);

        ContentResolver cr = c.getContentResolver();
        int updatedRowCount = cr.update(rowAddress, contentValues, null, null);
        return updatedRowCount;
    }
    
    // ------------- FORECAST_HELPERS ------------
    public static Uri addForecast (String CityCode, 
                                Date UTCIssueTime, 
                                String Name, 
                                String Summary, 
                                int IconCode, 
                                int LowTemp, 
                                int HighTemp, 
                                Context c) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CleverWeatherProvider.FORECAST_CITYCODE_COLUMN, CityCode);
        contentValues.put(CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN, UTCIssueTime.getTime());
        contentValues.put(CleverWeatherProvider.FORECAST_NAME_COLUMN, Name);
        contentValues.put(CleverWeatherProvider.FORECAST_SUMMARY_COLUMN, Summary);
        contentValues.put(CleverWeatherProvider.FORECAST_ICONCODE_COLUMN, IconCode);
        contentValues.put(CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN, LowTemp);
        contentValues.put(CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN, HighTemp);
        ContentResolver cr = c.getContentResolver();
        return cr.insert(CleverWeatherProvider.FORECAST_URI, contentValues);
    }

    public static int removeForecast(long rowIndex, Context c){
        ContentResolver cr = c.getContentResolver();
        Uri rowAddress = ContentUris.withAppendedId(CleverWeatherProvider.FORECAST_URI, rowIndex);
        return cr.delete(rowAddress, null, null);
    }

    public static int removeAllForecast(Context c){
        ContentResolver cr = c.getContentResolver();
        return cr.delete(CleverWeatherProvider.FORECAST_URI, null, null);
    }

    public static Cursor getAllForecast(Context c){
    	ContentResolver cr = c.getContentResolver();
        String[] resultColumns = new String[] {
                         CleverWeatherProvider.ROW_ID,
                         CleverWeatherProvider.FORECAST_CITYCODE_COLUMN,
                         CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN,
                         CleverWeatherProvider.FORECAST_NAME_COLUMN,
                         CleverWeatherProvider.FORECAST_SUMMARY_COLUMN,
                         CleverWeatherProvider.FORECAST_ICONCODE_COLUMN,
                         CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN,
                         CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN
                         };

        Cursor resultCursor = cr.query(CleverWeatherProvider.FORECAST_URI, resultColumns, null, null, null);
        return resultCursor;
    }

    public static Cursor getForecast(long rowId, Context c){
    	ContentResolver cr = c.getContentResolver();
        String[] resultColumns = new String[] {
                         CleverWeatherProvider.ROW_ID,
                         CleverWeatherProvider.FORECAST_CITYCODE_COLUMN,
                         CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN,
                         CleverWeatherProvider.FORECAST_NAME_COLUMN,
                         CleverWeatherProvider.FORECAST_SUMMARY_COLUMN,
                         CleverWeatherProvider.FORECAST_ICONCODE_COLUMN,
                         CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN,
                         CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN
                         };

        Uri rowAddress = ContentUris.withAppendedId(CleverWeatherProvider.FORECAST_URI, rowId);
        String where = null;    
        String whereArgs[] = null;
        String order = null;
    
        Cursor resultCursor = cr.query(rowAddress, resultColumns, where, whereArgs, order);
        return resultCursor;
    }

    public static int updateForecast (int rowId, 
                                   String CityCode,
                                   Date UTCIssueTime,
                                   String Name,
                                   String Summary,
                                   int IconCode,
                                   int LowTemp,
                                   int HighTemp,
                                   Context c) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(CleverWeatherProvider.FORECAST_CITYCODE_COLUMN, CityCode);
        contentValues.put(CleverWeatherProvider.FORECAST_UTCISSUETIME_COLUMN, UTCIssueTime.getTime());
        contentValues.put(CleverWeatherProvider.FORECAST_NAME_COLUMN, Name);
        contentValues.put(CleverWeatherProvider.FORECAST_SUMMARY_COLUMN, Summary);
        contentValues.put(CleverWeatherProvider.FORECAST_ICONCODE_COLUMN, IconCode);
        contentValues.put(CleverWeatherProvider.FORECAST_LOWTEMP_COLUMN, LowTemp);
        contentValues.put(CleverWeatherProvider.FORECAST_HIGHTEMP_COLUMN, HighTemp);
        Uri rowAddress = ContentUris.withAppendedId(CleverWeatherProvider.FORECAST_URI, rowId);

        ContentResolver cr = c.getContentResolver();
        int updatedRowCount = cr.update(rowAddress, contentValues, null, null);
        return updatedRowCount;
    }
    
}
