package org.max.successcounter;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ComplexButton
{
    private final int layoutID = R.layout.complex_button_layout;
    TextView tvTitle;
    TextView tvText;
    private Activity ctx;
    private LinearLayout layout;
    private String title;
    private String text;
    private View.OnClickListener clickListener;

    public ComplexButton(Activity ctx, String title, String text, View.OnClickListener clickListener)
    {
        this.ctx = ctx;
        this.title = title;
        this.text = text;
        this.clickListener = clickListener;
    }

    public LinearLayout inflate()
    {
        layout = (LinearLayout) ctx.getLayoutInflater().inflate(layoutID, null, false);
        tvTitle = layout.findViewById(R.id.tvTitle);
        tvTitle.setText(title);
        tvTitle.setOnClickListener(clickListener);

        tvText = layout.findViewById(R.id.tvText);
        tvText.setText(text);
        tvText.setOnClickListener(clickListener);

        return layout;
    }

    public void setEnabled(boolean enabled)
    {
        tvTitle.setEnabled( enabled );
        tvText.setEnabled( enabled );
/*
        if (enabled)
        {
            //tvTitle.setTextColor(ctx.getColor(android.R.color.black));
            tvTitle.setEnabled( enabled );
        }
        else
            tvTitle.setTextColor(ctx.getColor(android.R.color.darker_gray));
*/
    }
}
