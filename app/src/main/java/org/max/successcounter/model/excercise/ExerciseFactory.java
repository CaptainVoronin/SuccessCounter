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
                exs = new CompoundExcercise();
                ((CompoundExcercise) exs).setHasSummaryStep(template.isHasSummaryStep());
                for (OptionDescription od : template.getOptionsAsList())
                    ((CompoundExcercise) exs).addOption(makeOption(od));

                // If the template doesn't have the summary option then remove it (it's the last one)
                if (!template.isHasSummaryStep())
                    ((CompoundExcercise) exs).getOptions().remove(((CompoundExcercise) exs).getOptions().size() - 1);
                break;
            case simple:
                if (template.getLimited())
                    exs = new RunToExcercise(template.getLimit());
                else
                    exs = new SimpleExercise();
                exs.setName(template.getName());
                exs.setTemplate(template);
                break;
            case series:
                exs = new SeriesExercise();
                break;
            default:
                throw new IllegalArgumentException("Unknown exercise type");
        }
        return exs;
    }

    CompoundExcercise.Option makeOption(OptionDescription od)
    {
        CompoundExcercise.Option op = new CompoundExcercise.Option();
        op.setDescription(od.getDescription());
        op.setPoints(od.getPoints());
        return op;
    }
}
