package org.max.successcounter.model.excercise;

import com.github.mikephil.charting.data.Entry;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.subjects.PublishSubject;
import lombok.Getter;
import lombok.Setter;

public abstract class AExercise implements IExercise, Publisher<IExerciseEvent>
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

    @Getter @Setter
    long duration;

    @Getter @Setter
    String comment;

    @Getter
    boolean finished;

    @Getter
    PublishSubject<IExerciseEvent> publisher;

    public AExercise()
    {
        steps = new ArrayList<>();
        publisher = PublishSubject.create();
        finished = false;
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
    public IStep undo()
    {
        IStep step = null;

        if (steps.size() != 0)
        {
            step = steps.get(steps.size() - 1);
            steps.remove(step);
            publisher.onNext(new UndoEvent(step));

            if (isFinished())
            {
                finished = false;
                publisher.onNext(new ResumeEvent());
            }
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
    public final int getAttemptsCount()
    {
        return steps.size();
    }

    @Override
    public final Result getResult()
    {
        if (result == null)
        {
            result = new Result();
            result.setParent(getTemplate());
        }

        result.setShots(getAttemptsCount());
        if( steps.size() != 0)
            result.setPercent(steps.get(steps.size() - 1).getPercent());
        else
            result.setPercent(0f);

        result.setPoints(getTotalPoints());
        result.setComment( getComment() );

        return result;
    }

    @Override
    public final void setSteps(List<IStep> steps)
    {
        this.steps = steps;
        this.steps.forEach(step -> step.setExercise(this));

    }

    @Override
    public IStep addNewShot(int points)
    {
        boolean isFinished = false;
        // Prevent adding points to a finished exercise
        if (isFinished())
            return null;

        Float percent = 100f * (points + getTotalPoints()) / (getMaxPossiblePoints() * (getAttempts() + 1));
        IStep step = new Step();
        step.setPercent(percent);
        step.setPoints(points);
        steps.add(step);

        ExerciseEvent e = new NewShotEvent(step);
        publisher.onNext(e);

        if (template.getSuccesLimited())
        {
            // This is the case
            // when a player must pocket exact number of balls
            // and may miss any number of shots
            if (getTotalPoints() == template.getLimit())
                // the limit of successful shots is reached
                isFinished = true;
        } else
        {
            if (steps.size() == template.getLimit())
                isFinished = true;
        }

        if (isFinished && !finished)
        {
            finished = isFinished;
            publishFinishEvent();
        }

        return step;
    }

    protected int getAttempts()
    {
        return steps.size();
    }

    @Override
    public int getTotalPoints()
    {
        return (int) steps.stream().filter(step -> step.getPoints() != 0).count();
    }

    @Override
    public int getMaxPossiblePoints()
    {
        return 1;
    }

    protected final void publishFinishEvent()
    {
        publisher.onNext(new FinishEvent());
    }

    @Override
    public void subscribe(Subscriber<? super IExerciseEvent> s)
    {
        //publisher.subscribe(s);
    }
}