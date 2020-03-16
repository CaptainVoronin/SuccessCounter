package org.max.successcounter;

import android.app.Activity;
import android.graphics.Color;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import org.max.successcounter.model.excercise.OptionDescription;
import org.max.successcounter.model.excercise.Template;

public class ExerciseOutcomes extends AbstractTableAdapter<OptionDescription>
{
    EditorListener editorListener;
    Template template;

    public ExerciseOutcomes(Activity ctx, int view_id, Template template)
    {
        super(ctx, view_id, R.layout.compound_default_outcome, template.getOptionsAsListAllSteps());
        editorListener = new EditorListener();
        this.template = template;
    }

    @Override
    public void makeTable()
    {
        super.makeTable();
        getTable().addView(makePlaceholder(), getItems().size() - 1);
    }

    @Override
    protected TableRow makeRow(OptionDescription item)
    {
        if (item.getFirstDefault())
            return makeUneditableRow(item);
        else if (item.getLastDefault())
            return makeSummaryRow(item);
        else
            return makeGeneralRow(item);
    }

    /**
     * It makes the row with the summary step which sums points from all the options
     *
     * @param item
     * @return
     */
    private TableRow makeSummaryRow(OptionDescription item)
    {
        TextView tvStepName;
        TextView tvStepPoints;
        ImageView btnRemove;

        TableRow row = (TableRow) getContext().getLayoutInflater().inflate(getRow_layout_id(), null, false);
        tvStepName = row.findViewById(R.id.lbStepName);
        tvStepPoints = row.findViewById(R.id.lbStepPoints);
        btnRemove = row.findViewById(R.id.btnRemoveOption);
        btnRemove.setOnClickListener(v -> {
            template.setHasSummaryStep(false);
            if (editorListener.getCurrent() != null)
                editorListener.getCurrent().cancel();
            makeTable();
        });

        if (template.isHasSummaryStep())
        {
            tvStepName.setText(item.getDescription());
            tvStepPoints.setVisibility(View.VISIBLE);
            tvStepPoints.setText(item.getPoints().toString());
            btnRemove.setVisibility(View.VISIBLE);
        } else
        {
            tvStepPoints.setVisibility(View.INVISIBLE);
            tvStepName.setText(getContext().getString(R.string.messageRestoreSummaryStep));
            tvStepName.setOnClickListener(v -> {
                template.setHasSummaryStep(true);
                if (editorListener.getCurrent() != null)
                    editorListener.getCurrent().cancel();
                makeTable();
            });
            btnRemove.setVisibility(View.INVISIBLE);
        }

        return row;
    }

    private TableRow makeUneditableRow(OptionDescription item)
    {
        TableRow row = (TableRow) getContext().getLayoutInflater().inflate(getRow_layout_id(), null, false);
        TextView tv = row.findViewById(R.id.lbStepName);
        tv.setText(item.getDescription());

        tv = row.findViewById(R.id.lbStepPoints);
        tv.setText(item.getPoints().toString());

        ImageView btnRemove;
        btnRemove = row.findViewById(R.id.btnRemoveOption);
        btnRemove.setVisibility(View.INVISIBLE);
        return row;
    }

    private TableRow makeGeneralRow(OptionDescription item)
    {
        TableRow row = (TableRow) getContext().getLayoutInflater().inflate(getRow_layout_id(), null, false);
        InplaceEditor editor = new InplaceEditor(editorListener, item, row);
        TextView tv = row.findViewById(R.id.lbStepName);
        tv.setText(item.getDescription());
        tv.setOnClickListener(editor);

        tv = row.findViewById(R.id.lbStepPoints);
        tv.setText(item.getPoints().toString());
        tv.setOnClickListener(editor);

        // Here getOptionsAsList is used deliberately because
        // we need only non default options here which can be removed by user
        if (template.getOptionsAsList().stream().filter(op -> !op.getFirstDefault()).filter(op -> !op.getLastDefault()).count() <= 2)
        {
            ImageView iv = row.findViewById(R.id.btnRemoveOption);
            iv.setVisibility(View.INVISIBLE);
        }

        return row;
    }

    private TableRow makePlaceholder()
    {
        TableRow tr = (TableRow) getContext().getLayoutInflater().inflate(R.layout.add_step_placeholder, null, false);
        ImageView btn = tr.findViewById(R.id.btnAddStep);
        btn.setOnClickListener(new PlaceholderClickListener(editorListener));
        return tr;
    }

    private class InplaceEditor implements View.OnClickListener
    {
        Color color;
        TextView tvDesc, tvPoints;
        EditText edDesc, edPoints;
        ImageView btnSave, btnRemove;
        EditorListener editorListener;

        TableRow row;
        OptionDescription option;
        boolean inEditMode;

        public InplaceEditor(EditorListener editorListener, OptionDescription option, TableRow row)
        {
            this.row = row;
            this.option = option;
            inEditMode = false;
            this.editorListener = editorListener;

            tvDesc = row.findViewById(R.id.lbStepName);
            tvPoints = row.findViewById(R.id.lbStepPoints);
            edDesc = row.findViewById(R.id.edStepDesc);
            edPoints = row.findViewById(R.id.edStepPoints);
            btnSave = row.findViewById(R.id.btnSaveOption);
            btnSave.setOnClickListener(this);
            btnRemove = row.findViewById(R.id.btnRemoveOption);
            btnRemove.setOnClickListener(new RemoveRowListener(option));
        }

        @Override
        public void onClick(View v)
        {
            if (!inEditMode)
                toEditMode();
            else
                toDisplayMode();
        }

        public void toDisplayMode()
        {
            btnSave.setVisibility(View.GONE);
            btnRemove.setVisibility(View.VISIBLE);

            option.setDescription(edDesc.getText().toString());
            tvDesc.setText(option.getDescription());

            option.setPoints(Integer.parseInt(edPoints.getText().toString()));
            tvPoints.setText(option.getPoints().toString());

            edDesc.setVisibility(View.GONE);
            edPoints.setVisibility(View.GONE);
            tvDesc.setVisibility(View.VISIBLE);
            tvPoints.setVisibility(View.VISIBLE);

            editorListener.onEditFinish(this);
            setItems(template.getOptionsAsListAllSteps());
            row.setBackgroundColor(Color.WHITE);
            makeTable();
            inEditMode = false;
        }

        public void toEditMode()
        {
            editorListener.onEditStart(this);

            btnSave.setVisibility(View.VISIBLE);
            btnRemove.setVisibility(View.GONE);

            edDesc.setText(option.getDescription());
            edPoints.setText(option.getPoints().toString());

            edDesc.setVisibility(View.VISIBLE);
            edPoints.setVisibility(View.VISIBLE);
            tvDesc.setVisibility(View.GONE);
            tvPoints.setVisibility(View.GONE);
            row.setBackgroundColor(getContext().getColor(R.color.blue_worm15));
            inEditMode = true;
        }

        public void cancel()
        {
            btnSave.setVisibility(View.GONE);
            btnRemove.setVisibility(View.VISIBLE);
            edDesc.setVisibility(View.GONE);
            edPoints.setVisibility(View.GONE);
            tvDesc.setVisibility(View.VISIBLE);
            tvPoints.setVisibility(View.VISIBLE);
            editorListener.onEditFinish(this);
            setItems(template.getOptionsAsListAllSteps());
            makeTable();
            inEditMode = false;
        }
    }

    /**
     * Callback for the placeholder. The callback add new option to the list
     */
    class PlaceholderClickListener implements View.OnClickListener
    {
        EditorListener editorListener;

        public PlaceholderClickListener(EditorListener editorListener)
        {
            this.editorListener = editorListener;
        }

        @Override
        public void onClick(View v)
        {
            if (editorListener.getCurrent() != null)
                editorListener.getCurrent().cancel();

            OptionDescription op = new OptionDescription();
            op.setPoints(1);
            op.setDescription(getContext().getString(R.string.txtAddDescription));
            template.addOption(op);
            setItems(template.getOptionsAsListAllSteps());
            makeTable();
        }
    }

    class EditorListener
    {
        InplaceEditor current;

        public void onEditStart(InplaceEditor editor)
        {
            if (current != null && current != editor)
                current.cancel();
            current = editor;
        }

        public void onEditFinish(InplaceEditor editor)
        {
            if (current == editor)
                current = null;
        }

        public InplaceEditor getCurrent()
        {
            return current;
        }
    }

    private class RemoveRowListener implements View.OnClickListener
    {
        OptionDescription option;

        public RemoveRowListener(OptionDescription option)
        {
            this.option = option;
        }

        @Override
        public void onClick(View v)
        {
            if (editorListener.getCurrent() != null)
                editorListener.getCurrent().cancel();
            template.removeOption(option);
            setItems(template.getOptionsAsListAllSteps());
            makeTable();
        }
    }
}