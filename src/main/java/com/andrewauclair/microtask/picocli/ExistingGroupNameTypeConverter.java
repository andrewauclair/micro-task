// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.group.name.ExistingGroupName;
import picocli.CommandLine;

public class ExistingGroupNameTypeConverter implements CommandLine.ITypeConverter<ExistingGroupName> {

	private final Tasks tasks;

	public ExistingGroupNameTypeConverter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public ExistingGroupName convert(String value) {
		try {
			return new ExistingGroupName(tasks, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
