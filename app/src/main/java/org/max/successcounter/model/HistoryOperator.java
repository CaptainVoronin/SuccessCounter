package org.max.successcounter.model;

import com.github.mikephil.charting.data.Entry;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;

import org.max.successcounter.db.DatabaseHelper;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public enum HistoryOperator
{
    instance;

    Dao<HistoryItem, Integer> histDao;
    Exercise exercise;
    List<HistoryItem> items;

    public static ArrayList<Entry> historyToPercent(List<HistoryItem> items)
    {
        // TODO: implement it
        return null;
    }

    private Exercise getExercise() throws SQLException
    {
        if (exercise == null)
            throw new SQLException("No parent exercise set");

        return exercise;
    }

    public void setExercise(Exercise exercise)
    {
        this.exercise = exercise;
        items = new ArrayList<>();
    }

    void clearHistory() throws SQLException
    {
        DeleteBuilder<HistoryItem, Integer > dbl = getDAO().deleteBuilder();
        dbl.where().eq( "parent_id", getExercise().getId() );
        dbl.delete();
    }

    public void createDAO(DatabaseHelper db) throws SQLException
    {
        histDao = db.getDao(HistoryItem.class);
    }

    public List<HistoryItem> getHistory() throws SQLException
    {
        QueryBuilder<HistoryItem, Integer> bld = getDAO().queryBuilder();
        bld.where().eq("parent_id", getExercise().getId());
        return bld.query();
    }

    public void addItem(int points)
    {
        HistoryItem item = new HistoryItem();
        item.setPoints(points);
        item.setParent(exercise);
        items.add(item);
    }

    public void putItem(HistoryItem item) throws SQLException
    {
        if (item.getId() == -1)
            getDAO().create(item);
        items.add( item );
    }

    Dao<HistoryItem, Integer> getDAO() throws SQLException
    {
        if (histDao == null)
            throw new SQLException("DAO not initialized properly");
        return histDao;
    }

    public void clearHistoryForExercise() throws SQLException
    {
        DeleteBuilder<HistoryItem, Integer> bld = getDAO().deleteBuilder();
        bld.where().eq("parent_id", getExercise().getId());
        bld.delete();
    }

    public void undo() throws SQLException
    {
        if( items.size() == 0 )
            return;

        HistoryItem item = items.get( items.size() - 1 );
        deleteItem( item );
        items.remove( items.size() - 1 );
    }

    private void deleteItem(HistoryItem item) throws SQLException
    {
        getDAO().delete( item );
    }

    public List<HistoryItem> getItems()
    {
        return items;
    }

    public List<Entry> getPercentHistory()
    {
        List<Entry> list = new ArrayList<>();
        int i = 0;

        for( HistoryItem item : items )
        {

        }

        return list;
    }

}
