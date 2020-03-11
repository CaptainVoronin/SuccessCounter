package org.max.successcounter;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
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
import org.jetbrains.annotations.NotNull;
import org.max.successcounter.db.DatabaseHelper;
import org.max.successcounter.model.ResultDateComparator;
import org.max.successcounter.model.TagsOperator;
import org.max.successcounter.model.excercise.Result;
import org.max.successcounter.model.excercise.Tag;
import org.max.successcounter.model.excercise.Template;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import lombok.Setter;

// TODO: Увелить шрифт в таблице результатов

public class ProgressActivity extends AppCompatActivity implements DialogTags.DialogTagsResultListener
{
    public static final String DateFilterID = "DATEFILTER";
    public static final String TagFilterID = "TAGFILTER";

    public final static String TEMPLATE_ID = "TEMPLATE_ID";
    Integer templateId;
    LineChart mChart;
    Dao<Template, Integer> exsetDao;
    Dao<Tag, Integer> tagsDao;
    Template template;
    SharedPreferences mPrefs;
    List<Result> results;
    MultiFilter currentFilter;
    Date startDate;
    Date endDate;
    Result currentResult;
    List<Tag> tagFilterSet;
    List<Tag> currentFilterTagSet;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);
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

            TagsOperator.instance.init(db);

            exsetDao = db.getDao(Template.class);
            readResultsFromDB();

            tagsDao = db.getDao(Tag.class);
            tagFilterSet = new ArrayList<>();
            currentFilter = new MultiFilter();

            if (results.size() != 0)
            {
                startDate = getMinResultDateInFullSet();
                endDate = getMaxResultDateInFullSet();
            } else
                startDate = endDate = Calendar.getInstance().getTime();
            setDateFilter(getMinResultDateInFullSet(), getMaxResultDateInFullSet());

            makeToolbar();

            FloatingActionButton btn = findViewById(R.id.btnAddNew);
            btn.setOnClickListener(e -> {
                gotoExercise();
            });

            ImageView ibtn = findViewById(R.id.btnHistory);
            ibtn.setOnClickListener(e -> {
                gotoHistory();
            });

            ibtn = findViewById(R.id.btnSetTagFilter);
            ibtn.setOnClickListener(v -> showTagsDialogForFilter());

            ibtn = findViewById(R.id.btnResultTags);
            ibtn.setEnabled(false);
            ibtn.setOnClickListener(v -> showTagsDialogForResultTags());

            prepareChart();
            fillChart();

            fillStats();
            setTitle(template.getName());

            TextView tv = findViewById(R.id.tvComment);
            tv.setOnClickListener((View v) -> showComment(v));


        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void showTagsDialogForFilter()
    {
        try
        {
            DialogTags dlgTags = new DialogTags(this, TagsOperator.instance, this, false);
            dlgTags.setTitle(getString(R.string.title_dialog_tag_filter));
            dlgTags.showDialog(currentFilterTagSet);
        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void showTagsDialogForResultTags()
    {
        try
        {
            if (currentResult == null)
                return;

            DialogTags dlgTags = new DialogTags(this, TagsOperator.instance, new ResultTagsDialogListener(), true);
            dlgTags.setTitle(getString(R.string.title_dialog_result_tags));
            dlgTags.showDialog(currentResult.getTags());

        } catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    private void showComment(View v)
    {
        TextView tv = (TextView) v;
        String text = tv.getText().toString();

        if (text == null || text.length() == 0)
            return;

        AlertDialog.Builder dlg = new AlertDialog.Builder(this);
        dlg.setMessage(text).setNegativeButton(android.R.string.cancel, (d, id) -> d.dismiss()).show();
    }

    private void readResultsFromDB() throws SQLException
    {
        results = new ArrayList<>();
        template = exsetDao.queryForId(templateId);
        results.addAll(template.getResults());
        for (Result res : results)
            setTagsFoResult(res);
        results.sort(new ResultDateComparator());
    }

    private void setTagsFoResult(Result result) throws SQLException
    {
        List<Tag> tags = TagsOperator.instance.getTagsForResult(result);
        result.setTags(tags);
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

        ImageView btn = findViewById(R.id.btnStartDate);
        if (startDate != null)
        {
            tv = findViewById(R.id.tvStartDate);
            tv.setText(dt.format(startDate));
        }
        btn.setOnClickListener((View v) -> setDateStart());

        btn = findViewById(R.id.btnEndDate);

        if (endDate != null)
        {
            tv = findViewById(R.id.tvEndDate);
            tv.setText(dt.format(endDate));
        }

        btn.setOnClickListener((View v) -> setDateEnd());
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
                TextView tv = findViewById(R.id.tvEndDate);
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
                TextView tv = findViewById(R.id.tvStartDate);
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
        currentFilter.addFilter(DateFilterID, f);
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
            exes.add(e);
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
                onSelectValue(e, h);
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
        TextView tv = findViewById(R.id.tvResultDate);
        tv.setText(getString(R.string.txtExerciseNotSelected));

        tv = findViewById(R.id.tvPercent);
        tv.setText("");

        tv = findViewById(R.id.tvPoints);
        tv.setText("");

        tv = findViewById(R.id.tvAttempts);
        tv.setText("");

        tv = findViewById(R.id.tvTags);
        tv.setText("");

        currentResult = null;
        ImageView ibtn = findViewById(R.id.btnResultTags);
        ibtn.setEnabled(false);

    }

    private void onSelectValue(Entry e, Highlight h)
    {
        Object o = e.getData();
        if (o == null)
            return;
        currentResult = (Result) o;

        ImageView ibtn = findViewById(R.id.btnResultTags);
        ibtn.setEnabled(true);

        showResultDetails(currentResult);
    }

    private void showResultDetails(Result res)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");

        TextView tv = findViewById(R.id.tvResultDate);
        tv.setText(sdf.format(res.getDate()));

        tv = findViewById(R.id.tvPercent);
        tv.setText(Result.getPercentString(res));

        tv = findViewById(R.id.tvPoints);
        tv.setText(res.getPoints().toString());

        tv = findViewById(R.id.tvAttempts);
        tv.setText(res.getShots().toString());

        tv = findViewById(R.id.tvComment);
        tv.setText(res.getComment());

        tv = findViewById(R.id.tvTags);
        if (res.getTags().size() != 0)
        {
            List<String> names = res.getTags().stream().map(Tag::getTag).collect(Collectors.toList());
            String buff = String.join(";", names);
            tv.setText(buff);
        }
    }

    private void gotoExercise()
    {
        Intent in;
        in = new Intent(this, AExerciseActivity.getExerciseActivityClass(template));
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
                    setDateFilter(getMinResultDateInFullSet(), getMaxResultDateInFullSet());
                    fillChart();
                    fillStats();

                } catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
    }

    private void setDateFilter(Date minDate, Date maxDate)
    {

        if (minDate.before(startDate))
            startDate = minDate;

        if (maxDate.after(endDate))
            endDate = maxDate;

        DateIntervalFilter dateIntervalFilter = new DateIntervalFilter();
        dateIntervalFilter.setStart(startDate);
        dateIntervalFilter.setEnd(endDate);
        currentFilter.addFilter(DateFilterID, dateIntervalFilter);
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

    Date getMinResultDateInFullSet()
    {
        if (results != null && results.size() != 0)
            return results.get(0).getDate();
        else
            return Calendar.getInstance().getTime();
    }

    Date getMaxResultDateInFullSet()
    {
        if (results != null && results.size() != 0)
            return results.get(results.size() - 1).getDate();
        else
            return Calendar.getInstance().getTime();
    }

    /**
     * Callback fo DialogTags
     *
     * @param result
     * @param checked
     */
    @Override
    public void onResult(boolean result, List<Tag> checked)
    {
        if (result)
        {
            currentFilterTagSet = new ArrayList<>();
            currentFilterTagSet.addAll(checked);
            setTagFilter(currentFilterTagSet);
            ImageView ibtn = findViewById(R.id.btnSetTagFilter);

            if (checked.size() != 0)
                ibtn.setImageDrawable(getDrawable(R.drawable.ic_filter_list_green_36dp));
            else
                ibtn.setImageDrawable(getDrawable(R.drawable.ic_filter_list_gray_36dp));
        }
    }

    private void setTagFilter(List<Tag> checked)
    {
        TagFilter filter = new TagFilter();
        filter.setTagSet(checked);
        currentFilter.addFilter(TagFilterID, filter);
        fillChart();
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

    class TagFilter implements Predicate<Result>
    {
        @Setter
        @NotNull
        List<Tag> tagSet;

        public TagFilter()
        {
            tagSet = new ArrayList<>();
        }

        @Override
        public boolean test(Result result)
        {
            if (tagSet.size() == 0)
                return true;
            else
                return result.getTags().containsAll(tagSet);
        }
    }

    class MultiFilter implements Predicate<Result>
    {
        HashMap<String, Predicate<Result>> filters;

        public MultiFilter()
        {
            filters = new HashMap<>();
        }

        public void addFilter(String id, Predicate<Result> filter)
        {
            filters.put(id, filter);
        }

        public void removeFilter(String id)
        {
            filters.remove(id);
        }

        @Override
        public boolean test(Result result)
        {
            for (Predicate<Result> filter : filters.values())
            {
                if (!filter.test(result))
                    return false;
            }
            return true;
        }
    }

    /**
     * A callback for the result tags dialog
     */
    class ResultTagsDialogListener implements DialogTags.DialogTagsResultListener
    {
        @Override
        public void onResult(boolean result, List<Tag> checked)
        {
            // Save old tag set in case of problems with update
            List<Tag> oldSet = currentResult.getTags();
            try
            {
                currentResult.setTags(checked);
                TagsOperator.instance.setTagsForResult(currentResult);
                showResultDetails(currentResult);
            } catch (SQLException e)
            {
                // The old set will be restored in case of problems with DB
                currentResult.setTags(oldSet);
                e.printStackTrace();
            }
        }
    }
}
