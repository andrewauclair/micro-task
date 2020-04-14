// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.NewTaskGroupName;
import picocli.CommandLine;

public class NewTaskGroupNameTypeConverter implements CommandLine.ITypeConverter<NewTaskGroupName> {

	private final Tasks tasks;

	public NewTaskGroupNameTypeConverter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public NewTaskGroupName convert(String value) throws Exception {
		try {
			return new NewTaskGroupName(tasks, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
