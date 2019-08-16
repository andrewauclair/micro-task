// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.Task;
import com.andrewauclair.todo.task.Tasks;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.time.*;
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

		List<Task> tasks = TimesCommand.getTasksForDay(instant, osInterface, this.tasks);

		ZoneId zoneId = osInterface.getZoneId();

		LocalDate today = LocalDate.ofInstant(instant, zoneId);
		LocalDateTime midnight = LocalDateTime.of(today, LocalTime.MIDNIGHT);
		LocalDateTime nextMidnight = midnight.plusDays(1);

		long midnightStart = midnight.atZone(zoneId).toEpochSecond();
		long midnightStop = nextMidnight.atZone(zoneId).toEpochSecond();

		// total the task times and determine how much time is left, then add that to the current seconds
		long totalTime = 0;

		for (Task task : tasks) {
			totalTime += TimesCommand.getTotalTimeInDay(task, midnightStart, midnightStop, osInterface);
		}

		output.print("End of Day is ");

		int hours = Integer.parseInt(argsMap.get("hours").getValue());
		long eod = epochSecond + ((hours * 3600) - totalTime);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss a");
		String eodStr = Instant.ofEpochSecond(eod).atZone(zoneId).format(dateTimeFormatter);

		output.println(eodStr);
		output.println();
	}

	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Collections.emptyList();
	}
}
