package org.mi.plannitybe.schedule.converter;

import jakarta.persistence.Converter;
import org.mi.plannitybe.common.converter.CodeConverter;
import org.mi.plannitybe.schedule.type.TaskStatusType;

@Converter(autoApply = true)
public class TaskStatusConverter extends CodeConverter<TaskStatusType> {

    public TaskStatusConverter() {
        super(TaskStatusType.class, TaskStatusType::getCode, true);
    }
}