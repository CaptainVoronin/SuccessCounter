package org.max.successcounter.model.excercise;

import java.util.stream.Collectors;

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
    public IStep addStepByPoints(Integer points)
    {
        IStep step = null;

        // Prevent add points to a finished exercise
        if (isFinished())
            return null;

        step = new Step();
        step.setPoints(points);
        steps.add(step);
        step.setPercent(100f * getTotalPoints() / getAttemptsCount());

        if (template.getSuccesLimited())
        {
            // This is the case
            // when a player must pocket exact number of balls
            // and may miss any number of shots
            if (getTotalPoints() == getLimit())
                // the limit of successful shots is reached
                finished = true;
        }
        else
        {

            if( steps.size() == getLimit() )
                finished = true;
        }

        return step;
    }

    @Override
    public boolean isFinished()
    {
        return finished;
    }

    @Override
    public Integer getTotalPoints()
    {
        return steps.stream().collect( Collectors.summingInt( IStep::getPoints )).intValue();
    }

    @Override
    public IStep undo()
    {
        if (isFinished())
            finished = false;
        return super.undo();
    }
}