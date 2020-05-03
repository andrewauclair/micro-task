// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

public class ExistingIDTypeConverter implements CommandLine.ITypeConverter<ExistingID> {
	private final Tasks tasks;

	public ExistingIDTypeConverter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public ExistingID convert(String value) {
		try {
			return new ExistingID(tasks, Long.parseLong(value));
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
