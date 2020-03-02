package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@Data
@DatabaseTable ( tableName = "tags" )
public class Tag
{
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    Integer id;

    @DatabaseField ( unique = true )
    String tag;
}
