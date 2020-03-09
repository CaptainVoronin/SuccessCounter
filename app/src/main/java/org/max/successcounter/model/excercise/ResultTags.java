package org.max.successcounter.model.excercise;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import lombok.Data;

@Data
@DatabaseTable( tableName = "result_tags" )
public class ResultTags
{
    @DatabaseField(generatedId = true, allowGeneratedIdInsert = true)
    Integer id;

    @DatabaseField ( canBeNull = false, columnDefinition = "INTEGER NOT NULL REFERENCES result( id ) ON DELETE CASCADE")
    Integer resultID;

    @DatabaseField ( canBeNull = false, columnDefinition = "INTEGER NOT NULL REFERENCES tags( id ) ON DELETE CASCADE")
    Integer tagID;

    public ResultTags(Integer resultID, Integer tagID)
    {
        this.resultID = resultID;
        this.tagID = tagID;
    }

    public ResultTags()
    {
    }

}
