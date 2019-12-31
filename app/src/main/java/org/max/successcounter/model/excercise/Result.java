package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

@DatabaseTable(tableName = "result")
public class Result
{

    final static SimpleDateFormat sdf = new SimpleDateFormat( "dd.MM.yyyy ");

    @DatabaseField( generatedId = true, allowGeneratedIdInsert = true )
    Integer id;

    @DatabaseField( foreign = true, columnName = "parent_id" )
    Template parent;

    @DatabaseField
    Float percent;

    @DatabaseField
    Integer shots;

    @DatabaseField
    Date date;

    @DatabaseField
    Integer points;

    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    public Result()
    {
        date = Calendar.getInstance().getTime();
    }

    public Date getDate()
    {
        return date;
    }

    public void setDate(Date date)
    {
        this.date = date;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Template getParent()
    {
        return parent;
    }

    public void setParent(Template parent)
    {
        this.parent = parent;
    }

    public Float getPercent()
    {
        return percent;
    }

    public void setPercent(Float percent)
    {
        this.percent = percent;
    }

    public Integer getShots()
    {
        return shots;
    }

    public void setShots(Integer shots)
    {
        this.shots = shots;
    }

    public static final String getPercentString( Result res )
    {
        float r = 100f * ((float)res.getPercent()) / res.getShots();
        double d = Math.round(r * 10) / 10.0;
        return String.format( "%.1f%%", d);
    }

    public static final String getFormattedDate( Result ex )
    {
        return sdf.format( ex.getDate() );
    }
}
