// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.time;

import picocli.CommandLine.Option;

public class DayMonthYear {
	@Option(names = {"-d", "--day"}, description = "Day to display times for.")
	public Integer day;

	@Option(names = {"-m", "--month"}, description = "Month to display times for.")
	public Integer month;

	@Option(names = {"-y", "--year"}, description = "Year to display times for.")
	public Integer year;
}
