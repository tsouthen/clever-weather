package com.listotechnologies.cleverweather;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;

public class CleverWeatherProviderExtended extends CleverWeatherProvider {
    public static String AUTHORITY = "com.listotechnologies.cleverweather.provider";
    private static final int SEARCH_SUGGEST = 0;
    private static Exception sLastQueryException = null;
    private static UriMatcher sSuggestMatcher = buildUriMatcher();
    private static String sLastSuggestionQuery = null;
    private static String sLastSuggestion = null;

    private static UriMatcher buildUriMatcher() {
        UriMatcher matcher =  new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY, SEARCH_SUGGEST);
        matcher.addURI(AUTHORITY, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SEARCH_SUGGEST);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        myOpenHelper = new DbHelper2(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
    }

    private static String getProvinceForCityCode(SQLiteDatabase db, String cityCode) {
        try {
            String selection = String.format("%s=?", CITY_CODE_COLUMN);
            Cursor cursor = db.query(CITY_TABLE, new String[] { CITY_PROVINCE_COLUMN }, selection, new String[] { cityCode }, null, null, null);
            cursor.moveToFirst();
            if (!cursor.isNull(0)) {
                String province = cursor.getString(0);
                cursor.close();
                return province;
            }
        } catch (SQLException sqlE) {
        }
        return null;
    }

    private void logForecastsTable(SQLiteDatabase db) {
        String[] cols = { FORECAST_CITYCODE_COLUMN, FORECAST_NAME_COLUMN, FORECAST_UTCISSUETIME_COLUMN };
        String orderBy = FORECAST_CITYCODE_COLUMN + ", " + ROW_ID;
        Cursor cursor = db.query(FORECAST_TABLE, cols, null, null, null, null, orderBy);
        while (cursor.moveToNext()) {
            String msg = String.format("%s, %s, %s", cursor.getString(0), cursor.getString(1), cursor.getString(2));
            Log.d("Forecast", msg);
        }
        cursor.close();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        sLastQueryException = null;

        if (sSuggestMatcher.match(uri) == SEARCH_SUGGEST)
            return getSuggestions(uri);

        if (uri == FORECAST_URI) {
            //see if the current forecast is out of date
            SQLiteDatabase db;
            try {
                db = myOpenHelper.getWritableDatabase();
                String issueProjection = String.format("max(%s)", FORECAST_UTCISSUETIME_COLUMN);
                Cursor cursor = db.query(FORECAST_TABLE, new String[] { issueProjection }, selection, selectionArgs, null, null, null);
                cursor.moveToFirst();
                boolean requery = cursor.isNull(0);
                if (!requery) {
                    //if value is over 1 hour old, delete and re-query
                    long expiryTime = cursor.getLong(0) + 3600000;
                    Date now = new Date();
                    if (now.getTime() > expiryTime)
                        requery = true;
                }
                cursor.close();

                if (requery && selectionArgs != null && selectionArgs.length > 0) {
                    //HACK: assuming selection args contains city code
                    String cityCode = null;
                    for (String selArg: selectionArgs) {
                        if (selArg != null && selArg.startsWith("s000")) {
                            cityCode = selArg;
                            break;
                        }
                    }
                    if (cityCode != null) {
                        String provAbbr = getProvinceForCityCode(db, cityCode);
                        if (provAbbr != null) {
                            ArrayList<ContentValues> newValues = ForecastParser.parseXml(getContext(), provAbbr, cityCode);
                            if (newValues != null && newValues.size() > 0) {
                                db.beginTransaction();
                                boolean success = true;
                                int rowsDeleted = db.delete(FORECAST_TABLE, selection, selectionArgs);
                                for (ContentValues values : newValues) {
                                    long id = db.insert(FORECAST_TABLE, null, values);
                                    if (id == -1) {
                                        success = false;
                                        break;
                                    }
                                }
                                if (success)
                                    db.setTransactionSuccessful();
                                db.endTransaction();
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                sLastQueryException = ex;
            }
        }

        try {
            return super.query(uri, projection, selection, selectionArgs, sortOrder);
        } catch (Exception ex) {
            if (sLastQueryException != null)
                sLastQueryException = ex;
            return null;
        }
    }

    private Cursor getSuggestions(Uri uri) {
        try {
            sLastSuggestionQuery = null;
            sLastSuggestion = null;
            String query = uri.getLastPathSegment();
            if (query == null || query.length() == 0 || query.equals(SearchManager.SUGGEST_URI_PATH_QUERY))
                return null;

            query = query.trim();
            SQLiteDatabase db = myOpenHelper.getReadableDatabase();
            SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
            queryBuilder.setTables(CITY_TABLE);

            String[] projection = new String[] { ROW_ID,
                    String.format("%s as %s", CITY_NAMEEN_COLUMN, SearchManager.SUGGEST_COLUMN_TEXT_1),
                    String.format("%s as %s", CITY_PROVINCE_COLUMN, SearchManager.SUGGEST_COLUMN_TEXT_2),
                    String.format("%s||'|'||%s||'|'||%s as %s", CITY_CODE_COLUMN, CITY_ISFAVORITE_COLUMN, CITY_NAMEEN_COLUMN, SearchManager.SUGGEST_COLUMN_INTENT_DATA),
            };
            String selection = String.format("%s LIKE '%s%%'", CITY_NAMEEN_COLUMN, query);
            String sortOrder = String.format("%s,%s", CITY_NAMEEN_COLUMN, CITY_PROVINCE_COLUMN);
            Cursor cursor = queryBuilder.query(db, projection, selection, null, null, null, sortOrder);
            if (cursor.getCount() == 1) {
                cursor.moveToFirst();
                sLastSuggestionQuery = query;
                sLastSuggestion = cursor.getString(3);
                cursor.moveToPrevious();
            }
            return cursor;
        } catch (Exception ex) {
        }
        return null;
    }

    public static ContentValues getSuggestionContent(String suggestionData) {
        String[] params = suggestionData.split("\\|");
        if (params != null && params.length == 3) {
            ContentValues content = new ContentValues();
            content.put(CITY_CODE_COLUMN, params[0]);
            boolean isFav = params[1].equals("1");
            content.put(CITY_ISFAVORITE_COLUMN, isFav);
            content.put(CITY_NAMEEN_COLUMN, params[2]);
            return content;
        }
        return null;
    }

    public static String getLastSuggestionQuery() {
        return sLastSuggestionQuery;
    }

    public static String getLastSuggestion() {
        return sLastSuggestion;
    }

    public static Exception getLastQueryException() {
        return sLastQueryException;
    }

    public static String getDistanceSquaredProjection(Location location, String colName) {
        String selection = null;
        double lat = 0.0, lon = 0.0;
        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
        }
        return String.format("(((%.3f-%s)*(%.3f-%s))+((%.3f-%s)*(%.3f-%s))) as %s",
                lon, CITY_LONGITUDE_COLUMN, lon, CITY_LONGITUDE_COLUMN,
                lat, CITY_LATITUDE_COLUMN, lat, CITY_LATITUDE_COLUMN, colName);
    }

    public static Cursor queryClosestCity(ContentResolver contentResolver, Location location) {
        ArrayList<String> projection = new ArrayList<String>();
        projection.add(CleverWeatherProvider.ROW_ID);
        projection.add(CleverWeatherProvider.CITY_CODE_COLUMN);
        projection.add(CleverWeatherProvider.CITY_NAMEEN_COLUMN);
        projection.add(CleverWeatherProvider.CITY_NAMEFR_COLUMN);
        projection.add(CleverWeatherProvider.CITY_ISFAVORITE_COLUMN);
        projection.add(CleverWeatherProvider.CITY_LATITUDE_COLUMN);
        projection.add(CleverWeatherProvider.CITY_LONGITUDE_COLUMN);
        String colName = "dist";
        projection.add(getDistanceSquaredProjection(location, colName));
        String orderBy = colName + " limit 1";
        String selection = CleverWeatherProvider.CITY_PROVINCE_COLUMN + "<>'HEF'";

        return contentResolver.query(CleverWeatherProvider.CITY_URI, projection.toArray(new String[projection.size()]), selection, null, orderBy);
    }
    protected static class DbHelper2 extends DbHelper {
        private Context mContext;

        public DbHelper2(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            super.onCreate(db);

            //populate the City table
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(mContext.getAssets().open("cities.sql")));
                String line;

                while ((line = in.readLine()) != null) {
                    try {
                        db.execSQL(line);
                    } catch (SQLException sqlE) {
                    }
                }
            } catch (IOException ioe) {
            } finally {
                try {
                    if (in != null)
                        in.close();
                } catch (IOException ioe2) {

                }
            }
        }
    }
}
