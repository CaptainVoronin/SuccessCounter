package org.max.successcounter;

import com.github.mikephil.charting.formatter.ValueFormatter;

public class EmptyValueFormatter extends ValueFormatter
{
    @Override
    public String getFormattedValue(float value)
    {
        return "";
    }

}
