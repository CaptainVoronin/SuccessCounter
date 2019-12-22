package org.max.successcounter.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

@DatabaseTable( tableName = "exercise")
public class Exercise
{

    public static final String PARENT_FIELD = "parent_id";

    final static SimpleDateFormat sdf = new SimpleDateFormat( "dd.MM.yyyy ");

    @DatabaseField( generatedId = true )
    Integer id;

    @DatabaseField
    java.util.Date date;

    @DatabaseField
    int attemptsCount;

    @DatabaseField
    int successCount;

    @DatabaseField( foreign = true, columnName = "parent_id" )
    ExerciseSet parent;

    public ExerciseSet getParent()
    {
        return parent;
    }

    public void setParent(ExerciseSet parent)
    {
        this.parent = parent;
    }

    public Exercise()
    {
        date = java.util.Calendar.getInstance().getTime();
        attemptsCount = 0;
        successCount = 0;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public int getAttemptsCount()
    {
        return attemptsCount;
    }

    public void setAttemptsCount(int attemptsCount)
    {
        this.attemptsCount = attemptsCount;
    }

    public int getSuccessCount()
    {
        return successCount;
    }

    public void setSuccessCount(int successCount)
    {
        this.successCount = successCount;
    }

    public int incSuccess()
    {
        successCount++;
        attemptsCount++;
        return successCount;
    }

    public int incAttempts()
    {
        attemptsCount++;
        return attemptsCount;
    }

    public static final String getPercentString( Exercise ex )
    {
        float res = 100f * ((float)ex.getSuccessCount()) / ex.getAttemptsCount();
        double d = Math.round(res * 10) / 10.0;
        return String.format( "%.1f%%", d);
    }

    public static final String getFormattedDate( Exercise ex )
    {
        return sdf.format( ex.getDate() );
    }

    public double getPercent()
    {
        float val = ( 100f * getSuccessCount() / getAttemptsCount() );
        return Math.round( val * 10 ) / 10.0;
    }
}
