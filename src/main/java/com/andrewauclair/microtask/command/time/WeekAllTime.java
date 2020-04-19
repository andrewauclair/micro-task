// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.time;

import picocli.CommandLine.Option;

public class WeekAllTime {
	@Option(names = {"--week"}, description = "Week to display times for.")
	public boolean week;

	@Option(names = {"--all-month"}, description = "Display times for the entire month")
	public boolean all_month;
}
