package org.max.successcounter;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import org.max.successcounter.model.excercise.SeriesExercise;

public class SeriesExerciseActivity extends AExerciseActivity<SeriesExercise>
{
    ViewSwitcher switcher;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void prepareChart(LinearLayout placeholder)
    {

    }

    @Override
    protected void prepareControlButtons(LinearLayout placeholder)
    {
        LayoutInflater lif = getLayoutInflater();
        LinearLayout ll = (LinearLayout) lif.inflate(R.layout.run_to_exercise_buttons, placeholder, true);
        switcher = ll.findViewById(R.id.btnSwitcher);

        ImageButton btn = placeholder.findViewById(R.id.btnAttempt);
        btn.setOnClickListener(e -> {
            addStep(0);
        });

        btn = findViewById(R.id.btnSuccess);
        btn.setOnClickListener(e -> {
            addStep(1);
        });

    }

    @Override
    protected void updateChart()
    {

    }

    @Override
    public void onExerciseFinished()
    {
        switcher.showNext();
        switcher.getCurrentView().getId();
    }

    @Override
    public String getEfficiencyString()
    {
        String buff;

        buff = "" + getExercise().getAttemptsCount();

        return buff;
    }

    @Override
    public String getAttemptsString()
    {
        return "";
    }

}
