package org.max.successcounter.model.excercise;

public interface IExerciseEvent
{
    Type getType();

    ;

    enum Type
    {ShotAdded, Finished, undo}
}
