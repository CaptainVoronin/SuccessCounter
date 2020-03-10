package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "options")
public class OptionDescription
{
    Boolean firstDefault;

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

    public void setFirstDefault(Boolean firstDefault)
    {
        this.firstDefault = firstDefault;
    }

    public Boolean getLastDefault()
    {
        return lastDefault == null ? false : lastDefault;
    }

    public void setLastDefault(Boolean lastDefault)
    {
        this.lastDefault = lastDefault;
    }

    /**
     * The points which this option brings to the player
     */
    @Getter
    @Setter
    @DatabaseField
    Integer points;
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
