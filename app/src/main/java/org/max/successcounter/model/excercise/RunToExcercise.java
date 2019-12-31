package org.max.successcounter.model.excercise;

public class RunToExcercise extends SimpleExercise
{
    boolean finished;

    public RunToExcercise()
    {
        super();
        finished = false;
    }

    public RunToExcercise( int limit )
    {
        super();
        setLimit( limit );
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

    int limit;

    @Override
    public IStep addStepByPoints(Integer points)
    {
        // Упражнение с ограничением
        // выполняется до первого промаха
        if( points == 0 )
        {
            finished = true;
            return null;
        }

        if( isFinished() )
            return null;

        IStep step = new Step();
        step.setPoints( 1 );
        steps.add( step );
        step.setPercent( 100f * steps.size() / getLimit() );

        if( steps.size() == getLimit() )
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
        finished = false;
        return super.undo();
    }
}
