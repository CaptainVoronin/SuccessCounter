package org.max.successcounter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity
{
    DatabaseHelper dbHelper;
    Dao<ExerciseSet, Integer> exSetDao;
    TableLayout table;
    ListView mainList;
    ExerciseComparator comparator;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        comparator = new ExerciseComparator();
        table = findViewById(R.id.mainTable);
        dbHelper = new DatabaseHelper(this);

        try
        {
            exSetDao = dbHelper.getDao(ExerciseSet.class);
            fillList();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void fillList() throws SQLException
    {
        List<ExerciseSet> list = exSetDao.queryForAll();
        table.removeAllViews();
        
        for( ExerciseSet set : list )
        {
            TableRow tr = makeRow( set );
            table.addView( tr );
        }
    }

    private TableRow makeRow(ExerciseSet set)
    {

        TableRow tr = (TableRow) getLayoutInflater().inflate( R.layout.exsetrow, null );

        TextView tv = tr.findViewById( R.id.lbName );
        tv.setTag( set );
        tv.setText( set.getName() );
        tv.setOnClickListener(new OnExSetClick(set.getId()));
        tv.setOnLongClickListener(new ExSetLongClickListener(tv));

        if( set.getExercises().size() != 0 )
        {
            List<Exercise> lst = new ArrayList<>();
            lst.addAll(set.getExercises());
            Exercise ex = getLatestExercise(lst);

            tv = tr.findViewById( R.id.lbPercent );
            tv.setTag( set );
            tv.setText( Exercise.getPercentString( ex ) );
            tv.setOnClickListener(new OnExSetClick(set.getId()));
            tv.setOnLongClickListener(new ExSetLongClickListener(tv));

            tv = tr.findViewById( R.id.lbDate );
            tv.setTag( set );
            tv.setText( Exercise.getFormattedDate( ex ) );
            tv.setOnClickListener(new OnExSetClick(set.getId()));
            tv.setOnLongClickListener(new ExSetLongClickListener(tv));
        }

        return tr;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.idAddNew:
                addNewExerciseSet();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewExerciseSet()
    {
        AlertDialog.Builder b = makeDialog("Новое упражнение");
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.newexsetdialog, null);
        b.setView(view);

        EditText ed = view.findViewById(R.id.edName);

        b.setPositiveButton(R.string.isSave, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
                saveNewExSet(ed.getText().toString());
            }
        });

        b.setNegativeButton(R.string.idCancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        b.show();

    }

    void saveNewExSet(String name)
    {
        ExerciseSet set = new ExerciseSet();
        set.setName(name);
        int id = 0;
        try
        {
            id = exSetDao.create(set);
            set.setId(id);
            fillList();

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    /**
     * ЗАпустить экран со списком выполненных подходов к упражнению
     *
     * @param id - упражнение
     */
    private void gotoExercise(Integer id)
    {
        Intent in = new Intent(this, ExerciseDynamicActivity.class);
        //in.putExtra(HistoryActivity.EX_SET_ID, id);
        in.putExtra(ExerciseDynamicActivity.EX_SET_ID, id);
        startActivityForResult(in,ActivityIDs.EXERCISEDYNAMICACTIVITY_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if( requestCode == ActivityIDs.EXERCISEDYNAMICACTIVITY_ID )
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

    private void renameExSet(Object tag)
    {
        ExerciseSet set = (ExerciseSet) tag;
        AlertDialog.Builder b = makeDialog("Переименовать");
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.newexsetdialog, null);
        b.setView(view);

        EditText ed = view.findViewById(R.id.edName);
        ed.setText(set.getName());

        b.setPositiveButton(R.string.isSave, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try
                {
                    dialog.dismiss();
                    String name = ed.getText().toString();
                    set.setName(name);
                    exSetDao.update(set);
                    fillList();
                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        });

        b.setNegativeButton(R.string.idCancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        b.show();
    }

    private void deleteExSet(Object tag)
    {
        ExerciseSet set = (ExerciseSet) tag;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Удаление");
        builder.setMessage("Подтвердите удаление " + set.getName());
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
                try
                {
                    exSetDao.delete(set);
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

    boolean showPopup(View v)
    {
        PopupMenu popup = new PopupMenu(MainActivity.this, v);
        //Inflating the Popup using xml file
        popup.getMenuInflater()
                .inflate(R.menu.exset_popup_menu, popup.getMenu());

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
        {
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.idMenuDeleteExSet:
                        deleteExSet(v.getTag());
                        return true;
                    case R.id.idMenuRenameExSet:
                        renameExSet(v.getTag());
                        return true;
                    default:
                        return false;
                }
            }

        });

        popup.show();
        return false;
    }

    private Exercise getLatestExercise(List<Exercise> exercises)
    {
        Collections.sort((List) exercises, comparator);
        Exercise ex = (Exercise) ((List) exercises).get(exercises.size() - 1);
        return ex;
    }

    AlertDialog.Builder makeDialog(String title)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.newexsetdialog, null);
        b.setView(view);
        b.setTitle(title);
        b.setCancelable(true);
        //EditText ed = view.findViewById(R.id.edName);
        return b;
    }

    class ExSetAdapter extends BaseAdapter
    {
        List<ExerciseSet> items;

        public ExSetAdapter(List<ExerciseSet> items)
        {
            this.items = items;
        }

        @Override
        public int getCount()
        {
            return items.size();
        }

        @Override
        public Object getItem(int position)
        {
            return items.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return items.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup container)
        {
            if (convertView == null)
            {
                convertView = getLayoutInflater().inflate(R.layout.exsetrowview, container, false);
            }
            ExerciseSet set = (ExerciseSet) getItem(position);
            TextView tv = (TextView) convertView.findViewById(R.id.lbAttempts);

            tv.setText(set.getName());
            tv.setOnClickListener(new OnExSetClick(set.getId()));

            tv.setTag(set);
            tv.setOnLongClickListener(new ExSetLongClickListener(tv));

            TextView tvPercent = (TextView) convertView.findViewById(R.id.lbPercent);
            TextView tvDate = (TextView) convertView.findViewById(R.id.lbDate);
            if (set.getExercises() != null && set.getExercises().size() > 0)
            {
                List<Exercise> lst = new ArrayList<>();
                lst.addAll(set.getExercises());
                Exercise ex = getLatestExercise(lst);

                tvPercent.setText(Exercise.getPercentString(ex));
                tvPercent.setOnClickListener(new OnExSetClick(set.getId()));
                tvPercent.setOnLongClickListener(new ExSetLongClickListener(tv));

                tvDate.setText(Exercise.getFormattedDate(ex));
                tvDate.setOnLongClickListener(new ExSetLongClickListener(tv));
                tvDate.setOnClickListener(new OnExSetClick(set.getId()));
            }
            else
            {
                tvPercent.setText( "" );
                tvDate.setText( "" );
            }
            return convertView;
        }
    }

    class ExerciseComparator implements Comparator<Exercise>
    {
        @Override
        public int compare(Exercise o1, Exercise o2)
        {
            return Long.compare(o1.getDate().getTime(), o2.getDate().getTime());
        }
    }

    class OnExSetClick implements View.OnClickListener
    {
        Integer id;

        public OnExSetClick(Integer id)
        {
            this.id = id;
        }

        @Override
        public void onClick(View v)
        {
            gotoExercise(id);
        }
    }

    class ExSetLongClickListener implements View.OnLongClickListener
    {
        private final View view;

        public ExSetLongClickListener(View view)
        {
            this.view = view;
        }

        @Override
        public boolean onLongClick(View v)
        {
            return showPopup(v);
        }
    }
}
