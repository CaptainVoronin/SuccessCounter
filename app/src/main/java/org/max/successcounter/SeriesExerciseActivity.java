package org.max.successcounter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.LinearLayout;

import org.max.successcounter.model.excercise.SeriesExercise;
import org.max.successcounter.model.excercise.SimpleExercise;

public class SeriesExerciseActivity extends AExerciseActivity<SeriesExercise>
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_series_exercise);
    }

    @Override
    protected void prepareChart(LinearLayout placeholder)
    {

    }

    @Override
    protected void prepareControlButtons(LinearLayout placeholder)
    {

    }

    @Override
    protected void updateChart()
    {

    }

    @Override
    public void onExerciseFinished()
    {

    }
}
