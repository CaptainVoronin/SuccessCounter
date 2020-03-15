package org.max.successcounter.model.excercise;

public enum ExerciseFactory
{
    instance;

    public IExercise makeExercise(Template template)
    {
        IExercise exs;

        switch (template.getExType())
        {
            case compound:
                exs = new CompoundExercise(template);
                for (OptionDescription od : template.getOptionsAsList())
                    ((CompoundExercise) exs).addOption(makeOption(od));
                break;
            case simple:
                exs = new BaseExercise(template);
                break;
            case series:
                exs = new SeriesExercise(template);
                break;
            default:
                throw new IllegalArgumentException("Unknown exercise type");
        }

        return exs;
    }

    CompoundExercise.Option makeOption(OptionDescription od)
    {
        CompoundExercise.Option op = new CompoundExercise.Option();
        op.setDescription(od.getDescription());
        op.setPoints(od.getPoints());
        op.setColor(od.getColor());
        return op;
    }
}
