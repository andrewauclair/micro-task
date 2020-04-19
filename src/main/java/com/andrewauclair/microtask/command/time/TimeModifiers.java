// Copyright (C) 2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.command.time;

import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Option;

public class TimeModifiers {
	@Option(names = {"--today"}, description = "Display times for today.")
	public boolean today;

	@Option(names = {"--yesterday"}, description = "Display times for yesterday.")
	public boolean yesterday;

	@Option(names = {"--all-time"}, description = "Display all task times recorded.")
	public boolean all_time;

	@ArgGroup(exclusive = false, multiplicity = "1")
	public SpecificModifiers specificModifiers;
}
