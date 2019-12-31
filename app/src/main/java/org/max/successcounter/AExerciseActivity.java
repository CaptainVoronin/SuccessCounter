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
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.HistoryOperator;
import org.max.successcounter.model.excercise.ExerciseFactory;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Template;
import org.max.successcounter.model.excercise.IExercise;
import org.max.successcounter.model.excercise.IStep;

import java.sql.SQLException;
import java.util.List;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public abstract class AExerciseActivity<T> extends AppCompatActivity implements IExerciseForm<T>
{
    public final static String RESULT_ID = "RESULT_ID";

    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private DatabaseHelper db;
    private TextView lbPercent;
    private TextView lbAttempts;
    private LineChart mChart;
    private ImageButton btnRollback;
    private View mContentView;
    private IExercise exercise;
    private Dao<Result, Integer> daoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView( getViewID() );

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mVisible = true;

        mContentView = findViewById(R.id.fullscreen_content);

        mContentView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                toggle();
            }
        });

        db = new DatabaseHelper(this);

        try
        {
            prepareControls();
            prepareObjects();
            setTitle( getExercise().getName() );

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
            updateUIResults();
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

    protected void addStep(int points)
    {
        getExercise().addStepByPoints( points );
        saveResult( );
        updateUIResults();
        if( getExercise().isFinished() )
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
            Result res = exercise.getResult();
            daoResult.createOrUpdate( res );
            HistoryOperator.instance.saveStep( getExercise().getLastStep() );

            btnRollback.setEnabled(getExercise().getSteps().size() != 0);
            setResult(RESULT_OK);
        } catch ( Exception e)
        {
            e.printStackTrace();
        }
    }

    void updateUIResults()
    {
        IStep step = getExercise().getLastStep();
        if( step != null )
            lbPercent.setText( step.getPercent() + "%");
        else
            lbPercent.setText( "0.0%");

        lbAttempts.setText("" + getExercise().getSuccessCount() + "(" + getExercise().getAttemptsCount() + ")");
        drawCharts();
    }

    void prepareObjects() throws SQLException
    {

        Integer templateID = getIntent().getIntExtra( ExerciseProgressActivity.TEMPLATE_ID, -1);

        if (templateID == -1)
            throw new IllegalArgumentException("Exercise result id is missing");

        // Load the execercise template
        Template et = getTemplate( templateID );

        // Make an exercise instance
        IExercise ex = ExerciseFactory.instance.makeExercise( et );
        setExerсise( ex );
        HistoryOperator.instance.init( getDb(), getExercise() );

        // Check if it is the continue of an exercise
        Integer id = getIntent().getIntExtra( AExerciseActivity.RESULT_ID, -1);
        daoResult = getDao( Result.class, getDb() );

        // Yes, it is the continue
        if( id != -1 )
        {
            List<IStep> steps = HistoryOperator.instance.getHistory();
            getExercise().setSteps( steps );
            updateUIResults();
        }
        else
        {
            // This is the new exercise,
            // so the the old history must be deleted
            HistoryOperator.instance.clearHistory();
        }
    }

    protected DatabaseHelper getDb()
    {
        return db;
    }

    void undo()
    {
        try
        {
            IStep step = getExercise().getLastStep();
            HistoryOperator.instance.deleteStep(step);
            getExercise().undo();
            btnRollback.setEnabled( getExercise().getSteps().size() != 0);
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public <T> Dao<T, Integer> getDao(Class<T> exerciseClass, DatabaseHelper db) throws SQLException
    {
        return db.getDao( exerciseClass );
    }

    @Override
    public void setExerсise( IExercise exercise )
    {
        this.exercise = exercise;
    }

    @Override
    public IExercise getExercise()
    {
        return exercise;
    }

    private void drawCharts()
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
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
        {
            actionBar.hide();
        }
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

    private Template getTemplate(Integer id ) throws SQLException
    {
        Dao<Template, Integer> d = getDb().getDao( Template.class );
        Template et = d.queryForId( id );
        return et;
    }
}
