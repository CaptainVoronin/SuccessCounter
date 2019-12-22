package org.max.successcounter.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "exerciseset")
public class ExerciseSet
{
    @DatabaseField ( generatedId = true, allowGeneratedIdInsert = true )
    Integer id;

    @DatabaseField
    String name;

    @ForeignCollectionField( eager = true )
    ForeignCollection<Exercise> exercises;

    List<Exercise> asList;

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public ForeignCollection<Exercise> getExercises()
    {
        return exercises;
    }

    public List<Exercise> getExercisesAsList()
    {
        if( asList == null )
        {
            asList = new ArrayList<>();
            asList.addAll( exercises );
        }
        return asList;
    }

    public void setExercises(ForeignCollection<Exercise> exercises)
    {
        this.exercises = exercises;
    }
}
