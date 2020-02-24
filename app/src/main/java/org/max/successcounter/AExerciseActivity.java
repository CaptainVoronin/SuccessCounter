package org.max.successcounter;

import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;

public abstract class AExerciseActivity<T> extends AppCompatActivity implements IExerciseForm<T>
{
    public final static String RESULT_ID = "RESULT_ID";

    private DatabaseHelper db;
    private TextView lbPercent;
    private TextView lbAttempts;
    private TextView lbExName;

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

        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

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
        lbExName = findViewById(R.id.tvExName);

        lbExName.setText( getExercise().getName()  );
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
            List<IStep> steps = getExercise().getSteps();
            HistoryOperator.instance.saveStep( steps.get( steps.size() - 1 ) );

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
        Integer templateID = getIntent().getIntExtra( ProgressActivity.TEMPLATE_ID, -1);

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
        try
        {
            IStep step = getExercise().undo();
            if( step != null )
                HistoryOperator.instance.deleteStep(step);

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
        List<IStep> steps = getExercise().getSteps();

        if( steps.size() != 0 )
            buff = Result.getPercentString( steps.get( steps.size() - 1 ).getPercent() );
        else
            buff = "0.0%";

        return buff;
    }

    @Override
    public String getAttemptsString()
    {
        return "" + getExercise().getTotalPoints() + "(" + getExercise().getAttemptsCount() + ")";
    }

}