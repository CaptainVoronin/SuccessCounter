package org.max.successcounter;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.max.successcounter.model.excercise.IExercise;
import org.max.successcounter.model.excercise.Result;

public class MinMaxValueFormatter extends ValueFormatter
{
    private final IExercise exercise;

    public MinMaxValueFormatter(IExercise exercise )
    {
        this.exercise = exercise;
    }

    @Override
    public String getPointLabel(Entry entry)
    {
        float index = entry.getX();
        float percent = entry.getY();
        if( exercise.isMinOrMax( ( int ) index, percent  ) )
            return Result.getPercentString( percent );
        else
            return "";
    }
}

