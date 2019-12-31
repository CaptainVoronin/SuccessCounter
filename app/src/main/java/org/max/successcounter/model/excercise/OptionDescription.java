package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "options")
public class OptionDescription
{
    @DatabaseField( generatedId = true, allowGeneratedIdInsert = true )
    Integer id;

    @DatabaseField
    Integer points;

    @DatabaseField
    String description;

    @DatabaseField( foreign = true, columnName = "parent_id" )
    Template parent;

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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public Template getParent()
    {
        return parent;
    }

    public void setParent(Template parent)
    {
        this.parent = parent;
    }
}
