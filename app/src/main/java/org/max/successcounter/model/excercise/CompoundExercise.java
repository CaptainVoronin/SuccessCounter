package org.max.successcounter.model.excercise;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

public class CompoundExcercise extends AExercise
{
    @Getter
    @Setter
    List<Option> options;

    int maxShotPoints;
    int maxResult;

    /**
     * If fact, this option sets the logical operations
     * which combines exercise outcomes AND  - several outcomes possible (the object ball pocketed
     * and the cue ball is drawn to the rail, for instance). If the option set to false the only
     * one from outcome is possible
     */
    @Getter @Setter
    boolean hasSummaryStep;

    public CompoundExcercise()
    {
        super();
        options = new ArrayList<>();
        maxResult = -1;
        hasSummaryStep = true;
        maxShotPoints = -1;
    }

    /**
     * Set the maximum possible value points for one step
     */
    private void setMaxShotPoints()
    {
        // If the exercise has summary step it is the last step in the options list
        if (hasSummaryStep)
            maxShotPoints = options.get(options.size() - 1).getPoints();
        else
            maxShotPoints = (int) options.stream().collect(Collectors.maxBy((Option o1, Option o2) -> Integer.compare(o1.getPoints(), o2.getPoints()))).get().getPoints();
    }

    public void addOption( Option opt )
    {
        options.add( opt );
    }


    @Override
    public IStep addStepByPoints(Integer points)
    {
        incAttempts();
        Float percent = 100f * (points + getTotalPoints()) / (getMaxPossiblePoints() * getAttempts());
        IStep step = new Step();
        step.setPercent( percent );
        step.setPoints( points );
        steps.add( step );

        return step;
    }

    @Override
    public int getAttemptsCount()
    {
        return getAttempts();
    }

    @Override
    public IStep undo()
    {
        if( steps.size() == 0 )
            return null;

        decAttempts();
        return super.undo();
    }

    public Integer getTotalPoints()
    {
        long l = steps.stream().collect(Collectors.summarizingInt(IStep::getPoints)).getSum();
        return ( int ) l;
    }

    @Override
    public int getMaxPossiblePoints()
    {
        if (maxShotPoints == -1)
            setMaxShotPoints();
        return maxShotPoints;
    }

    public static class Option{
        @Getter @Setter
        String description;

        @Getter @Setter
        Integer points;

        @Getter @Setter
        int color;
    }
}
