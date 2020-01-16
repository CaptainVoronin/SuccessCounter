package org.max.successcounter.model.excercise;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Step implements IStep
{
    Float percent;
    Integer points;
    IExercise exercise;
    Integer id;
}
