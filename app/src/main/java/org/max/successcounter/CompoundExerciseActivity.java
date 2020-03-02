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

import org.apache.commons.math3.analysis.function.Min;
import org.max.successcounter.model.excercise.CompoundExcercise;

public class CompoundExerciseActivity extends LineChartExerciseActivity<CompoundExcercise>
{

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
