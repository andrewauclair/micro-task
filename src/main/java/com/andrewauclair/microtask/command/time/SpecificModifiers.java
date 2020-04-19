// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.time;

import picocli.CommandLine.ArgGroup;

public class SpecificModifiers {
	@ArgGroup(exclusive = false)//, multiplicity = "1")
	public DayMonthYear dmy;

	@ArgGroup()
	public WeekAllTime weekAllTime;
}
