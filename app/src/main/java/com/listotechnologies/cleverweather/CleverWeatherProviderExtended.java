package com.listotechnologies.cleverweather;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.SQLException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CleverWeatherProviderExtended extends CleverWeatherProvider {
    @Override
    public boolean onCreate() {
        myOpenHelper = new DbHelper2(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
        return true;
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
