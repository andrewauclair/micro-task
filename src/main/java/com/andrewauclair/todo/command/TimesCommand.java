// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ActiveListCompleter;
import com.andrewauclair.todo.jline.ActiveTaskCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.*;
import org.jline.builtins.Completers;

import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class TimesCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("tasks", CommandOption.NO_SHORTNAME, Collections.emptyList()),
			new CommandOption("summary", CommandOption.NO_SHORTNAME, Collections.emptyList()),
			new CommandOption("today", CommandOption.NO_SHORTNAME, Collections.emptyList()),
			new CommandOption("list", CommandOption.NO_SHORTNAME, Collections.singletonList("List")),
			new CommandOption("task", CommandOption.NO_SHORTNAME, Collections.singletonList("Task")),
			new CommandOption("day", 'd', Collections.singletonList("Day")),
			new CommandOption("month", 'm', Collections.singletonList("Month")),
			new CommandOption("year", 'y', Collections.singletonList("Year"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	private final OSInterface osInterface;

	public TimesCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}

	static void printTotalTime(PrintStream output, long totalTime, boolean printExtraSpace) {
		long hours = totalTime / (60 * 60);
		long minutes = (totalTime - (hours * 60 * 60)) / 60;
		long seconds = (totalTime - (hours * 60 * 60) - (minutes * 60));

		if (hours > 0) {
			output.print(String.format("%02dh ", hours));
		}
		else if (printExtraSpace) {
			output.print("    ");
		}

		if (minutes > 0 || hours > 0) {
			output.print(String.format("%02dm ", minutes));
		}
		else if (printExtraSpace) {
			output.print("    ");
		}

		output.print(String.format("%02ds", seconds));
	}

	static long getTotalTime(TaskTimes time, OSInterface osInterface) {
		long totalTime = time.getDuration();

		if (time.stop == TaskTimes.TIME_NOT_SET) {
			totalTime += osInterface.currentSeconds() - time.start;
		}
		return totalTime;
	}

	@Override
	public void execute(PrintStream output, String command) {
		List<CommandArgument> args = parser.parse(command);
		Map<String, CommandArgument> argsMap = new HashMap<>();

		for (CommandArgument arg : args) {
			argsMap.put(arg.getName(), arg);
		}

		String[] s = command.split(" ");

		List<String> parameters = Arrays.asList(s);

		// TODO Usage output if format is wrong, can we regex the format or something to verify it?
		if (s.length == 1) {
			output.println("Invalid command.");
			output.println();
			return;
		}

		if (s[1].equals("--list")) {
			String list = s[2];

			if (parameters.contains("--summary")) {
				output.println("Times summary for list '" + list + "'");
				output.println();

				List<Task> listTasks = new ArrayList<>(tasks.getTasksForList(list));

				listTasks.sort(Comparator.comparingLong(this::getTotalTaskTime).reversed());

				long totalTime = 0;

				for (Task task : listTasks) {
					long time = getTotalTaskTime(task);

					totalTime += time;

					if (time > 0) {
						printTotalTime(output, time, true);
						output.println("   " + task.description());
					}
				}
				output.println();
				printTotalTime(output, totalTime, false);
				output.println("     - Total Time");
				output.println();
			}
			else {
				output.println("Times for list '" + list + "'");
				output.println();

				long totalTime = 0;
				for (Task task : tasks.getTasksForList(list)) {
					for (TaskTimes time : task.getStartStopTimes()) {
						totalTime += getTotalTime(time, osInterface);
					}
				}

				output.print("Total time spent on list: ");

				printTotalTime(output, totalTime, false);

				output.println();
				output.println();
			}
		}
		else if (s[1].equals("--task") && !parameters.contains("--today")) {
			long taskID = Long.parseLong(s[2]);

			String list = tasks.findListForTask(taskID);
			Optional<Task> firstTask = tasks.getTasksForList(list).stream()
					.filter(task -> task.id == taskID)
					.findFirst();

			if (firstTask.isPresent()) {
				Task task = firstTask.get();

				if (task.getStartStopTimes().size() == 0) {
					output.println("No times for task " + task.description());
				}
				else {
					output.println("Times for task " + task.description());
					output.println();

					long totalTime = 0;
					for (TaskTimes time : task.getStartStopTimes()) {
						output.println(time.description(osInterface.getZoneId()));

						totalTime += getTotalTime(time, osInterface);
					}

					output.println();
					output.print("Total time: ");

					printTotalTime(output, totalTime, false);
					output.println();
				}
			}
			else {
				output.println("Task not found.");
			}
			output.println();
		}
		else if (s[1].equals("--tasks") && parameters.contains("--today")) {
			long epochSecond = osInterface.currentSeconds();

			Instant instant = Instant.ofEpochSecond(epochSecond);

			displayTimesForDay(output, instant);

			// TODO We should still use getTasksForList if --list was provided, going to skip that for now
		}
		else if (s[1].equals("--tasks")) {
			long epochSecond = osInterface.currentSeconds();

			Instant epochInstant = Instant.ofEpochSecond(epochSecond);

			ZoneId zoneId = osInterface.getZoneId();

			int day = Integer.parseInt(argsMap.get("day").getValue());
			int month = argsMap.get("month") != null ? Integer.parseInt(argsMap.get("month").getValue()) : epochInstant.atZone(zoneId).getMonthValue();
			int year = argsMap.get("year") != null ? Integer.parseInt(argsMap.get("year").getValue()) : epochInstant.atZone(zoneId).getYear();

			LocalDate of = LocalDate.of(year, month, day);


			Instant instant = of.atStartOfDay(zoneId).toInstant();

			displayTimesForDay(output, instant);
			// TODO We should still use getTasksForList if --list was provided, going to skip that for now
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}

	private void displayTimesForDay(PrintStream output, Instant day) {
		// get date and execute it
		output.print("Times for day ");

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		ZoneId zoneId = osInterface.getZoneId();

		output.println(day.atZone(zoneId).format(dateTimeFormatter));

		LocalDate today = LocalDate.ofInstant(day, zoneId);

		output.println();

		long totalTime = 0;

		// TODO We should still use getTasksForList if --list was provided, going to skip that for now
		List<TaskFilter.TaskFilterResult> data = new TaskFilter(tasks).filterForDay(today.getMonth().getValue(), today.getDayOfMonth(), today.getYear()).getData();

		data.sort(Comparator.comparingLong(TaskFilter.TaskFilterResult::getTotal).reversed());

		for (TaskFilter.TaskFilterResult result : data) {
			printTotalTime(output, result.getTotal(), true);

			Task task = result.getTask();

			if (tasks.getActiveTaskID() == task.id) {
				output.print(" * ");
				ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, task.description());
			}
			else if (task.state == TaskState.Finished) {
				output.print(" F ");
				output.println(task.description());
			}
			else if (task.isRecurring()) {
				output.print(" R ");
				output.println(task.description());
			}
			else {
				output.print("   ");
				output.println(task.description());
			}

			totalTime += result.getTotal();
		}

		output.println();
		output.print("Total time: ");
		printTotalTime(output, totalTime, false);
		output.println();
		output.println();
	}

	private long getTotalTaskTime(Task task) {
		long totalTime = 0;

		for (TaskTimes time : task.getStartStopTimes()) {
			totalTime += getTotalTime(time, osInterface);
		}
		return totalTime;
	}
	
	@Override
	public List<Completers.TreeCompleter.Node> getAutoCompleteNodes() {
		return Arrays.asList(
				node("times",
						node("--list",
								node(new ActiveListCompleter(tasks)),
								node("--today")
						)
				),
				node("times",
						node("--tasks",
								node(new ActiveTaskCompleter(tasks)),
								node("--today")
						)
				),
				node("times",
						node("--task"
						)
				)
		);
	}
}
