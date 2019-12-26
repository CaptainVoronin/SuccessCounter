package org.max.successcounter.model;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable( tableName = "history")
public class HistoryItem
{
    @DatabaseField( generatedId = true )
    Integer id;

    @DatabaseField
    Integer points;

    @DatabaseField( foreign = true, columnName = "parent_id" )
    Exercise parent;

    public Exercise getParent()
    {
        return parent;
    }

    public void setParent(Exercise parent)
    {
        this.parent = parent;
    }

    public HistoryItem(){ id = -1; }

    public long getId()
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
}
