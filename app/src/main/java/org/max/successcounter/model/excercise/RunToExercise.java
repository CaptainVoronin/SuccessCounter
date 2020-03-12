package org.max.successcounter.model.excercise;

import lombok.Getter;
import lombok.Setter;

public class RunToExercise extends SimpleExercise
{
    boolean finished;

    @Getter
    @Setter
    int limit;

    public RunToExercise()
    {
        super();
        finished = false;
    }

    public RunToExercise(int limit)
    {
        super();
        setLimit(limit);
        finished = false;
    }

    @Override
    public IStep addNewShot(int points)
    {
        boolean isFinished = false;
        // Prevent add points to a finished exercise
        if (isFinished())
            return null;

        IStep step = super.addNewShot(points);

        if (template.getSuccesLimited())
        {
            // This is the case
            // when a player must pocket exact number of balls
            // and may miss any number of shots
            if (getTotalPoints() == getLimit())
                // the limit of successful shots is reached
                isFinished = true;
        }
        else
        {
            if( steps.size() == getLimit() )
                isFinished = true;
        }

        if (isFinished && !finished)
        {
            finished = isFinished;
            publishFinishEvent();
        }

        return step;
    }

    @Override
    public IStep undo()
    {
        if (isFinished())
            finished = false;
        return super.undo();
    }
}