package org.max.successcounter.model.excercise;

public class SeriesExercise extends AExercise
{
    @Override
    public IStep addStepByPoints(Integer points)
    {
        SeriesStep step = new SeriesStep();
        step.setPoints( 1 );
        steps.add( step );
        step.setPercent((float) steps.size());
        return null;
    }

    @Override
    public Integer getSuccessCount()
    {
        return steps.size();
    }

    @Override
    public int getMaxPossiblePoints()
    {
        return 1;
    }
}
