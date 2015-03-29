package com.fischer.tom.hackwesternmuse;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;


public class EmergencyContactContract {
    // inner class that defines the table contents
    public static abstract class EmergencyContactEntry implements BaseColumns {
        public static final String TABLE_NAME = "emergency_contacts";
        public static final String COLUMN_NAME_CONTACT_NAME = "contact_name";
        public static final String COLUMN_NAME_CONTACT_PHONE = "contact_phone";
        public static final String COLUMN_NAME_DELETE_ID = "delete_id";

        public static final String TEXT_TYPE = " TEXT";
        public static final String COMMA_SEP = ",";
        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + EmergencyContactEntry.TABLE_NAME + " (" +
                        EmergencyContactEntry._ID + " INTEGER PRIMARY KEY," +
                        EmergencyContactEntry.COLUMN_NAME_CONTACT_NAME + TEXT_TYPE + COMMA_SEP +
                        EmergencyContactEntry.COLUMN_NAME_CONTACT_PHONE + TEXT_TYPE + COMMA_SEP +
                        EmergencyContactEntry.COLUMN_NAME_DELETE_ID + " INTEGER" + ")";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + EmergencyContactEntry.TABLE_NAME;

        public static class EmergencyContactDbHelper extends SQLiteOpenHelper {
            // If you change the database schema, you must increment the database version.
            public static final int DATABASE_VERSION = 1;
            public static final String DATABASE_NAME = "EmergencyContact.db";

            public EmergencyContactDbHelper(Context context) {
                super(context, DATABASE_NAME, null, DATABASE_VERSION);
            }

            public void onCreate(SQLiteDatabase db) {
                db.execSQL(SQL_CREATE_ENTRIES);
            }

            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                // This database is only a cache for online data, so its upgrade policy is
                // to simply to discard the data and start over
                db.execSQL(SQL_DELETE_ENTRIES);
                onCreate(db);
            }

            public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                onUpgrade(db, oldVersion, newVersion);
            }
        }
    }
}

