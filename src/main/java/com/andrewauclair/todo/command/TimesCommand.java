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
import java.util.stream.Collectors;

import static org.jline.builtins.Completers.TreeCompleter.node;

public class TimesCommand extends Command {
	private final List<CommandOption> options = Arrays.asList(
			new CommandOption("tasks", CommandOption.NO_SHORTNAME, Collections.emptyList()),
			new CommandOption("summary", CommandOption.NO_SHORTNAME, Collections.emptyList()),
			new CommandOption("today", CommandOption.NO_SHORTNAME, Collections.emptyList()),
			new CommandOption("proj-feat", CommandOption.NO_SHORTNAME, Collections.emptyList()),
			new CommandOption("list", CommandOption.NO_SHORTNAME, Collections.singletonList("List")),
			new CommandOption("task", CommandOption.NO_SHORTNAME, Collections.singletonList("Task")),
			new CommandOption("day", 'd', Collections.singletonList("Day")),
			new CommandOption("month", 'm', Collections.singletonList("Month")),
			new CommandOption("year", 'y', Collections.singletonList("Year"))
	);
	private final CommandParser parser = new CommandParser(options);
	private final Tasks tasks;
	private final OSInterface osInterface;

	TimesCommand(Tasks tasks, OSInterface osInterface) {
		this.tasks = tasks;
		this.osInterface = osInterface;
	}
	
	public static void printTotalTime(PrintStream output, long totalTime, boolean printExtraSpace) {
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

				List<Task> listTasks = tasks.getTasksForList(list).stream()
						.sorted(Comparator.comparingLong(o -> ((Task) o).getElapsedTime(osInterface)).reversed())
						.filter(task -> task.getElapsedTime(osInterface) > 0)
						.collect(Collectors.toList());

				long totalTime = 0;

				for (Task task : listTasks) {
					long time = task.getElapsedTime(osInterface);

					totalTime += time;

					printTotalTime(output, time, true);
					output.println("   " + task.description());
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
						totalTime += time.getDuration(osInterface);
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

			if (tasks.hasTaskWithID(taskID)) {
				Task task = tasks.getTask(taskID);

				if (task.getStartStopTimes().size() == 0) {
					output.println("No times for task " + task.description());
				}
				else {
					output.println("Times for task " + task.description());
					output.println();

					long totalTime = 0;
					for (TaskTimes time : task.getStartStopTimes()) {
						output.println(time.description(osInterface.getZoneId()));

						totalTime += time.getDuration(osInterface);
					}

					output.println();
					output.print("Total time: ");

					printTotalTime(output, totalTime, false);
					output.println();
				}
			}
			else {
				output.println("Task does not exist.");
			}
			output.println();
		}
		else if (s[1].equals("--tasks") && parameters.contains("--today")) {
			long epochSecond = osInterface.currentSeconds();

			Instant instant = Instant.ofEpochSecond(epochSecond);

			TaskFilter filter = new TaskFilter(tasks);

			if (argsMap.containsKey("list")) {
				filter.filterForList(argsMap.get("list").getValue());
			}
			LocalDate day = LocalDate.ofInstant(instant, osInterface.getZoneId());

			filter.filterForDay(day.getMonth().getValue(), day.getDayOfMonth(), day.getYear());

			displayTimesForDay(output, instant, filter);
		}
		else if (s[1].equals("--tasks")) {
			long epochSecond = osInterface.currentSeconds();

			Instant epochInstant = Instant.ofEpochSecond(epochSecond);

			ZoneId zoneId = osInterface.getZoneId();
			
			// if there's only times --tasks then print all time numbers
			if (parameters.size() == 2) {
				output.println("Times");
				output.println();
				printTasks(output, new TaskFilter(tasks));
			}
			else {
				int day = Integer.parseInt(argsMap.get("day").getValue());
				int month = argsMap.get("month") != null ? Integer.parseInt(argsMap.get("month").getValue()) : epochInstant.atZone(zoneId).getMonthValue();
				int year = argsMap.get("year") != null ? Integer.parseInt(argsMap.get("year").getValue()) : epochInstant.atZone(zoneId).getYear();
				
				LocalDate of = LocalDate.of(year, month, day);
				
				
				Instant instant = of.atStartOfDay(zoneId).toInstant();
				
				TaskFilter filter = new TaskFilter(tasks);
				
				if (argsMap.containsKey("list")) {
					filter.filterForList(argsMap.get("list").getValue());
				}
				
				filter.filterForDay(month, day, year);
				
				displayTimesForDay(output, instant, filter);
			}
		}
		else if (s[1].equals("--proj-feat")) {
			TaskFilter filter = new TaskFilter(tasks);
			
			Map<String, Long> totals = new HashMap<>();
			
			for (Task task : filter.getTasks()) {
				String project = tasks.getProjectForTask(task.id);
				String feature = tasks.getFeatureForTask(task.id);
				
				if (project.isEmpty()) {
					project = "None";
				}
				if (feature.isEmpty()) {
					feature = "None";
				}
				String projfeat = project + " / " + feature;
				
				totals.put(projfeat, totals.getOrDefault(projfeat, 0L) + task.getElapsedTime(osInterface));
			}
			
			List<String> str = new ArrayList<>(totals.keySet());
			str.sort(String::compareTo);
			
			for (String s1 : str) {
				output.print(s1);
				output.print(" ");
				printTotalTime(output, totals.get(s1), true);
				output.println();
			}
			output.println();
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}

	private void displayTimesForDay(PrintStream output, Instant day, TaskFilter filter) {
		// get date and execute it
		output.print("Times for day ");

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

		ZoneId zoneId = osInterface.getZoneId();

		output.println(day.atZone(zoneId).format(dateTimeFormatter));
		output.println();

		long totalTime = 0;

		List<TaskFilter.TaskFilterResult> data = filter.getData();

		data.sort(Comparator.comparingLong(TaskFilter.TaskFilterResult::getTotal).reversed());
		
		totalTime = printResults(output, totalTime, data);
		
		output.println();
		output.print("Total time: ");
		printTotalTime(output, totalTime, false);
		output.println();
		output.println();
	}
	
	private long printResults(PrintStream output, long totalTime, List<TaskFilter.TaskFilterResult> data) {
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
		return totalTime;
	}
	
	private void printTasks(PrintStream output, TaskFilter filter) {
		long totalTime = 0;
		
		List<TaskFilter.TaskFilterResult> data = filter.getData();
		
		data.sort(Comparator.comparingLong(TaskFilter.TaskFilterResult::getTotal).reversed());
		
		totalTime = printResults(output, totalTime, data);
		
		output.println();
		output.print("Total time: ");
		printTotalTime(output, totalTime, false);
		output.println();
		output.println();
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
