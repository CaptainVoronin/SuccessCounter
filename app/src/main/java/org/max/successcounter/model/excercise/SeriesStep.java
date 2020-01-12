package org.max.successcounter.model.excercise;

public class SeriesStep implements IStep
{
    int id;
    SeriesExercise exercise;
    int points;
    Float percent;

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
        return 1;
    }

    @Override
    public void setPoints(Integer points)
    {
        // deliberately empty
    }

    @Override
    public void setExercise(IExercise exercise)
    {
        this.exercise = ( SeriesExercise ) exercise;
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
