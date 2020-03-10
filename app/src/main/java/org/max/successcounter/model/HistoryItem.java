package org.max.successcounter.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.max.successcounter.model.excercise.Result;

import lombok.Getter;
import lombok.Setter;

@DatabaseTable( tableName = "history")
public class HistoryItem
{
    @Getter
    @Setter
    @DatabaseField( generatedId = true )
    private Integer id;

    @Getter
    @Setter
    @DatabaseField
    private Integer points;

    @Getter
    @Setter
    @DatabaseField
    private Float percent;

    @Getter
    @Setter
    @DatabaseField(foreign = true, columnName = "parent_id", columnDefinition = "INTEGER NOT NULL REFERENCES result( id ) ON DELETE CASCADE")
    private Result parent;

    @Getter
    @Setter
    @DatabaseField(canBeNull = false, columnDefinition = "INTEGER NOT NULL REFERENCES template( id ) ON DELETE CASCADE")
    private Integer templateID;

}
