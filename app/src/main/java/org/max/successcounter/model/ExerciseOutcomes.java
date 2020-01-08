package org.max.successcounter.model;

import android.app.Activity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import org.max.successcounter.AbstractTableAdapter;
import org.max.successcounter.R;
import org.max.successcounter.model.excercise.OptionDescription;
import org.max.successcounter.model.excercise.Template;

public class ExerciseOutcomes extends AbstractTableAdapter<OptionDescription>
{
    EditorListener editorListener;
    Template template;

    public ExerciseOutcomes(Activity ctx, int view_id, Template template )
    {
        super(ctx, view_id, R.layout.compound_default_outcome, template.getOptionsAsList());
        editorListener = new EditorListener();
        this.template = template;
    }

    @Override
    public void makeTable()
    {
        super.makeTable();
        getTable().addView( makePlaceholder(), getItems().size() - 1 );
    }

    @Override
    protected TableRow makeRow(OptionDescription item)
    {
        if( item.getFirstDefault() )
            return makeUneditableRow( item );
        else if( item.getLastDefault() )
            return makeSummaryRow( item );
        else
            return makeGeneralRow( item );
    }

    private TableRow makeSummaryRow(OptionDescription item)
    {
        TextView tvStepName;
        TextView tvStepPoints;
        ImageButton btnRemove;

        TableRow row = ( TableRow ) getContext().getLayoutInflater().inflate( getRow_layout_id(), null, false );
        tvStepName = row.findViewById(R.id.lbStepName);
        tvStepPoints = row.findViewById(R.id.lbStepPoints);
        btnRemove = row.findViewById(R.id.btnRemoveStep);
        btnRemove.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                template.setHasSummaryStep( false );
                makeTable();
            }
        });

        if( template.isHasSummaryStep() )
        {
            tvStepName.setText(item.getDescription());
            tvStepPoints.setVisibility( View.VISIBLE );
            tvStepPoints.setText(item.getPoints().toString());
            btnRemove.setVisibility(View.VISIBLE);
        }
        else
        {
            tvStepPoints.setVisibility( View.INVISIBLE );
            tvStepName.setText( getContext().getString( R.string.messageRestoreSummaryStep ));
            tvStepName.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    template.setHasSummaryStep( true );
                    makeTable();
                }
            });
            btnRemove.setVisibility(View.INVISIBLE);
        }

        return row;
    }

    private TableRow makeUneditableRow(OptionDescription item)
    {
        TableRow row = ( TableRow ) getContext().getLayoutInflater().inflate( getRow_layout_id(), null, false );
        TextView tv = row.findViewById(R.id.lbStepName);
        tv.setText( item.getDescription() );

        tv = row.findViewById(R.id.lbStepPoints);
        tv.setText(item.getPoints().toString());

        ImageButton btnRemove;
        btnRemove = row.findViewById( R.id.btnRemoveStep );
        btnRemove.setVisibility( View.INVISIBLE );
        return row;
    }

    private TableRow makeGeneralRow(OptionDescription item)
    {
        TableRow row = ( TableRow ) getContext().getLayoutInflater().inflate( getRow_layout_id(), null, false );
        InplaceEditor editor = new InplaceEditor( editorListener, item, row );
        TextView tv = row.findViewById(R.id.lbStepName);
        tv.setText( item.getDescription() );
        tv.setOnClickListener( editor );

        tv = row.findViewById(R.id.lbStepPoints);
        tv.setText(item.getPoints().toString());
        tv.setOnClickListener( editor );

        /*ImageButton btnRemove;
        btnRemove = row.findViewById( R.id.btnRemoveStep );*/

        return row;

    }

    private TableRow makePlaceholder()
    {
        TableRow tr = (TableRow) getContext().getLayoutInflater().inflate(R.layout.add_step_placeholder, null, false);
        ImageButton btn = tr.findViewById(R.id.btnAddStep);
        btn.setOnClickListener(new PlaceholderClickListener ( editorListener ) );
        return tr;
    }

    private class InplaceEditor implements View.OnClickListener
    {
        TextView tvDesc, tvPoints;
        EditText edDesc, edPoints;
        ImageButton btnSave, btnRemove;
        ViewSwitcher swcDesc, swcPoints;
        EditorListener editorListener;

        TableRow row;
        OptionDescription option;
        boolean inEditMode;

        public InplaceEditor( EditorListener editorListener, OptionDescription option, TableRow row )
        {
            this.row = row;
            this.option = option;
            inEditMode = false;
            this.editorListener = editorListener;

            tvDesc = row.findViewById( R.id.lbStepName );
            tvPoints = row.findViewById( R.id.lbStepPoints );
            edDesc = row.findViewById( R.id.edStepDesc);
            edPoints = row.findViewById( R.id.edStepPoints );
            swcDesc = row.findViewById( R.id.swcDescription );
            swcPoints = row.findViewById( R.id.swcPoints );
            btnSave = row.findViewById( R.id.btnSave );
            btnSave.setOnClickListener( this );

            btnRemove = row.findViewById( R.id.btnRemoveStep );
            btnRemove.setOnClickListener( new RemoveRowListener( option ) );
        }

        @Override
        public void onClick(View v)
        {

            if( !inEditMode )
                toEditMode();
            else
                toDisplayMode();
         }

        public void toDisplayMode()
        {
            swcDesc.showNext();
            swcPoints.showNext();

            btnSave.setVisibility( View.GONE );
            btnRemove.setVisibility( View.VISIBLE );

            option.setDescription( edDesc.getText().toString() );
            tvDesc.setText( option.getDescription() );

            option.setPoints( Integer.parseInt( edPoints.getText().toString() ) );
            tvPoints.setText( option.getPoints().toString() );

            editorListener.onEditFinish( this );
            setItems( template.getOptionsAsList() );
            makeTable();
            inEditMode = false;
        }

        public void toEditMode()
        {
            swcDesc.showNext();
            swcPoints.showNext();

            editorListener.onEditStart( this );

            btnSave.setVisibility( View.VISIBLE);
            btnRemove.setVisibility( View.GONE );

            edDesc.setText( option.getDescription() );
            edPoints.setText( option.getPoints().toString() );

            inEditMode = true;
        }
    }

    class PlaceholderClickListener implements View.OnClickListener
    {
        EditorListener editorListener;

        public PlaceholderClickListener( EditorListener editorListener )
        {
            this.editorListener = editorListener;
        }

        @Override
        public void onClick(View v)
        {
            OptionDescription op = new OptionDescription();
            op.setPoints( 1 );
            op.setDescription( "Enter description" );
            template.addOption( op );
            setItems( template.getOptionsAsList() );
            makeTable();
        }
    }

    class EditorListener
    {
        InplaceEditor current;

        public void onEditStart( InplaceEditor editor )
        {
            if( current != null && current != editor )
                current.toDisplayMode();
            current = editor;
        }

        public void onEditFinish( InplaceEditor editor )
        {
            if( current == editor )
                current = null;
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
            template.removeOption( option );
            setItems( template.getOptionsAsList() );
            makeTable();
        }
    }
}