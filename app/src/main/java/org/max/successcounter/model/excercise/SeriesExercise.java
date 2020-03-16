package org.max.successcounter.model.excercise;

/**
 * This is a class realises functionality for
 * an exercise with lasts to the first miss. The first miss terminates
 * the run
 */
public class SeriesExercise extends BaseExercise
{

    public SeriesExercise(Template template)
    {
        super(template);
    }

    @Override
    public IStep addNewShot(int points)
    {
        // Упражнение выполняется до первого промаха
        if (points == 0)
        {
            setFinished(true);
            return null;
        }

        return super.addNewShot(points);
    }

    @Override
    protected float calculateStepPercent(int stepPoints)
    {
        return 100f * getAttemptsCount() / template.getLimit();
    }
}
