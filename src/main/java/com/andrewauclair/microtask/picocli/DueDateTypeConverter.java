// Copyright (C) 2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.picocli;

import com.andrewauclair.microtask.DueDate;
import com.andrewauclair.microtask.os.OSInterface;
import picocli.CommandLine;

import java.time.LocalDate;
import java.time.MonthDay;
import java.time.Period;
import java.time.format.DateTimeParseException;

public class DueDateTypeConverter implements CommandLine.ITypeConverter<DueDate> {
	private final OSInterface osInterface;

	public DueDateTypeConverter(OSInterface osInterface) {
		this.osInterface = osInterface;
	}

	@Override
	public DueDate convert(String value) throws Exception {
		try {
			return new DueDate(osInterface, Period.parse(value));
		}
		catch (DateTimeParseException ignore) {
		}

		try {
			return new DueDate(osInterface, MonthDay.parse(value));
		}
		catch (DateTimeParseException ignore) {
		}

		try {
			return new DueDate(osInterface, LocalDate.parse(value));
		}
		catch (DateTimeParseException ignore) {
		}

		throw new CommandLine.TypeConversionException("Failed to parse DueDate as Period, MonthDay or LocalDate");
	}
}
