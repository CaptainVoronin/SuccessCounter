package org.max.successcounter;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private ImageButton btnRollback;
    private View mContentView;
    private IExercise exercise;
    private Dao<Result, Integer> daoResult;
    private ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setContentView( R.layout.aexercise_layout );

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
            loadTemplate();
            prepareControls();
            loadHistory();
            setTitle( getExercise().getName() );
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        setResult(RESULT_CANCELED);
    }

    protected void prepareControls()
    {
        lbPercent = findViewById(R.id.lbPercent);
        lbAttempts = findViewById(R.id.lbAttempts);
        btnRollback = findViewById(R.id.btnRollback);
        btnRollback.setOnClickListener(e -> {
            undo();
            updateUIResults();
            saveResult();
        });
        btnRollback.setEnabled(false);

        btnBack = findViewById( R.id.btnBack );
        btnBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        LinearLayout ll = findViewById( R.id.viewChartPlaceholder );
        prepareChart( ll );

        ll = findViewById( R.id.buttonsPlaceholder );
        prepareControlButtons( ll );
    }

    protected abstract void prepareChart(LinearLayout placeholder);

    protected abstract void prepareControlButtons(LinearLayout placeholder);

    protected void addStep(int points)
    {
        getExercise().addStepByPoints( points );
        saveResult( );
        updateUIResults();
        if( getExercise().isFinished() )
        {
            onExerciseFinished();
        }
    }

    protected void saveResult()
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

    protected void updateUIResults()
    {
        lbPercent.setText( getEfficiencyString() );
        lbAttempts.setText( getAttemptsString() );
        updateChart();
    }

    protected void loadTemplate() throws SQLException
    {
        Integer templateID = getIntent().getIntExtra( ExerciseProgressActivity.TEMPLATE_ID, -1);

        if (templateID == -1)
            throw new IllegalArgumentException("Exercise result id is missing");

        // Load the execercise template
        Template et = getTemplate( templateID );

        // Make an exercise instance
        IExercise ex = ExerciseFactory.instance.makeExercise( et );
        ex.setTemplate( et );
        setExerсise( ex );
    }

    protected void loadHistory() throws SQLException
    {
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
        // TODO: Перестает работать отмена после того, как упражнение завершено
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

    protected abstract void updateChart();

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
        et.setMissOptionName( getString( R.string.missOptionName ) );
        et.setSuccessOptionName( getString( R.string.successOptionName ) );
        return et;
    }

    @Override
    public String getEfficiencyString()
    {
        String buff;

        IStep step = getExercise().getLastStep();
        float percent = step.getPercent();

        if( step != null )
            buff = Result.getPercentString( percent );
        else
            buff = "0.0%";

        return buff;
    }

    @Override
    public String getAttemptsString()
    {
        return "" + getExercise().getSuccessCount() + "(" + getExercise().getAttemptsCount() + ")";
    }

}