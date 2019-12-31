package org.max.successcounter;

import org.max.successcounter.model.excercise.SimpleExercise;

public class RunToExerciseActivity extends AExerciseActivity<SimpleExercise>
{
    @Override
    public int getViewID()
    {
        return R.layout.activity_exercise;
    }

    @Override
    public void onExerciseFinished()
    {

    }
}
