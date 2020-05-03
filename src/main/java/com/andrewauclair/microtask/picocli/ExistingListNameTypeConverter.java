// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.Tasks;
import com.andrewauclair.microtask.task.list.name.ExistingListName;
import picocli.CommandLine;

public class ExistingListNameTypeConverter implements CommandLine.ITypeConverter<ExistingListName> {

	private final Tasks tasks;

	public ExistingListNameTypeConverter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public ExistingListName convert(String value) {
		try {
			return new ExistingListName(tasks, value);
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
