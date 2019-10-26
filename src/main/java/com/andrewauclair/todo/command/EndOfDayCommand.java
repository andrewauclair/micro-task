// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.TaskFilter;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

class EndOfDayCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("hours", 'h', Collections.singletonList("Hours"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	private final OSInterface osInterface;

	EndOfDayCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	@Override
	public void execute(PrintStream output, String command) {
		List<CommandArgument> args = parser.parse(command);
		Map<String, CommandArgument> argsMap = new HashMap<>();

		for (CommandArgument arg : args) {
			argsMap.put(arg.getName(), arg);
		}

		if (!argsMap.containsKey("hours")) {
			output.println("Missing hours argument.");
			output.println();
			return;
		}

		long epochSecond = osInterface.currentSeconds();

		Instant instant = Instant.ofEpochSecond(epochSecond);

		ZoneId zoneId = osInterface.getZoneId();

		LocalDate today = LocalDate.ofInstant(instant, zoneId);

		List<TaskFilter.TaskFilterResult> data = new TaskFilter(this.tasks).filterForDay(today.getMonth().getValue(), today.getDayOfMonth(), today.getYear()).getData();

		// total the task times and determine how much time is left, then add that to the current seconds
		long totalTime = 0;

		for (TaskFilter.TaskFilterResult result : data) {
			totalTime += result.getTotal();
		}

		output.print("End of Day is in ");


		int hours = Integer.parseInt(argsMap.get("hours").getValue());
		long eod = epochSecond + ((hours * 3600) - totalTime);

		TimesCommand.printTotalTime(output, eod - epochSecond, false);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
		String eodStr = Instant.ofEpochSecond(eod).atZone(zoneId).format(dateTimeFormatter);

		output.print(" at ");
		output.println(eodStr);
		output.println();
	}

	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.emptyList();
	}
}
