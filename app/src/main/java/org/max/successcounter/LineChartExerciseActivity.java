package org.max.successcounter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.max.successcounter.model.excercise.BaseExercise;
import org.max.successcounter.model.excercise.IExercise;
import org.max.successcounter.model.excercise.IExerciseEvent;
import org.max.successcounter.model.excercise.NewShotEvent;
import org.max.successcounter.model.excercise.UndoEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class LineChartExerciseActivity<T extends BaseExercise> extends AExerciseActivity<T>
{
    MinMaxValueFormatter formatter;
    PieChart mIndicatorChart;
    AtomicInteger limitIndicatorValue;
    private LineChart mChart;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getExercise().getTemplate().getLimit() != 0)
        {
            prepareLimitIndicator();
        }
        //setScreenProportions(0.2f, 0.2f);
    }

    protected void prepareLimitIndicator()
    {
        limitIndicatorValue = new AtomicInteger(0);
        prepareIndicatorChart();


        if (getExercise().getTemplate().getSuccesLimited())
        {
            getExercise().getPublisher().filter(event -> ((event.getType() == IExerciseEvent.Type.ShotAdded)))
                    .filter(event -> ((NewShotEvent) event).getStep().getPoints() > 0)
                    .subscribe(event -> setLimitIndicator(limitIndicatorValue.incrementAndGet()));

            getExercise().getPublisher().filter(event -> ((event.getType() == IExerciseEvent.Type.Undo)))
                    .filter(event -> ((UndoEvent) event).getStep().getPoints() > 0)
                    .subscribe(event -> setLimitIndicator(limitIndicatorValue.decrementAndGet()));
        } else
        {
            getExercise().getPublisher().filter(event -> ((event.getType() == IExerciseEvent.Type.ShotAdded)))
                    .subscribe(event -> setLimitIndicator(limitIndicatorValue.incrementAndGet()));

            getExercise().getPublisher().filter(event -> ((event.getType() == IExerciseEvent.Type.Undo)))
                    .subscribe(event -> setLimitIndicator(limitIndicatorValue.decrementAndGet()));
        }
    }

    private void prepareIndicatorChart()
    {
        mIndicatorChart = findViewById(R.id.indicatorChartHolder);
        mIndicatorChart.getDescription().setEnabled(false);
        mIndicatorChart.setDrawHoleEnabled(false);
        mIndicatorChart.setNoDataText("");
        mIndicatorChart.setRotationAngle(-90);
        mIndicatorChart.setDrawEntryLabels(false);
        mIndicatorChart.setBackgroundColor(Color.rgb(0, 0x1C, 0x2B));
        mIndicatorChart.getLegend().setEnabled(false);
        mIndicatorChart.setDescription(null);
    }

    private Object setLimitIndicator(int value)
    {
        final int[] CHART_COLORS = {
                getColor(R.color.blue_worm13), Color.rgb(0, 0x1C, 0x2B)};
        //

        IExercise ex = getExercise();
        int percent1 = value;
        int percent2 = ex.getTemplate().getLimit() - value;

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(percent1));
        entries.add(new PieEntry(percent2));
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(CHART_COLORS);
        dataSet.setValueFormatter(new RunToExerciseActivity.BlankValueFormatter());
        PieData data = new PieData(dataSet);
        mIndicatorChart.setData(data);
        mIndicatorChart.invalidate();
        return null;
    }


    @Override
    protected void prepareChart(LinearLayout placeholder)
    {
        LayoutInflater lif = getLayoutInflater();
        lif.inflate(R.layout.line_chart, placeholder, true);

        int axisColor = Color.LTGRAY;
        mChart = placeholder.findViewById(R.id.chartHolder);
        mChart.setDrawMarkers(false);
        mChart.setDrawGridBackground(false);
        mChart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mChart.setNoDataText(getString(R.string.no_data_text));
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
        formatter = new MinMaxValueFormatter(getExercise());
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
        set.setValueTextColor(getColor(android.R.color.white));
        set.setValueFormatter(new EmptyValueFormatter());
        data.setDrawValues(false);
        data.addDataSet(set);
        data.setValueFormatter(formatter);
        data.setValueTextSize(12);
        mChart.setData(data);
        mChart.invalidate();
    }
}