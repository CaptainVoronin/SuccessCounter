package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

/**
 * The class implements a single option in a compound exercise
 */
@DatabaseTable(tableName = "options")
public class OptionDescription
{
    /**
     * If this field is true
     * the option means "miss" and should have 0 points
     * Can't be true if lastDefault is true
     */
    Boolean firstDefault;

    /**
     * If this field is true the option means "maximum points are collected"
     * It must have the points value equals to sum of all other options
     * Can't be true if firstDefault is true
     */
    Boolean lastDefault;

    /**
     * The primary key
     */
    @Getter
    @Setter
    @DatabaseField(generatedId = true)
    Integer id;

    public Boolean getFirstDefault()
    {
        return firstDefault == null ? false : firstDefault;
    }

    /**
     * The points which this option brings to the player
     * If firstDefault is true it should be 0
     */
    @Getter
    @DatabaseField
    Integer points;

    public Boolean getLastDefault()
    {
        return lastDefault == null ? false : lastDefault;
    }

    public void setFirstDefault(Boolean value)
    {
        if (value && lastDefault)
            throw new IllegalArgumentException();
        this.firstDefault = value;
    }

    public void setLastDefault(Boolean value)
    {
        if (firstDefault && value)
            throw new IllegalArgumentException();
        this.lastDefault = value;
    }

    public void setPoints(int value)
    {
        if (firstDefault && value != 0)
            throw new IllegalArgumentException();
        points = value;
    }

    /**
     * Text description for UI
     */
    @Getter
    @Setter
    @DatabaseField
    String description;
    /**
     * The option order number in the list
     */
    @Getter
    @Setter
    @DatabaseField
    int orderNum;
    /**
     * The RGB code of the background color for the option button in the exercise activity
     * The default value is #001C2B
     */
    @Getter
    @Setter
    @DatabaseField
    int color;
    @Getter
    @Setter
    @DatabaseField( canBeNull = false, foreign = true, columnName = "parent_id",
            columnDefinition = "INTEGER REFERENCES template(id) ON DELETE CASCADE" )
    Template parent;

    public OptionDescription()
    {
        color = 0x001C2B;
    }
}
