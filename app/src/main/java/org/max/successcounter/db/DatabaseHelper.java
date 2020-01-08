package org.max.successcounter.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import org.max.successcounter.R;
import org.max.successcounter.model.HistoryItem;
import org.max.successcounter.model.excercise.OptionDescription;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Template;

public final class DatabaseHelper extends OrmLiteSqliteOpenHelper
{
    private static final String DATABASE_NAME = "scounter.db";
    private static final int DATABASE_VERSION = 24;

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION, R.raw.ormlite_config); //
    }

    @Override
    public void onCreate(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource)
    {
        try
        {
            TableUtils.createTable(connectionSource, Template.class);
            TableUtils.createTable(connectionSource, OptionDescription.class);
            TableUtils.createTable(connectionSource, Result.class);
            TableUtils.createTable(connectionSource, HistoryItem.class);
        } catch (java.sql.SQLException e)
        {
            Log.e(DatabaseHelper.class.getName(), "Unable to create datbases", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqliteDatabase, ConnectionSource connectionSource, int oldVer, int newVer)
    {
        try
        {
            TableUtils.dropTable(connectionSource, Template.class, false);
            TableUtils.createTable(connectionSource, Template.class);

            TableUtils.dropTable(connectionSource, OptionDescription.class, false);
            TableUtils.createTable(connectionSource, OptionDescription.class);

            TableUtils.dropTable(connectionSource, Result.class, false);
            TableUtils.createTable(connectionSource, Result.class);

            TableUtils.dropTable(connectionSource, HistoryItem.class, false);
            TableUtils.createTable(connectionSource, HistoryItem.class);

        } catch (java.sql.SQLException e)
        {
            Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVer + " to new "
                    + newVer, e);
        }
    }
}
