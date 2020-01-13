package org.max.successcounter.model.excercise;

public class SeriesExercise extends AExercise
{
    boolean finished;

    @Override
    public IStep addStepByPoints(Integer points)
    {
        // Prevent add points to a finished exercise
        if (isFinished())
            return null;

        // Упражнение с ограничением
        // выполняется до первого промаха
        if (points == 0)
        {
            finished = true;
            return null;
        }

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

    @Override
    public boolean isFinished()
    {
        return finished;
    }

}
