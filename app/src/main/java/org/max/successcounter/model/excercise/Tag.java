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

    @DatabaseField ( unique = true, canBeNull = false, columnDefinition = "VARCHAR NOT NULL" )
    String tag;

    @Override
    public boolean equals( Object o )
    {
        if( o == null )
            return false;

        if( !( o instanceof Tag ))
            return false;

        String name = ( (Tag ) o ).getTag();

        if( name == null )
            return false;

        return name.equals( tag );
    }
}
