package org.max.successcounter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.Exercise;
import org.max.successcounter.model.ExerciseSet;

import java.sql.SQLException;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HistoryActivity extends AppCompatActivity
{
    public final static String EX_SET_ID = "EX_SET_ID";
    Dao<Exercise, Integer> exDao;
    Dao<ExerciseSet, Integer> exSetDao;
    Integer exSetId;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if( requestCode == ActivityIDs.EXERCISEACTIVITY_ID )
            if( resultCode == RESULT_OK )
            {
                try
                {
                    fillList();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
    }

    ExerciseSet exSet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DatabaseHelper db = new DatabaseHelper(this);
        try
        {
            exDao = db.getDao(Exercise.class);
            exSetDao = db.getDao(ExerciseSet.class);
            Intent in = getIntent();
            exSetId = in.getIntExtra(EX_SET_ID, -1);
            if (exSetId == -1)
                throw new IllegalArgumentException();

            fillList();
            setTitle( exSet.getName() );
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

    }

    private void fillList() throws SQLException
    {
        exSet = exSetDao.queryForId( exSetId );
        TableLayout table = findViewById( R.id.historyTable );
        table.removeAllViews();

        for( Exercise ex : exSet.getExercisesAsList() )
            table.addView( makeRow( ex, isItLastExercise( ex, exSet.getExercisesAsList() ) ) );
    }

    private View makeRow(Exercise ex, boolean isLast)
    {
        TableRow tr = (TableRow) getLayoutInflater().inflate( R.layout.historyrow, null );
        TextView tv = tr.findViewById( R.id.lbDate );
        tv.setText( Exercise.getFormattedDate( ex ) );
        tv.setOnLongClickListener( new ExLongClickListener( ex.getId() ));

        if( isLast )
            tv.setOnClickListener( new ExClickListener( ex.getId() ) );

        tv = tr.findViewById( R.id.lbPercent );
        tv.setText( Exercise.getPercentString( ex ) );
        tv.setOnLongClickListener( new ExLongClickListener( ex.getId() ));

        if( isLast )
            tv.setOnClickListener( new ExClickListener( ex.getId() ) );

        tv = tr.findViewById( R.id.lbCount );
        tv.setText( "" + ex.getSuccessCount() + "(" + ex.getAttemptsCount() + ")" );
        tv.setOnLongClickListener( new ExLongClickListener( ex.getId() ));

        if( isLast )
            tv.setOnClickListener( new ExClickListener( ex.getId() ) );


        return tr;
    }

    private void deleteEx(Object tag)
    {
        Exercise ex = (Exercise) tag;
        AlertDialog.Builder builder = new AlertDialog.Builder(HistoryActivity.this);
        builder.setTitle("Удаление");
        builder.setMessage("Подтвердите удаление");
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
                try
                {
                    exDao.delete(ex);
                    setResult(RESULT_OK);
                    fillList();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("Нет", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    boolean showPopup(View v, long id)
    {
        PopupMenu popup = new PopupMenu(HistoryActivity.this, v);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.ex_popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.idMenuDeleteExSet:
                        deleteEx(v.getTag());
                        return true;
                    default:
                        return false;
                }
            }

        });

        popup.show();
        return false;
    }

    boolean isItLastExercise(Exercise ex, List<Exercise> items)
    {
        long date = ex.getDate().getTime();

        for (Exercise item : items)
            if (item.getDate().getTime() > date)
                return false;

        return true;
    }

    class ExLongClickListener implements View.OnLongClickListener
    {
        private final long id;

        public ExLongClickListener( long id )
        {
            this.id = id;
        }

        @Override
        public boolean onLongClick(View v)
        {
            return showPopup(v, id);
        }
    }

    class ExClickListener implements View.OnClickListener{

        private final Integer id;

        public ExClickListener(Integer id )
        {
            this.id = id;
        }

        @Override
        public void onClick(View v)
        {
            Intent in = new Intent( HistoryActivity.this, ExerciseActivity.class );
            in.putExtra( ExerciseActivity.exerciseID, id );
            in.putExtra( ExerciseDynamicActivity.EX_SET_ID, exSet.getId() );
            startActivityForResult( in, ActivityIDs.EXERCISEACTIVITY_ID );

            // TODO : Обновлять после OK
        }
    }
}
