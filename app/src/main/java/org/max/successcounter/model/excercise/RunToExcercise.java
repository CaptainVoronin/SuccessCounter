package org.max.successcounter.model.excercise;

public class RunToExcercise extends SimpleExercise
{
    boolean finished;
    int limit;

    public RunToExcercise()
    {
        super();
        finished = false;
    }

    public RunToExcercise(int limit)
    {
        super();
        setLimit(limit);
        finished = false;
    }

    public int getLimit()
    {
        return limit;
    }

    public void setLimit(int limit)
    {
        this.limit = limit;
    }

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

        IStep step = new Step();
        step.setPoints(points);
        steps.add(step);
        step.setPercent(100f * steps.size() / getLimit());

        if (steps.size() == getLimit())
            finished = true;

        return step;
    }

    @Override
    public boolean isFinished()
    {
        return finished;
    }

    @Override
    public Integer getSuccessCount()
    {
        return steps.size();
    }

    @Override
    public int getAttemptsCount()
    {
        return limit;
    }

    @Override
    public IStep undo()
    {
        if (isFinished())
        {
            finished = false;
            return null;
        } else
            return super.undo();
    }
}
