package org.max.successcounter;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ExerciseDynamicActivity extends AppCompatActivity
{
    public final static String EX_SET_ID = "EX_SET_ID";
    Integer exSetId;
    LineChart mChart;
    Dao<ExerciseSet, Integer> exsetDao;
    ExerciseSet exSet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_dynamic);

        DatabaseHelper db = new DatabaseHelper( this );
        try
        {
            Intent in = getIntent();
            exSetId = in.getIntExtra( EX_SET_ID, -1 );
            if( exSetId == -1 )
                throw new IllegalArgumentException();

            exsetDao = db.getDao( ExerciseSet.class );
            exSet = exsetDao.queryForId( exSetId );
            setTitle( exSet.getName() );

            TextView tv = findViewById( R.id.lbNew );
            tv.setOnClickListener( e->{gotoExercise();} );

            tv = findViewById( R.id.lbHistory );
            tv.setOnClickListener( e->{gotoHistory();} );

            makeChart( );
            fillStats();
            setTitle( exSet.getName() );

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void fillStats()
    {
        TextView tv = findViewById( R.id.lbTotalExercises );
        int count = 0;

        tv.setText( "" + exSet.getExercises().size() );

        for( Exercise ex : exSet.getExercisesAsList() )
            count += ex.getSuccessCount();

        tv = findViewById( R.id.lbTotalSuccess );
        tv.setText( "" + count );

        for( Exercise ex : exSet.getExercisesAsList() )
            count += ex.getAttemptsCount();

        tv = findViewById( R.id.lbTotalShots );
        tv.setText( "" + count );
    }

    private void gotoHistory()
    {
        Intent in = new Intent( this, HistoryActivity.class );
        in.putExtra( EX_SET_ID, exSetId );
        startActivityForResult( in, ActivityIDs.HISTORYACTIVITY_ID );
    }

    private void makeChart() throws SQLException
    {
        prepareChart();
        fillChart( );
    }

    private void fillChart( ) throws SQLException
    {
        List<Exercise> items = new ArrayList<>();
        exSet = exsetDao.queryForId( exSetId );

        items.addAll( exSet.getExercises() );
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
        Intent in = new Intent( this, ExerciseActivity.class );
        in.putExtra( EX_SET_ID, exSetId );
        startActivityForResult( in, ActivityIDs.EXERCISEDYNAMICACTIVITY_ID );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if( requestCode == ActivityIDs.EXERCISEDYNAMICACTIVITY_ID ||
            requestCode == ActivityIDs.HISTORYACTIVITY_ID )
            if( resultCode == RESULT_OK )
            {
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
}
