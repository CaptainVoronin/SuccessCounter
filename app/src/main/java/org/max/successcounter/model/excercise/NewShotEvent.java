package org.max.successcounter.model.excercise;

import lombok.Getter;

public class NewShotEvent extends ExerciseEvent
{
    @Getter
    private IStep step;

    public NewShotEvent(IStep step)
    {
        super(Type.ShotAdded);
        this.step = step;
    }
}
