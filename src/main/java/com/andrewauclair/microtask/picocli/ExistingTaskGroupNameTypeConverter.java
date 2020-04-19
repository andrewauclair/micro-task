// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingTaskGroupName;
import picocli.CommandLine;

public class ExistingTaskGroupNameTypeConverter implements CommandLine.ITypeConverter<ExistingTaskGroupName> {

	private final Tasks tasks;

	public ExistingTaskGroupNameTypeConverter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public ExistingTaskGroupName convert(String value) throws Exception {
		try {
			return new ExistingTaskGroupName(tasks, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
