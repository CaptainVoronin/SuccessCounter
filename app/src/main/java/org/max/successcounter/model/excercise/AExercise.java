package org.max.successcounter.model.excercise;

import com.github.mikephil.charting.data.Entry;
import java.util.ArrayList;
import java.util.List;

public abstract class AExercise implements IExercise
{
    String name;
    Integer id;
    List<IStep> steps;
    Template template;
    Result result;

    @Override
    public void setTemplate(Template template)
    {
        this.template = template;
    }

    @Override
    public Template getTemplate()
    {
        return template;
    }

    @Override
    public void setName(String name)
    {
        this.name = name;
    }

    public AExercise()
    {
        steps = new ArrayList<>();
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public Integer getId()
    {
        return id;
    }

    public void setId( Integer id )
    {
        this.id = id;
    }

    @Override
    public List<IStep> getSteps()
    {
        return steps;
    }

    @Override
    public void addStep(IStep step)
    {
        steps.add( step );
    }

    @Override
    public IStep undo()
    {
        IStep step = getLastStep();
        if( step != null )
            steps.remove( step );
        return  step;
    }

    public List<Entry> getPercentHistory()
    {
        List<IStep> steps = getSteps();
        List<Entry> items = new ArrayList<>();
        int i = 0;
        for( IStep step : steps )
        {
            items.add( new Entry( i, step.getPercent() ) );
            i++;
        }

        return items;
    }

    @Override
    public IStep getLastStep()
    {
        if( steps.size() != 0 )
            return steps.get( steps.size() - 1 );
        else
            return null;
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
        if( result == null )
        {
            result = new Result();
            result.setParent( getTemplate() );
        }

        result.setShots( getAttemptsCount() );
        result.setPercent( getLastStep().getPercent() );
        result.setPoints( getSuccessCount() );

        return result;
    }

    @Override
    public void setSteps(List<IStep> steps)
    {
        this.steps = steps;
        this.steps.forEach( step -> step.setExercise( this ) );
    }

    @Override
    public boolean hasSummaryStep()
    {
        return false;
    }
}