package org.max.successcounter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.Exercise;
import org.max.successcounter.model.ExerciseSet;
import org.max.successcounter.model.HistoryItem;
import org.max.successcounter.model.HistoryOperator;
import org.max.successcounter.model.excercise.IStep;
import org.max.successcounter.model.excercise.RunToExcercise;
import org.max.successcounter.model.excercise.SimpleExercise;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class ExerciseActivity extends AppCompatActivity
{

    public final static String exerciseID = "exerciseID";
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();

    private final Runnable mShowPart2Runnable = new Runnable()
    {
        @Override
        public void run()
        {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null)
            {
                actionBar.show();
            }
            //mControlsView.setVisibility(View.VISIBLE);
        }
    };


    //SimpleExercise sxr;
    RunToExcercise sxr;
    ExerciseSet exSet;
    Dao<Exercise, Integer> exDao;
    TextView lbPercent;
    TextView lbAttempts;
    LineChart mChart;
    ImageButton btnRollback;
    private View mContentView;

    private final Runnable mHidePart2Runnable = new Runnable()
    {
        @SuppressLint("InlinedApi")
        @Override
        public void run()
        {
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            hide();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_exercise);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVisible = true;
        //mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                toggle();
            }
        });

        try
        {
            prepareControls();
            prepareObjects();
            setTitle( exSet.getName() );

        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        setResult(RESULT_CANCELED);
    }

    private void prepareControls()
    {
        lbPercent = findViewById(R.id.lbPercent);
        lbAttempts = findViewById(R.id.lbAttempts);

        ImageButton btn = findViewById(R.id.btnAttempt);

        btn.setOnClickListener(e -> {
            addStep( 0 );
        });

        btn = findViewById(R.id.btnSuccess);
        btn.setOnClickListener(e -> {
            addStep( 1 );
        });

        btnRollback = findViewById(R.id.btnRollback);
        btnRollback.setOnClickListener(e -> {
            undo();
            updatePercent();
            saveResult();
        });

        btnRollback.setEnabled(false);

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

    private void addStep(int points)
    {
        sxr.addStepByPoints( points );
        saveResult( );
        updatePercent();
        if( sxr.isFinished() )
        {
            lockScreen();
        }
    }

    private void lockScreen()
    {

    }

    private void saveResult()
    {
        try
        {
/*
            current.setDate(Calendar.getInstance().getTime());
            if (current.getId() == null)
                exDao.create(current);
            else
                exDao.update(current);
*/

            btnRollback.setEnabled(sxr.getSteps().size() != 0);

            setResult(RESULT_OK);
        } catch ( Exception e)
        {
            e.printStackTrace();
        }
    }

    void updatePercent()
    {
        IStep step = sxr.getLastStep();
        if( step != null )
            lbPercent.setText( step.getPercent() + "%");
        else
            lbPercent.setText( "0.0%");

        lbAttempts.setText("" + sxr.getSuccessCount() + "(" + sxr.getAttemptsCount() + ")");
        drawCharts();
    }

    void prepareObjects() throws SQLException
    {
        DatabaseHelper db = new DatabaseHelper(this);

        // initialize the history operator
        HistoryOperator.instance.createDAO( db );

        Integer setId = getIntent().getIntExtra(HistoryActivity.EX_SET_ID, -1);

        if (setId == -1)
            throw new IllegalArgumentException("Exercise set id is missing");

        Dao<ExerciseSet, Integer> exSetDao = db.getDao(ExerciseSet.class);

        // Get the exercise set by its id
        exSet = exSetDao.queryForId(setId);

        exDao = db.getDao(Exercise.class);
        //sxr = new SimpleExercise();
        sxr = new RunToExcercise( 15 );


        // Check, if exercise already exists
     /*   Integer id = getIntent().getIntExtra(exerciseID, -1);

        if (id == -1)
        {
            current = new Exercise();
            current.setParent(exSet);

        } else
        {
            current = findExById(exSet, id);
            if (current == null)
                throw new IllegalArgumentException("Exercise not found. id " + id);
            updatePercent();
        } */
    }

    private Exercise findExById(ExerciseSet exSet, Integer id)
    {
        for (Exercise ex : exSet.getExercisesAsList())
            if (ex.getId() == id)
                return ex;

        return null;
    }

    void undo()
    {
        sxr.undo();
        btnRollback.setEnabled( sxr.getSteps().size() != 0);
    }

    private void drawCharts()
    {
        mChart.clear();
        LineData data = new LineData();
        int color = Color.rgb(0xDD, 0x88, 0x00);
        LineDataSet set = new LineDataSet(sxr.getPercentHistory(), "%");
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
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
        delayedHide(100);
    }

    private void toggle()
    {
        if (mVisible)
        {
            hide();
        } else
        {
            show();
        }
    }

    private void hide()
    {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
//        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show()
    {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private void delayedHide(int delayMillis)
    {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private class EmptyValueFormatter extends ValueFormatter
    {
        @Override
        public String getFormattedValue(float value)
        {
            return "";
        }
    }
}
