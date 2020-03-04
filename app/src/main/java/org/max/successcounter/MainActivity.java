package org.max.successcounter;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.ForeignCollection;

import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

// TODO: Подвинуть кнопку вправо
public class MainActivity extends AppCompatActivity
{
    DatabaseHelper dbHelper;
    Dao<Template, Integer> exTemplateDao;
    TableLayout table;
    ResultComparator comparator;
    SimpleDateFormat dateFormatter;
    FloatingActionButton btnPlus;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        makeToolbar();
        comparator = new ResultComparator();
        table = findViewById(R.id.mainTable);
        dbHelper = new DatabaseHelper(this);
        dateFormatter = new SimpleDateFormat("dd.MM.yyyy");
        btnPlus = findViewById(R.id.btnAddNew);

        try
        {
            exTemplateDao = dbHelper.getDao(Template.class);
            fillList();
        } catch (SQLException e)
        {
            e.printStackTrace();
        }

        btnPlus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent in = new Intent(MainActivity.this, NewExerciseActivity.class);
                startActivityForResult(in, ActivityIDs.NEW_TEMPLATE_ACTIVITY_ID);
            }
        });
    }

    private void fillList() throws SQLException
    {
        List<Template> list = exTemplateDao.queryForAll();
        table.removeAllViews();

        for (Template set : list)
        {
            TableRow tr = makeRow(set);
            table.addView(tr);
        }
    }

    private TableRow makeRow(Template template)
    {
        TableRow tr = (TableRow) getLayoutInflater().inflate(R.layout.exsetrow, null);

        TextView tv = tr.findViewById(R.id.lbName);
        tv.setTag(template);
        tv.setText(template.getName());
        tv.setOnClickListener(new OnExSetClick(template));
        tv.setOnLongClickListener(new ExSetLongClickListener(tv));


        if (template.getResults() != null && template.getResults().size() > 0)
        {
            org.max.successcounter.model.excercise.Result res = getLatestResult(template.getResults());

            tv = tr.findViewById(R.id.lbPercent);
            tv.setTag(template);
            tv.setText(Result.getPercentString(res));
            tv.setOnClickListener(new OnExSetClick(template));
            tv.setOnLongClickListener(new ExSetLongClickListener(tv));

            tv = tr.findViewById(R.id.lbDate);
            tv.setTag(template);
            tv.setText(dateFormatter.format(res.getDate()));
            tv.setOnClickListener(new OnExSetClick(template));
            tv.setOnLongClickListener(new ExSetLongClickListener(tv));

            if (template.getResults().size() > 2)
            {
                int direction = Template.regressionDirection(template.getResultAsList());
                int drawableID;

                if (direction == 1)
                    drawableID = R.drawable.up_arrow_green_1;
                else if (direction == 0)
                    drawableID = R.drawable.right_arrow_1;
                else
                    drawableID = R.drawable.down_arrow_red_1;

                ImageView img = tr.findViewById(R.id.imgTrendArrow);
                img.setImageDrawable(getDrawable(drawableID));
                tv.setOnClickListener(new OnExSetClick(template));
                tv.setOnLongClickListener(new ExSetLongClickListener(tv));
            }
        }

        return tr;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return false;
    }

    /**
     * Запустить экран со списком выполненных подходов к упражнению
     *
     * @param id - упражнение
     */
    private void gotoExercise(Template template)
    {
        Intent in = new Intent(this, ProgressActivity.class);

        in.putExtra(ProgressActivity.TEMPLATE_ID, template.getId());

        startActivityForResult(in, ActivityIDs.DO_EXERCISE_ACTIVITY_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if (requestCode == ActivityIDs.DO_EXERCISE_ACTIVITY_ID ||
                requestCode == ActivityIDs.NEWSIMPLEEXERCISE_ID ||
                requestCode == ActivityIDs.NEW_TEMPLATE_ACTIVITY_ID)
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

    private void renameTemplate(Object tag)
    {
        Template res = (Template) tag;
        AlertDialog.Builder b = makeDialog("Переименовать");
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.newexsetdialog, null);
        b.setView(view);

        EditText ed = view.findViewById(R.id.edName);
        ed.setText(res.getName());

        b.setPositiveButton(R.string.isSave, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                try
                {
                    dialog.dismiss();
                    String name = ed.getText().toString();
                    res.setName(name);
                    //exTemplateDao.update(set);
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

    private void deleteTemplate(Object tag)
    {
        Template template = (Template) tag;
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Удаление");
        builder.setMessage("Подтвердите удаление " + template.getName());
        builder.setPositiveButton("Да", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
                try
                {
                    exTemplateDao.delete(template);
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
                        deleteTemplate(v.getTag());
                        return true;
                    case R.id.idMenuRenameExSet:
                        renameTemplate(v.getTag());
                        return true;
                    default:
                        return false;
                }
            }

        });

        popup.show();
        return false;
    }

    private Result getLatestResult(ForeignCollection<Result> results)
    {
        List<Result> lst = new ArrayList<>();
        results.forEach(item -> lst.add(item));
        Collections.sort(lst, comparator);
        Result res = lst.get(results.size() - 1);
        return res;
    }

    AlertDialog.Builder makeDialog(String title)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.newexsetdialog, null);
        b.setView(view);
        b.setTitle(title);
        b.setCancelable(true);
        //EditText ed = table.findViewById(R.id.edName);
        return b;
    }

    public void makeToolbar()
    {

        Toolbar tb = findViewById( R.id.tooBar );
        tb.inflateMenu( R.menu.main_menu );
        tb.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.idAbout:
                        showAbout();
                        return true;
                    default:
                        return false;
                }
            }
        });

        // Set the title text
        TextView tv = findViewById(R.id.tvTitle);
        tv.setText( getString( R.string.txtMainActivityTitle ) );
    }

    private void showAbout()
    {
        Intent in = new Intent( this, AboutActivity.class );
        startActivity( in );
    }

    class ResultComparator implements Comparator<Result>
    {
        @Override
        public int compare(Result o1, Result o2)
        {
            return Long.compare(o1.getDate().getTime(), o2.getDate().getTime());
        }
    }

    class OnExSetClick implements View.OnClickListener
    {
        Template template;

        public OnExSetClick(Template template)
        {
            this.template = template;
        }

        @Override
        public void onClick(View v)
        {
            gotoExercise(template);
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
