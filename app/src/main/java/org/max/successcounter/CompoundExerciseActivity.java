package org.max.successcounter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.max.successcounter.model.excercise.CompoundExcercise;

public class CompoundExerciseActivity extends AExerciseActivity<CompoundExcercise>
{
    private LineChart mChart;

    @Override
    public int getViewID()
    {
        return R.layout.activity_compound_exercise;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
            LinearLayout ll = findViewById( R.id.llOptionsLayout );
            CompoundExcercise cx = ( CompoundExcercise ) getExercise();
            cx.getOptions().forEach( item->{
                ll.addView( makeButton( item ) );
            } );

        setResult(RESULT_CANCELED);
    }

    @Override
    protected void prepareChart()
    {
        int axisColor = Color.LTGRAY;

        mChart = findViewById(R.id.chartHolder);
        mChart.setDrawMarkers(false);
        mChart.setDrawGridBackground(false);
        YAxis y = mChart.getAxisLeft();
        y.setAxisMinimum(0f);
        y.setAxisMaximum(100f);
        y.setTextColor(axisColor);
        y.setGridColor(axisColor);
        y.setAxisLineColor(axisColor);

        y = mChart.getAxisRight();
        y.setAxisMinimum(0f);
        y.setAxisMaximum(100f);
        y.setTextColor(axisColor);
        y.setGridColor(axisColor);
        y.setAxisLineColor(axisColor);

        XAxis x = mChart.getXAxis();
        x.setGridColor(axisColor);
        x.setAxisLineColor(axisColor);
        x.setTextColor(axisColor);

        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

    }

    @Override
    protected void updateChart()
    {
        mChart.clear();
        LineData data = new LineData();
        int color = Color.rgb(0xDD, 0x88, 0x00);
        LineDataSet set = new LineDataSet(getExercise().getPercentHistory(), "%");
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setColor(color);
        set.setValueFormatter(new EmptyValueFormatter());
        data.setDrawValues(false);
        data.addDataSet(set);
        mChart.setData(data);
        mChart.invalidate();
    }

    @Override
    public void onExerciseFinished()
    {

    }

    public class OnOptionClick implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Object tag = v.getTag();
            CompoundExcercise.Option opt = (CompoundExcercise.Option ) tag;
            addStep( opt.getPoints() );
        }
    }

    Button makeButton( CompoundExcercise.Option option )
    {
        Button btn = new Button( this );
        btn.setText( option.getDescription() );
        btn.setTag( option );
        btn.setOnClickListener( new OnOptionClick() );
        return btn;
    }
}
