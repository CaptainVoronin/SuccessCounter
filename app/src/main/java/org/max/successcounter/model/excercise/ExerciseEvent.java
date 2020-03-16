package org.max.successcounter.model.excercise;

class ExerciseEvent implements IExerciseEvent
{
    Type type;

    public ExerciseEvent(Type type)
    {
        this.type = type;
    }

    @Override
    public Type getType()
    {
        return type;
    }
}
