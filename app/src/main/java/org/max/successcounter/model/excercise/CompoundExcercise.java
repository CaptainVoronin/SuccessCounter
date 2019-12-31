package org.max.successcounter.model.excercise;

import java.util.ArrayList;
import java.util.List;

public class CompoundExcercise extends AExercise
{
    List<Option> options;
    int maxResult;
    int attempts;

    public CompoundExcercise()
    {
        super();
        options = new ArrayList<>();
        maxResult = -1;
        attempts = 0;

        Option opt = new Option();
        opt.setDescription( "Miss" );
        opt.setPoints( 0 );
        options.add( opt );
    }

    public void addOption( Option opt )
    {
        options.add( opt );
    }

    public List<Option> getOptions()
    {
        Integer[] cnt= {0};

        List<Option> tmp = new ArrayList<>();
        tmp.addAll( options );

        options.stream().forEach( item-> cnt[0] += item.getPoints() );
        Option opt = new Option();
        opt.setDescription( "Complete" );
        opt.setPoints( cnt[0] );
        tmp.add( opt );
        return tmp;
    }

    @Override
    public IStep addStepByPoints(Integer points)
    {
        attempts++;
        int max = getMaxResult();
        Float percent = 100f * ( points + getSuccessCount() ) / ( max * attempts );

        IStep step = new Step();
        step.setPercent( percent );
        step.setPoints( points );
        steps.add( step );

        return step;
    }

    private int getMaxResult()
    {
        if( maxResult == -1 )
        {
            maxResult = 0;
            for (Option option : options)
                maxResult += option.getPoints();
        }

        return maxResult;
    }

    @Override
    public int getAttemptsCount()
    {
        return attempts * getMaxResult();
    }

    @Override
    public IStep undo()
    {
        if( steps.size() == 0 )
            return null;

        attempts--;
        return super.undo();
    }

    public Integer getSuccessCount()
    {
        final Integer[] count = {0};
        steps.forEach( item -> { count[0] += item.getPoints(); } );
        return count[0];
    }

    public static class Option{
        String description;
        Integer points;

        public String getDescription()
        {
            return description;
        }

        public void setDescription(String description)
        {
            this.description = description;
        }

        public Integer getPoints()
        {
            return points;
        }

        public void setPoints(Integer points)
        {
            this.points = points;
        }
    }
}
