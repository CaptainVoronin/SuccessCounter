package org.max.successcounter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class HistoryActivity extends AppCompatActivity
{
    public final static String TEMPLATE_ID = "TEMPLATE_ID";
    Dao<Template, Integer> templateDao;
    Dao<Result, Integer> resultDao;
    Integer templateId;

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

    Template template;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        makeToolbar();

        DatabaseHelper db = new DatabaseHelper(this);
        try
        {
            Dao<Result, Integer> resultDao = db.getDao(Result.class);

            templateDao = db.getDao(Template.class);
            Intent in = getIntent();
            templateId = in.getIntExtra(TEMPLATE_ID, -1);
            if (templateId == -1)
                throw new IllegalArgumentException();


            fillList();
            setTitle( template.getName() );

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void fillList() throws SQLException
    {
        template = templateDao.queryForId(templateId);
        TableLayout table = findViewById( R.id.historyTable );
        table.removeAllViews();

        for( Result res : template.getResults() )
            table.addView( makeRow( res, isItLastExercise( res, template.getExercisesAsList() ) ) );
    }

    private View makeRow(Result res, boolean isLast)
    {
        TableRow tr = (TableRow) getLayoutInflater().inflate( R.layout.historyrow, null );
        TextView tv = tr.findViewById( R.id.lbDate );
        tv.setText( Result.getFormattedDate( res ) );
        tv.setOnLongClickListener( new ExLongClickListener( res.getId() ));

        if( isLast )
            tv.setOnClickListener( new ExClickListener( res.getId() ) );

        tv = tr.findViewById( R.id.lbPercent );
        tv.setText( Result.getPercentString( res ) );
        tv.setOnLongClickListener( new ExLongClickListener( res.getId() ));

        if( isLast )
            tv.setOnClickListener( new ExClickListener( res.getId() ) );

        tv = tr.findViewById( R.id.lbCount );
        tv.setText( "" + res.getPoints() + "(" + res.getShots() + ")" );
        tv.setOnLongClickListener( new ExLongClickListener( res.getId() ));

        if( isLast )
            tv.setOnClickListener( new ExClickListener( res.getId() ) );

        return tr;
    }

    private void deleteResult(Object tag)
    {
        Result res = (Result) tag;
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
                    resultDao.delete(res);
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
                        deleteResult(v.getTag());
                        return true;
                    default:
                        return false;
                }
            }

        });

        popup.show();
        return false;
    }

    boolean isItLastExercise(Result res, List<Result> items)
    {
        long date = res.getDate().getTime();

        for (Result item : items)
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
            Intent in = new Intent( HistoryActivity.this, SimpleExerciseActivity.class );
            in.putExtra( AExerciseActivity.RESULT_ID, id );
            in.putExtra( ExerciseProgressActivity.TEMPLATE_ID, template.getId() );
            startActivityForResult( in, ActivityIDs.EXERCISEACTIVITY_ID );
        }
    }

    public void makeToolbar()
    {
        TextView tv = findViewById( R.id.tvTitle );
        tv.setText( R.string.msgHistoryActivityTitle );
    }
}
