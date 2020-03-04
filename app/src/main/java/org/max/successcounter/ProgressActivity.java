package org.max.successcounter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.j256.ormlite.dao.Dao;

import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

// TODO: Увелить шрифт в таблице результатов
// TODO: Кнопка "История" не в дугу
// TODO: Сделать анимацию при выводе деталей упражнения

public class ProgressActivity extends AppCompatActivity
{
    public final static String TEMPLATE_ID = "TEMPLATE_ID";
    Integer templateId;
    LineChart mChart;
    Dao<Template, Integer> exsetDao;
    Template template;
    SharedPreferences mPrefs;
    List<Result> results;
    Predicate<Result> currentFilter;
    Date startDate;
    Date endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_progress);
        mPrefs = getPreferences(Context.MODE_PRIVATE);
        DatabaseHelper db = new DatabaseHelper(this);
        try
        {
            Intent in = getIntent();
            templateId = in.getIntExtra(TEMPLATE_ID, -1);
            if (templateId == -1)
            {
                templateId = savedInstanceState.getInt(TEMPLATE_ID);
                templateId = mPrefs.getInt(TEMPLATE_ID, -1);
                if (templateId == -1)
                    throw new IllegalArgumentException();
            }

            exsetDao = db.getDao(Template.class);
            readResultsFromDB();

            currentFilter = new ZeroFilter();
            startDate = getMinDate();
            endDate = getMaxDate();

            makeToolbar();

            FloatingActionButton btn = findViewById(R.id.btnAddNew);
            btn.setOnClickListener(e -> {
                gotoExercise();
            });

            TextView tv = findViewById(R.id.lbHistory);
            tv.setOnClickListener(e -> {
                gotoHistory();
            });

            prepareChart();
            fillChart();

            fillStats();
            setTitle(template.getName());

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void readResultsFromDB() throws SQLException
    {
        results = new ArrayList<>();
        template = exsetDao.queryForId(templateId);
        results.addAll(template.getResults());
    }

    private void fillStats()
    {
        SimpleDateFormat dt = new SimpleDateFormat("dd MMMM yyyy");

        TextView tv = findViewById(R.id.lbTotalExercises);
        int count = 0;

        tv.setText("" + template.getResults().size());

        for (Result res : template.getResults())
            count += res.getPoints();

        tv = findViewById(R.id.lbTotalSuccess);
        tv.setText("" + count);

        for (Result res : template.getResults())
            count += res.getShots();

        tv = findViewById(R.id.lbTotalShots);
        tv.setText("" + count);

        tv = findViewById(R.id.tvDateStart);

        if (startDate != null)
            tv.setText(dt.format(startDate));

        tv.setOnClickListener((View v) -> setDateStart());

        tv = findViewById(R.id.tvDateEnd);

        if (endDate != null)
            tv.setText(dt.format(endDate));

        tv.setOnClickListener((View v) -> setDateEnd());
    }

    private void setDateEnd()
    {
        getDate(endDate, new Function<Date, Date>()
        {
            @Override
            public Date apply(Date date)
            {
                if (date.before(startDate))
                    return null;

                endDate = date;
                SimpleDateFormat dt = new SimpleDateFormat("dd MMMM yyyy");
                TextView tv = findViewById(R.id.tvDateEnd);
                tv.setText(dt.format(endDate));
                applyDateFilter();
                return null;
            }
        });
    }

    private void setDateStart()
    {
        getDate(startDate, new Function<Date, Date>()
        {
            @Override
            public Date apply(Date date)
            {
                if (date.after(endDate))
                    return null;

                startDate = date;
                SimpleDateFormat dt = new SimpleDateFormat("dd MMMM yyyy");
                TextView tv = findViewById(R.id.tvDateStart);
                tv.setText(dt.format(startDate));
                applyDateFilter();
                return null;
            }
        });
    }

    void applyDateFilter()
    {
        DateIntervalFilter f = new DateIntervalFilter();
        f.setStart(startDate);
        f.setEnd(endDate);
        currentFilter = f;
        fillChart();
    }

    private void getDate(Date date, Function<Date, Date> callback)
    {
        Calendar c = Calendar.getInstance();

        if (date != null)
            c.setTime(date);

        DatePickerDialog dlg = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener()
        {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth)
            {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth);
                callback.apply(c.getTime());
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));

        dlg.show();
    }

    private void gotoHistory()
    {
        Intent in = new Intent(this, HistoryActivity.class);
        in.putExtra(TEMPLATE_ID, templateId);
        startActivityForResult(in, ActivityIDs.HISTORYACTIVITY_ID);
    }

    private void fillChart()
    {
        List<Entry> exes = new ArrayList<>();
        AtomicInteger i = new AtomicInteger();
        i.set(-1);
        List<Result> items = results.stream().filter(currentFilter).collect(Collectors.toList());

        items.forEach(item -> {
            Entry e = new Entry((float) i.incrementAndGet(), new Float(item.getPercent()));
            e.setData(item);
            exes.add( e );
        });

        mChart.clear();
        LineData data = new LineData();
        int color = Color.rgb(0xDD, 0x88, 0x00);

        LineDataSet set = new LineDataSet(exes, "%");
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setColor(color);
        set.setValueTextColor(getColor(android.R.color.black));
        data.setValueTextSize(12);
        data.addDataSet(set);

        if (items.size() > 2)
        {
            List<Entry> trend = makeTrend(items);
            color = getColor(android.R.color.holo_red_light);
            set = new LineDataSet(trend, getString(R.string.trend));
            set.setDrawCircleHole(false);
            set.setDrawCircles(false);
            set.setColor(color);
            set.setLineWidth(2f);
            set.setValueFormatter(new EmptyValueFormatter());
            data.addDataSet(set);
        }

        data.setValueTextSize(12);
        mChart.setData(data);
        mChart.invalidate();
    }

    private void prepareChart()
    {
        int axisColor = Color.LTGRAY;

        mChart = findViewById(R.id.chartHolder);
        YAxis y = mChart.getAxisLeft();
        y.setAxisMinimum(0f);
        y.setAxisMaximum(100f);
        y.setTextColor(axisColor);
        y.setGridColor(axisColor);
        y.setAxisLineColor(axisColor);
        y.setDrawLabels(false);
        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                onSelectValue( e, h );
            }

            @Override
            public void onNothingSelected()
            {
                deselectValue();
            }
        });

        y = mChart.getAxisRight();
        y.setAxisMinimum(0f);
        y.setAxisMaximum(100f);
        y.setTextColor(axisColor);
        y.setGridColor(axisColor);
        y.setAxisLineColor(axisColor);
        y.setDrawLabels(false);

        XAxis x = mChart.getXAxis();
        x.setGridColor(axisColor);
        x.setAxisLineColor(axisColor);
        x.setTextColor(axisColor);

        mChart.getLegend().setEnabled(false);
        mChart.setDescription(null);
    }

    private void deselectValue()
    {
        TextView tv = findViewById( R.id.tvResultDate );
        tv.setText( getString( R.string.txtExerciseNotSelected ) );

        tv = findViewById( R.id.tvPercent);
        tv.setText( "" );

        tv = findViewById( R.id.tvPoints );
        tv.setText( "" );

        tv = findViewById( R.id.tvAttempts );
        tv.setText( "" );
    }

    private void onSelectValue(Entry e, Highlight h)
    {
        Object o = e.getData();
        if( o == null )
            return;
        Result res = ( Result ) o;

        showResultDetails( res );
    }

    private void showResultDetails(Result res)
    {
        SimpleDateFormat sdf = new SimpleDateFormat( "dd MMMM yyyy" );

        TextView tv = findViewById( R.id.tvResultDate );
        tv.setText( sdf.format( res.getDate() ) );

        tv = findViewById( R.id.tvPercent);
        tv.setText( Result.getPercentString( res ));

        tv = findViewById( R.id.tvPoints );
        tv.setText( res.getPoints().toString() );

        tv = findViewById( R.id.tvAttempts );
        tv.setText( res.getShots().toString() );
    }

    private void gotoExercise()
    {
        Intent in;

        switch (template.getExType())
        {
            case simple:
                if (template.getLimited())
                {
                    if (template.getSuccesLimited())
                        in = new Intent(this, SeriesExerciseActivity.class);
                    else
                        in = new Intent(this, RunToExerciseActivity.class);
                } else
                    in = new Intent(this, SimpleExerciseActivity.class);
                break;
            case compound:
                in = new Intent(this, CompoundExerciseActivity.class);
                break;
            case series:
                in = new Intent(this, SeriesExerciseActivity.class);
                break;
            default:
                throw new IllegalArgumentException("Unknown template type");
        }
        in.putExtra(TEMPLATE_ID, template.getId());
        startActivityForResult(in, ActivityIDs.DO_EXERCISE_ACTIVITY_ID);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ActivityIDs.DO_EXERCISE_ACTIVITY_ID ||
                requestCode == ActivityIDs.HISTORYACTIVITY_ID)
            if (resultCode == RESULT_OK)
            {
                setResult(RESULT_OK);
                try
                {
                    readResultsFromDB();
                    startDate = getMinDate();
                    endDate = getMaxDate();
                    fillChart();
                    fillStats();

                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        super.onSaveInstanceState(savedInstanceState);
        SharedPreferences.Editor ed = mPrefs.edit();
        ed.putInt("templateID", templateId);
        ed.commit();
    }

    private List<Entry> makeTrend(List<Result> items)
    {
        ArrayList<Entry> trendData = new ArrayList<>();

        SimpleRegression sr = new SimpleRegression();

        for (int i = 1; i <= items.size(); i++)
            sr.addData(i, items.get(i - 1).getPercent());

        trendData.add(new Entry(0f, (float) sr.predict(1)));
        trendData.add(new Entry(items.size() - 1, (float) sr.predict(items.size())));
        return trendData;
    }

    public void makeToolbar()
    {
        TextView tv = findViewById(R.id.tvTitle);
        tv.setText(template.getName());
    }

    Date getMinDate()
    {
        List<Result> items = results.stream().filter(currentFilter).collect(Collectors.toList());
        try
        {
            Result res = items.stream().min(new Comparator<Result>()
            {
                @Override
                public int compare(Result o1, Result o2)
                {
                    return Long.compare(o1.getDate().getTime(), o2.getDate().getTime());
                }
            }).orElseThrow(NoSuchElementException::new);

            return res.getDate();

        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
            return null;
        }
    }

    Date getMaxDate()
    {
        List<Result> items = results.stream().filter(currentFilter).collect(Collectors.toList());

        try
        {
            Result res = items.stream().max(new Comparator<Result>()
            {
                @Override
                public int compare(Result o1, Result o2)
                {
                    return Long.compare(o1.getDate().getTime(), o2.getDate().getTime());
                }
            }).orElseThrow(NoSuchElementException::new);

            return res.getDate();

        } catch (Throwable throwable)
        {
            throwable.printStackTrace();
            return null;
        }
    }

    class ZeroFilter implements Predicate<Result>
    {

        @Override
        public boolean test(Result result)
        {
            return true;
        }
    }

    class DateIntervalFilter implements Predicate<Result>
    {
        Date start;
        Date end;

        @Override
        public boolean test(Result result)
        {
            return result.getDate().getTime() >= start.getTime() && result.getDate().getTime() <= end.getTime();
        }

        public void setStart(Date start)
        {
            this.start = start;
        }

        public void setEnd(Date end)
        {
            this.end = end;
        }
    }
}
