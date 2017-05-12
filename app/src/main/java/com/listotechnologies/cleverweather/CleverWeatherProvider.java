/**********************************************************************************************************************************************************************
****** AUTO GENERATED FILE BY ANDROID SQLITE HELPER SCRIPT BY FEDERICO PAOLINELLI. ANY CHANGE WILL BE WIPED OUT IF THE SCRIPT IS PROCESSED AGAIN. *******
**********************************************************************************************************************************************************************/
package com.listotechnologies.cleverweather;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import java.util.Date;

public class CleverWeatherProvider extends ContentProvider {
    private static final String TAG = "CleverWeatherProvider";

    protected static final String DATABASE_NAME = "CleverWeather.db";
    protected static final int DATABASE_VERSION = 1;

    // --------------- URIS --------------------
    public static final Uri CITY_URI = Uri.parse("content://com.listotechnologies.cleverweather.provider/City");
    public static final Uri FORECAST_URI = Uri.parse("content://com.listotechnologies.cleverweather.provider/Forecast");
    
    // -------------- CITY DEFINITIONS ------------
    public static final String CITY_TABLE = "City";

    public static final String CITY_ID_COLUMN = "City._id";
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
    public static final int ALL_CITY = 0;
    public static final int SINGLE_CITY = 1;

    
    // -------------- FORECAST DEFINITIONS ------------
    public static final String FORECAST_TABLE = "Forecast";

    public static final String FORECAST_ID_COLUMN = "Forecast._id";
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
    public static final int ALL_FORECAST = 2;
    public static final int SINGLE_FORECAST = 3;

    private static final UriMatcher uriMatcher;

    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    
        uriMatcher.addURI("com.listotechnologies.cleverweather.provider", "City", ALL_CITY);
        uriMatcher.addURI("com.listotechnologies.cleverweather.provider", "City/#", SINGLE_CITY);
    
        uriMatcher.addURI("com.listotechnologies.cleverweather.provider", "Forecast", ALL_FORECAST);
        uriMatcher.addURI("com.listotechnologies.cleverweather.provider", "Forecast/#", SINGLE_FORECAST);
    }
 

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
    

    protected DbHelper myOpenHelper;

    @Override
    public boolean onCreate() {
        myOpenHelper = new DbHelper(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    private String getTableNameFromUri(Uri uri) {
        return getTableNameFromUri(uri, false);
    }

    /**
    * Returns the right table name for the given uri
    * @param uri
    * @return
    */
    private String getTableNameFromUri(Uri uri, boolean isQuery){
        switch (uriMatcher.match(uri)) {
            case ALL_CITY:
            case SINGLE_CITY:
                return CITY_TABLE;
            case ALL_FORECAST:
            case SINGLE_FORECAST:
                if (isQuery)
                    return String.format("%1$s INNER JOIN %3$s ON (%1$s.%2$s = %3$s.%4$s)", FORECAST_TABLE, FORECAST_CITYCODE_COLUMN, CITY_TABLE, CITY_CODE_COLUMN);
                return FORECAST_TABLE;
            default: break;
        }
        return null;
    }
    
    /**
    * Returns the parent uri for the given uri
    * @param uri
    * @return
    */
    private Uri getContentUriFromUri(Uri uri){
        switch (uriMatcher.match(uri)) {
            case ALL_CITY:
            case SINGLE_CITY:
                return CITY_URI;
            case ALL_FORECAST:
            case SINGLE_FORECAST:
                return FORECAST_URI;
            default: break;
        }
        return null;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
        String[] selectionArgs, String sortOrder) {

        // Open the database.
        SQLiteDatabase db;
        try {
            db = myOpenHelper.getWritableDatabase();
        } catch (SQLiteException ex) {
            db = myOpenHelper.getReadableDatabase();
        }

        // Replace these with valid SQL statements if necessary.
        String groupBy = null;
        String having = null;

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // If this is a row query, limit the result set to the passed in row.
        String idColumn = null;
        switch (uriMatcher.match(uri)) {
            case SINGLE_CITY:
                idColumn = CITY_ID_COLUMN;
                break;
            case SINGLE_FORECAST:
                idColumn = FORECAST_ID_COLUMN;
                break;
        }
        if (idColumn != null) {
            String rowID = uri.getPathSegments().get(1);
            queryBuilder.appendWhere(idColumn + "=" + rowID);
        }

        // Specify the table on which to perform the query. This can
        // be a specific table or a join as required.
        queryBuilder.setTables(getTableNameFromUri(uri, true));

        // Execute the query.
        Cursor cursor = queryBuilder.query(db, projection, selection,
                    selectionArgs, groupBy, having, sortOrder);
            cursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the result Cursor.
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        // Return a string that identifies the MIME type
        // for a Content Provider URI
        switch (uriMatcher.match(uri)) {
            case ALL_CITY:
                return "vnd.android.cursor.dir/vnd.com.listotechnologies.cleverweather.City";
            case SINGLE_CITY:
                return "vnd.android.cursor.dir/vnd.com.listotechnologies.cleverweather.City";
            case ALL_FORECAST:
                return "vnd.android.cursor.dir/vnd.com.listotechnologies.cleverweather.Forecast";
            case SINGLE_FORECAST:
                return "vnd.android.cursor.dir/vnd.com.listotechnologies.cleverweather.Forecast";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
            }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();

        String idColumn = null;
        switch (uriMatcher.match(uri)) {
            case SINGLE_CITY:
                idColumn = CITY_ID_COLUMN;
                break;
            case SINGLE_FORECAST:
                idColumn = FORECAST_ID_COLUMN;
                break;
        }
        if (idColumn != null) {
            String rowID = uri.getPathSegments().get(1);
            selection = idColumn + "=" + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
        }
        if (selection == null)
            selection = "1";

        int deleteCount = db.delete(getTableNameFromUri(uri),
                selection, selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);
        return deleteCount;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();
        String nullColumnHack = null;

        long id = db.insert(getTableNameFromUri(uri), nullColumnHack, values);
        if (id > -1) {
            Uri insertedId = ContentUris.withAppendedId(getContentUriFromUri(uri), id);
                                getContext().getContentResolver().notifyChange(insertedId, null);
            getContext().getContentResolver().notifyChange(insertedId, null);
            return insertedId;
        } else {
            return null;
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // Open a read / write database to support the transaction.
        SQLiteDatabase db = myOpenHelper.getWritableDatabase();

        // If this is a row URI, limit the deletion to the specified row.
        String idColumn = null;
        switch (uriMatcher.match(uri)) {
            case SINGLE_CITY:
                idColumn = CITY_ID_COLUMN;
                break;
            case SINGLE_FORECAST:
                idColumn = FORECAST_ID_COLUMN;
                break;
        }
        if (idColumn != null) {
            String rowID = uri.getPathSegments().get(1);
            selection = idColumn + "=" + rowID + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')' : "");
        }

        // Perform the update.
        int updateCount = db.update(getTableNameFromUri(uri), values, selection, selectionArgs);
        // Notify any observers of the change in the data set.
        getContext().getContentResolver().notifyChange(uri, null);
        return updateCount;
    }

    protected static class DbHelper extends SQLiteOpenHelper {
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

