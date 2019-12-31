package org.max.successcounter.model.excercise;

public class SimpleExercise extends AExercise
{
    @Override
    public IStep addStepByPoints(Integer points)
    {
        IStep step = new Step();
        step.setPoints(points);
        step.setExercise(this);
        addStep(step);
        Float p = 100F * getSuccessCount() / getSteps().size();
        step.setPercent(p);
        return step;
    }

    public Integer getSuccessCount()
    {
        return (int) steps.stream().filter(step -> step.getPoints() != 0).count();
    }
}
