// Copyright (C) 2020-2021 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.NewID;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

public class NewIDTypeConverter implements CommandLine.ITypeConverter<NewID> {
	private final Tasks tasks;

	public NewIDTypeConverter(Tasks tasks) {
		this.tasks = tasks;
	}

	@Override
	public NewID convert(String value) {
		try {
			return new NewID(tasks, Long.parseLong(value));
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
