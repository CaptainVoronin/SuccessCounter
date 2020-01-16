package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@DatabaseTable(tableName = "options")
public class OptionDescription
{
    Boolean firstDefault;

    Boolean lastDefault;

    @DatabaseField( generatedId = true )
    Integer id;

    @DatabaseField
    Integer points;

    @DatabaseField
    String description;

    @DatabaseField( foreign = true, columnName = "parent_id" )
    Template parent;
}
