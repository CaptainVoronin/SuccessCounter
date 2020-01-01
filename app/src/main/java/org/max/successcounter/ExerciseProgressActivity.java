package org.max.successcounter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Template;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class ExerciseProgressActivity extends AppCompatActivity
{
    public final static String TEMPLATE_ID = "TEMPLATE_ID";
    Integer templateId;
    LineChart mChart;
    Dao<Template, Integer> exsetDao;
    Template template;
    SharedPreferences mPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_progress);
        mPrefs = getPreferences( Context.MODE_PRIVATE );
        DatabaseHelper db = new DatabaseHelper( this );
        try
        {
            Intent in = getIntent();
            templateId = in.getIntExtra(TEMPLATE_ID, -1 );
            if( templateId == -1 )
            {
                templateId  = savedInstanceState.getInt( TEMPLATE_ID );
                templateId = mPrefs.getInt( TEMPLATE_ID , -1);
                if( templateId == -1 )
                    throw new IllegalArgumentException();
            }

            exsetDao = db.getDao( Template.class );
            template = exsetDao.queryForId(templateId);

            setTitle( template.getName() );

            TextView tv = findViewById( R.id.lbNew );
            tv.setOnClickListener( e->{gotoExercise();} );

            tv = findViewById( R.id.lbHistory );
            tv.setOnClickListener( e->{gotoHistory();} );

            makeChart( );
            fillStats();
            setTitle( template.getName() );

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void fillStats()
    {
        TextView tv = findViewById( R.id.lbTotalExercises );
        int count = 0;

        tv.setText( "" + template.getResults().size() );

        for( Result res : template.getResults() )
            count += res.getPoints();

        tv = findViewById( R.id.lbTotalSuccess );
        tv.setText( "" + count );

        for( Result res : template.getResults() )
            count += res.getShots();

        tv = findViewById( R.id.lbTotalShots );
        tv.setText( "" + count );
    }

    private void gotoHistory()
    {
        Intent in = new Intent( this, HistoryActivity.class );
        in.putExtra(TEMPLATE_ID, templateId);
        startActivityForResult( in, ActivityIDs.HISTORYACTIVITY_ID );
    }

    private void makeChart() throws SQLException
    {
        prepareChart();
        fillChart( );
    }

    private void fillChart( ) throws SQLException
    {
        List<Result> items = new ArrayList<>();
        template = exsetDao.queryForId(templateId);

        items.addAll( template.getResults() );
        List<Entry> exes = new ArrayList<>();
        for( int i = 0; i < items.size(); i++  )
            exes.add( new Entry( (float) i, new Float( items.get(i).getPercent() ) ) );

        mChart.clear();
        LineData data = new LineData();
        int color = Color.rgb(0xDD, 0x88, 0x00);
        LineDataSet set= new LineDataSet( exes, "%" );
        set.setDrawCircleHole( false );
        set.setDrawCircles( false );
        set.setColor(color);
        data.setDrawValues( false );
        data.addDataSet( set );
        data.setValueTextSize( 10 );
        mChart.setData(data);
        mChart.invalidate();
    }

    private void prepareChart()
    {
        int axisColor = Color.LTGRAY;

        mChart = findViewById( R.id.chartHolder );
        YAxis y = mChart.getAxisLeft();
        y.setAxisMinimum( 0f );
        y.setAxisMaximum( 100f );
        y.setTextColor( axisColor  );
        y.setGridColor( axisColor  );
        y.setAxisLineColor( axisColor  );

        y = mChart.getAxisRight();
        y.setAxisMinimum( 0f );
        y.setAxisMaximum( 100f );
        y.setTextColor( axisColor  );
        y.setGridColor( axisColor  );
        y.setAxisLineColor( axisColor  );

        XAxis x = mChart.getXAxis();
        x.setGridColor( axisColor  );
        x.setAxisLineColor( axisColor  );
        x.setTextColor( axisColor  );
    }

    private void gotoExercise()
    {
        Intent in;

        if( !template.getCompound()  )
        {
            if( template.getLimited() )
                in = new Intent( this, RunToExerciseActivity.class );
            else
                in = new Intent( this, SimpleExerciseActivity.class );
        }
        else
            in = new Intent( this, CompoundExerciseActivity.class );

        in.putExtra(TEMPLATE_ID, template.getId());
        startActivityForResult( in, ActivityIDs.EXERCISE_PROGRESS_ACTIVITY_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == ActivityIDs.EXERCISE_PROGRESS_ACTIVITY_ID ||
            requestCode == ActivityIDs.HISTORYACTIVITY_ID )
            if( resultCode == RESULT_OK )
            {
                setResult( RESULT_OK );
                try
                {
                    fillChart();
                    fillStats();

                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        SharedPreferences.Editor ed =  mPrefs.edit();
        ed.putInt("templateID", templateId);
        ed.commit();
    }
}