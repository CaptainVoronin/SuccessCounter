package org.max.successcounter.model.excercise;

public class ElementaryStep implements IStep
{
    Integer points;
    Float percent;
    IExercise exercise;
    Integer id;

    @Override
    public Float getPercent()
    {
        return percent;
    }

    @Override
    public void setPercent(Float percent)
    {
        this.percent = percent;
    }

    @Override
    public Integer getPoints()
    {
        return points;
    }

    @Override
    public void setPoints(Integer points)
    {
        this.points = points;
    }

    @Override
    public void setExercise(IExercise exercise)
    {
        this.exercise = exercise;
    }

    @Override
    public IExercise getExercise()
    {
        return exercise;
    }

    @Override
    public Integer getId()
    {
        return id;
    }

    @Override
    public void setId(Integer id)
    {
        this.id = id;
    }
}
