package org.max.successcounter;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.ResultDateComparator;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.ActionMode;
import androidx.appcompat.widget.Toolbar;

// TODO: Таблица истории не в дугу
// TODO: Нужна сортировка таблицы

public class HistoryActivity extends AppCompatActivity
{
    public final static String TEMPLATE_ID = "TEMPLATE_ID";
    Dao<Template, Integer> templateDao;
    Dao<Result, Integer> resultDao;
    Integer templateId;
    List<CheckBox> checks;
    Template template;
    Toolbar toolbar;
    private MenuItem deleteActionMenuItem;
    private ActionMode actionMode;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == ActivityIDs.EXERCISEACTIVITY_ID)
            if (resultCode == RESULT_OK)
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

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        makeToolbar();
        DatabaseHelper db = new DatabaseHelper(this);

        try
        {
            resultDao = db.getDao(Result.class);
            templateDao = db.getDao(Template.class);
            Intent in = getIntent();
            templateId = in.getIntExtra(TEMPLATE_ID, -1);
            if (templateId == -1)
                throw new IllegalArgumentException();

            fillList();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void fillList() throws SQLException
    {
        template = templateDao.queryForId(templateId);

        TableLayout table = findViewById(R.id.historyTable);
        table.removeAllViews();
        checks = new ArrayList<>();
        List<Result> results = template.getExercisesAsList();
        results.sort(new ResultDateComparator(true));
        boolean isLast = true;
        for (Result res : results)
        {
            table.addView(makeRow(res, isLast));
            isLast = false;
        }
    }

    private View makeRow(Result res, boolean isLast)
    {
        TextView tvDate;
        TextView tvPercent;
        TextView tvCount;

        TableRow tr = (TableRow) getLayoutInflater().inflate(R.layout.historyrow, null);

        CheckBox chb = tr.findViewById(R.id.chbSelected);
        checks.add(chb);
        chb.setVisibility(actionMode != null ? View.VISIBLE : View.INVISIBLE);
        chb.setTag(res);
        chb.setOnCheckedChangeListener((btn, isChecked) -> setDeleteActionState());

        tvDate = tr.findViewById(R.id.lbDate);
        String buff = Result.getFormattedDate(res);
        tvDate.setText(buff);
        tvDate.setOnLongClickListener((View v) -> this.startActionMode());

        tvPercent = tr.findViewById(R.id.lbPercent);
        tvPercent.setText(Result.getPercentString(res));
        tvPercent.setOnLongClickListener((View v) -> this.startActionMode());

        tvCount = tr.findViewById(R.id.lbCount);
        tvCount.setText("" + res.getPoints() + "(" + res.getShots() + ")");
        tvCount.setOnLongClickListener((View v) -> this.startActionMode());

        if (isLast)
        {
            ExClickListener listener = new ExClickListener(res.getId() );
            tvDate.setOnClickListener(listener);
            tvPercent.setOnClickListener(listener);
            tvCount.setOnClickListener(listener);
            tvDate.setTypeface(null, Typeface.BOLD);
            tvPercent.setTypeface(null, Typeface.BOLD);
            tvCount.setTypeface(null, Typeface.BOLD);
        }

        return tr;
    }

    private void setDeleteActionState()
    {
        long count = checks.stream().filter(CheckBox::isChecked).count();
        deleteActionMenuItem.setEnabled( count != 0 );
    }

    private boolean startActionMode()
    {
        checks.stream().forEach(item -> item.setVisibility(View.VISIBLE));
        actionMode = startSupportActionMode( new ActionModeCallback() );
        return false;
    }

    private void beginDeleteResults() throws SQLException
    {
        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setMessage( R.string.txtDeleteResults ).
        setPositiveButton( android.R.string.ok, ( DialogInterface dialog, int id ) -> deleteResults() ).setNegativeButton(android.R.string.cancel, ( DialogInterface dialog, int id ) -> dialog.dismiss() ) .show();
    }
    private void deleteResults()
    {
        List<Result> results = checks.stream().filter(CheckBox::isChecked).map( CheckBox::getTag ).
                map( item->(Result) item).collect(Collectors.toList());
        try
        {
            resultDao.delete( results );
            fillList();
            if( actionMode != null )
                finishActionMode();
            
            setResult( RESULT_OK );

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void finishActionMode()
    {
        checks.stream().forEach(item -> { item.setVisibility(View.INVISIBLE); item.setChecked(false);});
        actionMode.finish();
        actionMode = null;
    }

    public void makeToolbar()
    {
        toolbar = findViewById(R.id.tooBar);
        TextView tv = findViewById(R.id.tvTitle);
        tv.setText(R.string.msgHistoryActivityTitle);
        setSupportActionBar(toolbar);
    }

    class ExClickListener implements View.OnClickListener
    {

        private final Integer id;

        public ExClickListener(Integer id)
        {
            this.id = id;
        }

        @Override
        public void onClick(View v)
        {
            Intent in = new Intent(HistoryActivity.this, AExerciseActivity.getExerciseActivityClass(template));
            in.putExtra(AExerciseActivity.RESULT_ID, id);
            in.putExtra(ProgressActivity.TEMPLATE_ID, template.getId());
            startActivityForResult(in, ActivityIDs.EXERCISEACTIVITY_ID);
        }
    }

    class ActionModeCallback implements  androidx.appcompat.view.ActionMode.Callback
    {
        @Override
        public boolean onCreateActionMode(androidx.appcompat.view.ActionMode mode, Menu menu)
        {
            mode.getMenuInflater().inflate(R.menu.history_actions, menu);
            deleteActionMenuItem = menu.findItem( R.id.itemActionDeleteResults );
            return true;
        }

        @Override
        public boolean onPrepareActionMode(androidx.appcompat.view.ActionMode mode, Menu menu)
        {
            return false;
        }

        @Override
        public boolean onActionItemClicked(androidx.appcompat.view.ActionMode mode, MenuItem item)
        {
            if( item.getItemId() == R.id.itemActionDeleteResults )
            {
                try
                {
                    beginDeleteResults();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
                return true;
            }
            else if ( item.getItemId() == R.id.itemActionCheckAll )
            {
                checks.stream().forEach( chb->chb.setChecked( !chb.isChecked() ) );
                return true;
            }
            else
                return false;
        }

        @Override
        public void onDestroyActionMode(androidx.appcompat.view.ActionMode mode)
        {
            mode = null;
            deleteActionMenuItem.setEnabled( false );
            deleteActionMenuItem = null;
        }
    }
}
