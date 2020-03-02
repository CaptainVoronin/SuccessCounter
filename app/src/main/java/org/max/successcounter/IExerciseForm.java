package org.max.successcounter;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.IExercise;

import java.sql.SQLException;

interface IExerciseForm<T extends IExercise>
{
    void setExerсise( T exercise );
    IExercise getExercise();
    <T> Dao<T,Integer> getDao( Class<T> exerciseClass, DatabaseHelper db ) throws SQLException;
    void onExerciseFinished();
    String getEfficiencyString();
    String getAttemptsString();
}
