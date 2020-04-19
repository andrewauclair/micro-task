// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.ExistingTaskListName;
import picocli.CommandLine;

public class ExistingTaskListNameTypeConverter implements CommandLine.ITypeConverter<ExistingTaskListName> {

	private final Tasks tasks;

	public ExistingTaskListNameTypeConverter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public ExistingTaskListName convert(String value) throws Exception {
		try {
			return new ExistingTaskListName(tasks, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
