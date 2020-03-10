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
                exs = new CompoundExercise();
                ((CompoundExercise) exs).setHasSummaryStep(template.isHasSummaryStep());
                for (OptionDescription od : template.getOptionsAsList())
                    ((CompoundExercise) exs).addOption(makeOption(od));
                break;
            case simple:
                if (template.getLimited())
                    exs = new RunToExercise(template.getLimit());
                else
                    exs = new SimpleExercise();
                break;
            case series:
                exs = new SeriesExercise();
                break;
            default:
                throw new IllegalArgumentException("Unknown exercise type");
        }

        exs.setName(template.getName());
        exs.setTemplate(template);
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
