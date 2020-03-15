package org.max.successcounter.model.excercise;

import com.github.mikephil.charting.data.Entry;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.subjects.PublishSubject;
import lombok.Getter;
import lombok.Setter;

/**
 * This is the base class which contains all functions needed to provide
 * stats for an exercise with only two options — miss/hit with limits or without
 */
public class BaseExercise implements IExercise, Publisher<IExerciseEvent>
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
    Template template;

    Result result;

    @Getter
    @Setter
    long duration;

    @Getter
    @Setter
    String comment;

    @Getter
    PublishSubject<IExerciseEvent> publisher;

    @Getter
    private boolean finished;

    public BaseExercise(Template template)
    {
        this.template = template;
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
            publishUndoEvent(step);

            if (isFinished())
                setFinished(false);
        }
        return step;
    }

    private void publishUndoEvent(IStep step)
    {
        publisher.onNext(new UndoEvent(step));
    }

    /**
     * This crap should be moved to another place
     *
     * @return
     */
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
        if (steps.size() != 0)
            result.setPercent(steps.get(steps.size() - 1).getPercent());
        else
            result.setPercent(0f);

        result.setPoints(getTotalPoints());
        result.setComment(getComment());

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
        // Prevent adding points to a finished exercise
        if (isFinished())
            return null;

        IStep step = new Step();
        step.setPoints(points);
        steps.add(step);
        step.setPercent(calculateStepPercent(points));

        ExerciseEvent e = new NewShotEvent(step);
        publisher.onNext(e);

        // If the template has some limits
        // they might be reached, check it
        if (template.getLimit() != 0)
            if (template.getSuccesLimited())
            {
                // This is the case
                // when a player must pocket exact number of balls
                // and may miss any number of shots
                if (getSuccessfulShots() == template.getLimit())
                    setFinished(true);
            } else
            {
                if (getAttemptsCount() == template.getLimit())
                    setFinished(true);
            }

        return step;
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

    protected void setFinished(boolean value)
    {
        if (finished == value) // State is not changed
            return;
        else if (!finished && value) // State changes to finished
        {
            finished = value;
            publishFinishEvent();
        } else if (finished && !value) // State changes to non finished
        {
            finished = value;
            publishResumeEvent();
        }
    }

    private void publishResumeEvent()
    {
        publisher.onNext(new ResumeEvent());
    }

    /**
     * It returns count of shots that have points greate than zero
     * Such shots ara considered as successful
     *
     * @return
     */
    public int getSuccessfulShots()
    {
        return (int) steps.stream().filter(step -> step.getPoints() > 0).count();
    }

    /**
     * This function is called after the new step added in the list!!!
     * At this moment step count is correct but the step being added doesn't have correct percent
     *
     * @param stepPoints
     * @return
     */
    protected float calculateStepPercent(int stepPoints)
    {
        return 100f * getTotalPoints() / (getMaxPossiblePoints() * getAttemptsCount());
    }
}