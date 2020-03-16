package org.max.successcounter.model.excercise;

public interface IStep
{
    Float getPercent();
    void setPercent(Float percent);
    Integer getPoints();
    void setPoints( Integer points );
    void setExercise( IExercise exercise );
    IExercise getExercise( );
    Integer getId();
    void setId( Integer id );
}
