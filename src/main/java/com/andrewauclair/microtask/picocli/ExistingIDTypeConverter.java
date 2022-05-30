// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.task.ExistingID;
import com.andrewauclair.microtask.task.IDValidator;
import picocli.CommandLine;

public class ExistingIDTypeConverter implements CommandLine.ITypeConverter<ExistingID> {
	private final IDValidator idValidator;

	public ExistingIDTypeConverter(IDValidator idValidator) {
		this.idValidator = idValidator;
	}

	@Override
	public ExistingID convert(String value) {
		try {
			return new ExistingID(idValidator, Long.parseLong(value));
		}
		catch (RuntimeException e) {
			throw new CommandLine.TypeConversionException(e.getMessage());
		}
	}
}
