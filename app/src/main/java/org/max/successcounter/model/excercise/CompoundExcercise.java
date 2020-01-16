package org.max.successcounter.model.excercise;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class CompoundExcercise extends AExercise
{
    @Getter
    @Setter
    List<Option> options;

    int maxResult;
    int attempts;

    @Getter @Setter
    boolean hasSummaryStep;

    public CompoundExcercise()
    {
        super();
        options = new ArrayList<>();
        maxResult = -1;
        attempts = 0;
        hasSummaryStep = true;
    }

    public void addOption( Option opt )
    {
        options.add( opt );
    }


    @Override
    public IStep addStepByPoints(Integer points)
    {
        attempts++;
        int max = getMaxPossiblePoints();
        Float percent = 100f * ( points + getSuccessCount() ) / ( max * attempts );

        IStep step = new Step();
        step.setPercent( percent );
        step.setPoints( points );
        steps.add( step );

        return step;
    }

    @Override
    public int getMaxPossiblePoints()
    {
        return options.get( options.size() - 1 ).getPoints();
    }

    @Override
    public int getAttemptsCount()
    {
        return attempts;
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
