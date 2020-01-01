package org.max.successcounter.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import org.max.successcounter.model.excercise.Result;

@DatabaseTable( tableName = "history")
public class HistoryItem
{
    @DatabaseField( generatedId = true )
    private Integer id;

    @DatabaseField
    private Integer points;

    @DatabaseField
    private Float percent;

    @DatabaseField( foreign = true, columnName = "parent_id" )
    private Result parent;

    @DatabaseField( canBeNull = false )
    private Integer templateID;

    public Integer getTemplateID()
    {
        return templateID;
    }

    public void setTemplateID(Integer templateID)
    {
        this.templateID = templateID;
    }

    public Result getParent()
    {
        return parent;
    }

    public void setParent(Result parent)
    {
        this.parent = parent;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public Integer getPoints()
    {
        return points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    public void setPercent(Float percent)
    {
        this.percent = percent;
    }

    public Float getPercent()
    {
        return percent;
    }

}
