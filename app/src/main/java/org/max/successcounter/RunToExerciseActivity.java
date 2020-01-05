package org.max.successcounter;

import android.graphics.Color;
import android.widget.ImageButton;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import org.max.successcounter.model.excercise.IExercise;
import org.max.successcounter.model.excercise.RunToExcercise;

import java.util.ArrayList;
import java.util.List;

public class RunToExerciseActivity extends AExerciseActivity<RunToExcercise>
{

    public static final int[] CHART_COLORS = {
            Color.rgb( 255, 255, 255 ), Color.rgb( 0, 0x1C, 0x2B ) };

    PieChart mChart;
    @Override
    public int getViewID()
    {
        return R.layout.activity_run_to_exercise;
    }

    @Override
    public void onExerciseFinished()
    {
        // TODO: implement
    }

    @Override
    protected void prepareControls()
    {
        super.prepareControls();

        ImageButton btn = findViewById(R.id.btnAttempt);

        btn.setOnClickListener(e -> {
            addStep( 0 );
        });

        btn = findViewById(R.id.btnSuccess);
        btn.setOnClickListener(e -> {
            addStep( 1 );
        });
    }

    @Override
    protected void prepareChart()
    {
        mChart = findViewById( R.id.chartHolder );
        mChart.getDescription().setEnabled(false);
        mChart.setDrawHoleEnabled(false);
        mChart.setRotationAngle(-90);
        mChart.setDrawEntryLabels(false);
        mChart.setBackgroundColor( Color.rgb( 0, 0x1C, 0x2B ) );
    }

    @Override
    protected void updateChart()
    {
        IExercise ex = getExercise();
        Float percent1 = 100f * ex.getSuccessCount() / ex.getTemplate().getLimit();
        Float percent2 = 100 - percent1;
        List<PieEntry> entries = new ArrayList<>();
        entries.add( new PieEntry( percent1 ) );
        entries.add( new PieEntry( percent2 ) );
        PieDataSet dataSet = new PieDataSet(entries, "%");
        dataSet.setColors(CHART_COLORS);
        dataSet.setValueFormatter( new BlankValueFormatter() );
        PieData data = new PieData( dataSet );
        mChart.setData( data );
        mChart.invalidate();
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
