package org.max.successcounter;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ViewSwitcher;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.max.successcounter.model.excercise.BaseExercise;
import org.max.successcounter.model.excercise.IExercise;
import org.max.successcounter.model.excercise.IExerciseEvent;
import org.max.successcounter.model.excercise.NewShotEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SeriesExerciseActivity extends AExerciseActivity<BaseExercise>
{
    public static final int[] CHART_COLORS = {
            Color.rgb(255, 255, 255), Color.rgb(0, 0x1C, 0x2B)};

    ViewSwitcher switcher;
    int currentViewID;
    PieChart mChart;

    AtomicInteger limitIndicatorValue;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getExercise().getTemplate().getLimit() != 0)
        {
            prepareLimitIndicator();
        }
        setScreenProportions(0.2f, 0.2f);
    }

    protected void prepareLimitIndicator()
    {
        limitIndicatorValue = new AtomicInteger(0);

        if (getExercise().getTemplate().getSuccesLimited())
        {
            getExercise().getPublisher().filter(event -> ((event.getType() == IExerciseEvent.Type.ShotAdded)))
                    .filter(event -> ((NewShotEvent) event).getStep().getPoints() > 0)
                    .subscribe(event -> setLimitIndicator(limitIndicatorValue.incrementAndGet()));

            getExercise().getPublisher().filter(event -> ((event.getType() == IExerciseEvent.Type.Undo)))
                    .filter(event -> ((NewShotEvent) event).getStep().getPoints() > 0)
                    .subscribe(event -> setLimitIndicator(limitIndicatorValue.decrementAndGet()));
        } else
        {
            getExercise().getPublisher().filter(event -> ((event.getType() == IExerciseEvent.Type.ShotAdded)))
                    .subscribe(event -> setLimitIndicator(limitIndicatorValue.incrementAndGet()));

            getExercise().getPublisher().filter(event -> ((event.getType() == IExerciseEvent.Type.Undo)))
                    .subscribe(event -> setLimitIndicator(limitIndicatorValue.decrementAndGet()));
        }
    }

    private Object setLimitIndicator(int decrementAndGet)
    {
        return null;
    }

    @Override
    public void onExerciseFinished()
    {
        switcher.showNext();
        currentViewID = switcher.getCurrentView().getId();
    }

    @Override
    protected void prepareControlButtons(LinearLayout placeholder)
    {
        LayoutInflater lif = getLayoutInflater();
        LinearLayout ll = (LinearLayout) lif.inflate(R.layout.run_to_exercise_buttons, placeholder, true);
        switcher = ll.findViewById(R.id.btnSwitcher);

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
    protected void prepareChart(LinearLayout placeholder)
    {
        LayoutInflater lif = getLayoutInflater();
        LinearLayout ll = (LinearLayout) lif.inflate(R.layout.pie_chart, placeholder, true);

        mChart = ll.findViewById(R.id.chartHolder);
        mChart.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        mChart.getDescription().setEnabled(false);
        mChart.setDrawHoleEnabled(false);
        mChart.setRotationAngle(-90);
        mChart.setDrawEntryLabels(false);
        mChart.setBackgroundColor(Color.rgb(0, 0x1C, 0x2B));
        mChart.getLegend().setEnabled(false);
        mChart.setDescription(null);

    }

    @Override
    protected void updateChart()
    {
        Float percent1 = 0f;
        Float percent2 = 0f;

        IExercise ex = getExercise();

        if (!ex.getTemplate().getSuccesLimited())
        {
            percent1 = 100f * ex.getAttemptsCount() / ex.getTemplate().getLimit();
            percent2 = 100 - percent1;
        } else
        {
            percent1 = 100f * ex.getTotalPoints() / ex.getTemplate().getLimit();
            percent2 = 100 - percent1;
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(percent1));
        entries.add(new PieEntry(percent2));
        PieDataSet dataSet = new PieDataSet(entries, "%");
        dataSet.setColors(CHART_COLORS);
        dataSet.setValueFormatter(new BlankValueFormatter());
        PieData data = new PieData(dataSet);
        mChart.setData(data);
        mChart.invalidate();
    }

    @Override
    void undo()
    {
        super.undo();

        if (currentViewID == R.id.viewFinishMessage)
        {
            switcher.showNext();
            currentViewID = switcher.getCurrentView().getId();
        }
    }

    class BlankValueFormatter extends ValueFormatter
    {
        @Override
        public String getFormattedValue(float value)
        {
            return "";
        }

        @Override
        public String getPieLabel(float value, PieEntry pieEntry)
        {
            return "";
        }
    }
}
