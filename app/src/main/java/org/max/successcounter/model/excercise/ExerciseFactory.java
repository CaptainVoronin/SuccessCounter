package org.max.successcounter.model.excercise;

public enum ExerciseFactory
{
    instance;

    public IExercise makeExercise(Template template)
    {
        IExercise exs;

        if (template.getCompound() )
        {
            exs = new CompoundExcercise();
            for( OptionDescription od : template.getOptions() )
                ((CompoundExcercise)exs).addOption( makeOption( od ) );
        }
        else
        {
            if (template.getLimited())
                exs = new RunToExcercise(template.getLimit());
            else
                exs = new SimpleExercise();
        }
        exs.setName( template.getName() );
        exs.setTemplate( template );
        return exs;
    }

    CompoundExcercise.Option makeOption( OptionDescription od )
    {
        CompoundExcercise.Option op = new CompoundExcercise.Option();
        op.setDescription( od.getDescription() );
        op.setPoints( od.getPoints() );
        return op;
    }
}
