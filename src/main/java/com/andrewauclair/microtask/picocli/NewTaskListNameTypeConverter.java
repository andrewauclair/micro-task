// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.NewTaskListName;
import picocli.CommandLine;

public class NewTaskListNameTypeConverter implements CommandLine.ITypeConverter<NewTaskListName> {
	private final Tasks tasks;

	public NewTaskListNameTypeConverter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public NewTaskListName convert(String value) {
		try {
			return new NewTaskListName(tasks, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
