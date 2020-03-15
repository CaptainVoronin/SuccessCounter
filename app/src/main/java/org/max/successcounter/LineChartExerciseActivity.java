package org.max.successcounter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.max.successcounter.model.excercise.BaseExercise;

public class LineChartExerciseActivity<T extends BaseExercise> extends AExerciseActivity<T>
{
    private LineChart mChart;
    MinMaxValueFormatter formatter;

    @Override
    protected void prepareChart(LinearLayout placeholder)
    {
        LayoutInflater lif = getLayoutInflater();
        lif.inflate( R.layout.line_chart, placeholder, true );

        int axisColor = Color.LTGRAY;
        mChart = placeholder.findViewById(R.id.chartHolder);
        mChart.setDrawMarkers(false);
        mChart.setDrawGridBackground(false);
        mChart.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT) );
        mChart.setNoDataText( getString( R.string.no_data_text ) );
        mChart.setNoDataTextColor(getColor(R.color.red_worm15));
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
        formatter = new MinMaxValueFormatter( getExercise() );
    }

    @Override
    protected void prepareControlButtons(LinearLayout placeholder)
    {

        LayoutInflater lif = getLayoutInflater();
        LinearLayout ll = (LinearLayout) lif.inflate(R.layout.run_to_exercise_buttons, placeholder, true);

        ImageButton btn = placeholder.findViewById(R.id.btnAttempt);

        btn.setOnClickListener(e -> {
            addNewShot(0);
        });

        btn = findViewById(R.id.btnSuccess);
        btn.setOnClickListener(e -> {
            addNewShot(1);
        });
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
        set.setValueTextColor( getColor( android.R.color.white ));
        set.setValueFormatter(new EmptyValueFormatter());
        data.setDrawValues(false);
        data.addDataSet(set);
        data.setValueFormatter( formatter );
        data.setValueTextSize(12);
        mChart.setData(data);
        mChart.invalidate();
    }
}