package org.max.successcounter.model;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.IExercise;
import org.max.successcounter.model.excercise.IStep;
import org.max.successcounter.model.excercise.Step;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public enum HistoryOperator
{
    instance;

    private Dao<HistoryItem, Integer> histDao;
    private IExercise exercise;

    public IExercise getExercise()
    {
        return exercise;
    }

    public void clearHistory() throws SQLException
    {
        DeleteBuilder<HistoryItem, Integer > dbl = getDAO().deleteBuilder();
        dbl.where().eq( "templateID", exercise.getTemplate().getId() );
        dbl.delete();
    }

    public void init(DatabaseHelper db, IExercise exercise ) throws SQLException
    {
        histDao = db.getDao(HistoryItem.class);
        this.exercise = exercise;
    }

    public List<IStep> getHistory() throws SQLException
    {
        QueryBuilder<HistoryItem, Integer> bld = getDAO().queryBuilder();
        bld.where().eq("templateID", exercise.getTemplate().getId());
        List<IStep> steps = new ArrayList<>();
        List<HistoryItem> hist = bld.query();
        hist.forEach( hi -> {
            IStep step = new Step();
            step.setPercent( hi.getPercent() );
            step.setPoints( hi.getPoints() );
            step.setId( hi.getId() );
            steps.add( step );
        } );
        return steps;
    }

    public void saveStep(IStep step) throws SQLException
    {
        HistoryItem hi = new HistoryItem();
        hi.setTemplateID( exercise.getTemplate().getId() );
        hi.setPoints( step.getPoints() );
        hi.setPercent( step.getPercent( ) );
        hi.setParent( exercise.getResult() );
        getDAO().create(hi);
        step.setId( hi.getId() );
    }

    private Dao<HistoryItem, Integer> getDAO() throws SQLException
    {
        if (histDao == null)
            throw new SQLException("DAO not initialized properly");
        return histDao;
    }

    public int deleteStep(IStep step) throws SQLException
    {
        return getDAO().deleteById( step.getId() );
    }
}
