package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@DatabaseTable(tableName = "options")
public class OptionDescription
{

    Boolean firstDefault;

    Boolean lastDefault;

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

    @Getter
    @Setter
    @DatabaseField( generatedId = true )
    Integer id;

    @Getter
    @Setter
    @DatabaseField
    Integer points;

    @Getter
    @Setter
    @DatabaseField
    String description;

    @Getter
    @Setter
    @DatabaseField( foreign = true, columnName = "parent_id" )
    Template parent;
}
