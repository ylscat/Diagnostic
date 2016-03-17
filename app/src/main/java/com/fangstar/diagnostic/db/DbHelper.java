package com.fangstar.diagnostic.db;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created at 2016/3/17.
 *
 * @author YinLanShan
 */
public class DbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 100;

    public DbHelper(Context context, String name) {
        super(context, name, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private static final String CREATE_TABLE =
            "CREATE TABLE CALL_LOG ("
            + "start_time integer primary key, "
            + "number text, "
            + "call_time integer, "
            + "call_duration integer, "
            + "offhook_time integer, "
            + "idle_time integer, "
            + "fail integer);";
}
