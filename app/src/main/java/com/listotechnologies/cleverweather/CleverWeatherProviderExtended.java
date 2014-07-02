package com.listotechnologies.cleverweather;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.CancellationSignal;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

public class CleverWeatherProviderExtended extends CleverWeatherProvider {
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
                    //if value is over 2 hours old, delete and re-query
                    long expiryTime = cursor.getLong(0) + 7200000;
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
                            db.delete(FORECAST_TABLE, selection, selectionArgs);
                            ForecastParser.parseXml(getContext(), provAbbr, cityCode);
                        }
                    }
                }
            } catch (SQLiteException ex) {
            }
        }
        return super.query(uri, projection, selection, selectionArgs, sortOrder);
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
