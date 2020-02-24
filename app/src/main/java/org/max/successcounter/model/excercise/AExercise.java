package org.max.successcounter.model.excercise;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public abstract class AExercise implements IExercise
{
    @Getter
    @Setter
    String name;

    @Getter
    @Setter
    Integer id;
    @Getter
    List<IStep> steps;
    @Getter
    @Setter
    Template template;
    Result result;

    public AExercise()
    {
        steps = new ArrayList<>();
    }

    @Override
    public boolean isMinOrMax(int index, float value)
    {
        boolean isMax = false;
        boolean isMin = false;
        float min = 100f;
        float max = 0f;
        int indexMin = 0;
        int indexMax = 0;

        int i = 0;
        for (IStep step : steps)
        {
            if (step.getPercent() >= max)
            {
                max = step.getPercent();
                indexMax = i;
            }
            if (step.getPercent() <= min)
            {
                min = step.getPercent();
                indexMin = i;
            }

            i++;
        }

        if ((value <= min) && (index == indexMin))
            isMin = true;

        if ((value >= max) && (index == indexMax))
            isMax = true;

        return isMin || isMax;
    }

    @Override
    public void addStep(IStep step)
    {
        steps.add(step);
    }

    @Override
    public IStep undo()
    {
        IStep step = null;

        if (steps.size() != 0)
        {
            step = steps.get(steps.size() - 1);
            steps.remove(step);
        }
        return step;
    }

    public List<Entry> getPercentHistory()
    {
        List<IStep> steps = getSteps();
        List<Entry> items = new ArrayList<>();
        int i = 0;
        for (IStep step : steps)
        {
            items.add(new Entry(i, step.getPercent()));
            i++;
        }

        return items;
    }

    @Override
    public boolean isFinished()
    {
        return false;
    }

    public Float getPercentAtStep(int stepNum)
    {
        if (stepNum < 0 || stepNum >= steps.size())
            return 0f;
        else
            return steps.get(stepNum).getPercent();
    }

    @Override
    public int getAttemptsCount()
    {
        return steps.size();
    }

    @Override
    public Result getResult()
    {
        if (result == null)
        {
            result = new Result();
            result.setParent(getTemplate());
        }

        result.setShots(getAttemptsCount());
        result.setPercent(steps.get(steps.size() - 1).getPercent());
        result.setPoints(getTotalPoints());

        return result;
    }

    @Override
    public void setSteps(List<IStep> steps)
    {
        this.steps = steps;
        this.steps.forEach(step -> step.setExercise(this));
    }

    @Override
    public boolean hasSummaryStep()
    {
        return false;
    }
}