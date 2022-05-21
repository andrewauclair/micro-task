// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.IDValidator;
import com.andrewauclair.microtask.task.NewID;
import com.andrewauclair.microtask.task.Tasks;
import picocli.CommandLine;

public class NewIDTypeConverter implements CommandLine.ITypeConverter<NewID> {
	private final IDValidator idValidator;

	public NewIDTypeConverter(IDValidator idValidator) {
		this.idValidator = idValidator;
	}

	@Override
	public NewID convert(String value) {
		try {
			return new NewID(idValidator, Long.parseLong(value));
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
