package org.max.successcounter;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import org.max.successcounter.model.excercise.CompoundExcercise;

public class CompoundExerciseActivity extends AExerciseActivity<CompoundExcercise>
{
    private LineChart mChart;

    @Override
    protected void prepareControlButtons(LinearLayout placeholder)
    {
        CompoundExcercise cx = ( CompoundExcercise ) getExercise();

        LayoutInflater lif = getLayoutInflater();
        LinearLayout ll = (LinearLayout) lif.inflate( R.layout.compound_exercise_buttons, placeholder, true );
        ll.setOrientation( LinearLayout.VERTICAL );
        ll.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
        ll.setWeightSum( 0.5f );
        cx.getOptions().forEach( item->{
            ll.addView( makeButton( item ) );
        } );
    }

    @Override
    protected void prepareChart(LinearLayout placeholder)
    {
        LayoutInflater lif = getLayoutInflater();
        lif.inflate( R.layout.line_chart, placeholder, true );
        int axisColor = Color.LTGRAY;
        mChart = placeholder.findViewById(R.id.chartHolder);
        mChart.setDrawMarkers(false);
        mChart.setDrawGridBackground(false);
        mChart.setLayoutParams( new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT) );

        YAxis y = mChart.getAxisLeft();
        y.setAxisMinimum(0f);
        y.setAxisMaximum(100f);
        y.setTextColor(axisColor);
        y.setGridColor(axisColor);
        y.setAxisLineColor(axisColor);

        y = mChart.getAxisRight();
        y.setAxisMinimum(0f);
        y.setAxisMaximum(100f);
        y.setTextColor(axisColor);
        y.setGridColor(axisColor);
        y.setAxisLineColor(axisColor);

        XAxis x = mChart.getXAxis();
        x.setGridColor(axisColor);
        x.setAxisLineColor(axisColor);
        x.setTextColor(axisColor);

        Legend legend = mChart.getLegend();
        legend.setEnabled(false);
        mChart.setDescription( null );

    }

    @Override
    protected void updateChart()
    {
        mChart.clear();
        LineData data = new LineData();
        int color = Color.rgb(0xDD, 0x88, 0x00);
        LineDataSet set = new LineDataSet(getExercise().getPercentHistory(), "%");
        set.setDrawCircleHole(false);
        set.setDrawCircles(false);
        set.setColor(color);
        set.setValueFormatter(new EmptyValueFormatter());
        data.setDrawValues(false);
        data.addDataSet(set);
        mChart.setData(data);
        mChart.invalidate();
    }

    @Override
    public void onExerciseFinished()
    {

    }

    public class OnOptionClick implements Button.OnClickListener
    {
        @Override
        public void onClick(View v)
        {
            Object tag = v.getTag();
            CompoundExcercise.Option opt = (CompoundExcercise.Option ) tag;
            addStep( opt.getPoints() );
        }
    }

    Button makeButton( CompoundExcercise.Option option )
    {
        GradientDrawable gdw = new GradientDrawable();
        gdw.setColor( getColor( R.color.dark_blue )  );
        gdw.setCornerRadius( 20 );
        gdw.setStroke(3, getColor( R.color.secondAccent ));

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        lp.setMargins( 15, 10, 15, 10 );

        Button btn = new Button( this );
        btn.setText( option.getDescription() );
        btn.setTextColor( getColor( R.color.secondAccent ) );
        btn.setTag( option );
        btn.setOnClickListener( new OnOptionClick() );
        btn.setBackground( gdw );
        btn.setLayoutParams( lp );
        return btn;
    }
}
