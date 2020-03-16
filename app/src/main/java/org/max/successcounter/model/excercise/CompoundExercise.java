package org.max.successcounter.model.excercise;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.Setter;

/**
 * This is a class which provides functionality for a compound
 * exercise where a shot could bring different value of points
 */
public class CompoundExercise extends BaseExercise
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

    public CompoundExercise(Template template)
    {
        super(template);
        options = new ArrayList<>();
        maxResult = -1;
        hasSummaryStep = true;
        maxShotPoints = -1;
        setHasSummaryStep(template.isHasSummaryStep());
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
    public int getTotalPoints()
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