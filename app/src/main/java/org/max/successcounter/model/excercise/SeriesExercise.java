package org.max.successcounter.model.excercise;

public class SeriesExercise extends AExercise
{
    boolean finished;

    @Override
    public IStep addNewShot(int points)
    {
        // Prevent add points to a finished exercise
        if (isFinished())
            return null;

        // Упражнение с ограничением
        // выполняется до первого промаха
        if (points == 0)
        {
            finished = true;
            publishFinishEvent();
            return null;
        }

        return super.addNewShot(points);
    }

}
