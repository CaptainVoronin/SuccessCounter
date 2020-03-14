package org.max.successcounter.model.excercise;

import lombok.Getter;

public class UndoEvent extends ExerciseEvent
{
    @Getter
    private IStep step;

    public UndoEvent(IStep step)
    {
        super(Type.Undo);
        this.step = step;
    }
}
