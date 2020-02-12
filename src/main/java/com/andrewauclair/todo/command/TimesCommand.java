// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.command;

import com.andrewauclair.todo.jline.ActiveListCompleter;
import com.andrewauclair.todo.jline.ActiveTaskCompleter;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.OSInterface;
import com.andrewauclair.todo.task.*;
import org.jline.builtins.Completers;
import picocli.CommandLine;

import java.io.PrintStream;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.andrewauclair.todo.os.ConsoleColors.ANSI_RESET;
import static com.andrewauclair.todo.os.ConsoleColors.ANSI_REVERSED;
import static org.jline.builtins.Completers.TreeCompleter.node;

@CommandLine.Command(name = "times")
public class TimesCommand extends Command {
	@CommandLine.Option(names = {"--tasks"})
	private boolean tasks;

	@CommandLine.Option(names = {"--summary"})
	private boolean summary;

	@CommandLine.Option(names = {"--today"})
	private boolean today;

	@CommandLine.Option(names = {"--proj-feat"})
	private boolean proj_feat;

	@CommandLine.Option(names = {"--list"})
	private String list;

	@CommandLine.Option(names = {"--task"})
	private Integer id;

	@CommandLine.Option(names = {"-d", "--day"})
	private Integer day;

	@CommandLine.Option(names = {"-m", "--month"})
	private Integer month;

	@CommandLine.Option(names = {"-y", "--year"})
	private Integer year;

	private final Tasks tasksData;
	private final OSInterface osInterface;
	private TaskFilterBuilder taskFilterBuilder;
	
	TimesCommand(Tasks tasks, OSInterface osInterface, TaskFilterBuilder taskFilterBuilder) {
		this.tasksData = tasks;
		this.osInterface = osInterface;
		this.taskFilterBuilder = taskFilterBuilder;
	}
	
	public void replaceTaskFilterBuilder(TaskFilterBuilder taskFilterBuilder) {
		this.taskFilterBuilder = taskFilterBuilder;
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
			
			if (tasksData.getActiveTaskID() == task.id) {
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
	public void run() {
		if (list != null && id == null && day == null && !summary && !today){// && result.getArgCount() == 1) {
			String list = this.list;

			System.out.println("Times for list '" + list + "'");
			System.out.println();

			long totalTime = 0;
			for (Task task : tasksData.getTasksForList(list)) {
				for (TaskTimes time : task.getStartStopTimes()) {
					totalTime += time.getDuration(osInterface);
				}
			}

			System.out.print("Total time spent on list: ");

			printTotalTime(System.out, totalTime, false);

			System.out.println();
			System.out.println();
		}
		else if (summary && this.list != null){// && result.getArgCount() == 2) {
			String list = this.list;

			System.out.println("Times summary for list '" + list + "'");
			System.out.println();

			List<Task> listTasks = tasksData.getTasksForList(list).stream()
					.sorted(Comparator.comparingLong(o -> ((Task) o).getElapsedTime(osInterface)).reversed())
					.filter(task -> task.getElapsedTime(osInterface) > 0)
					.collect(Collectors.toList());

			long totalTime = 0;

			for (Task task : listTasks) {
				long time = task.getElapsedTime(osInterface);

				totalTime += time;

				printTotalTime(System.out, time, true);
				System.out.println("   " + task.description());
			}

			System.out.println();
			printTotalTime(System.out, totalTime, false);
			System.out.println("     - Total Time");
			System.out.println();
		}
		else if (id != null && !today){
			long taskID = id;

			if (tasksData.hasTaskWithID(taskID)) {
				Task task = tasksData.getTask(taskID);

				if (task.getStartStopTimes().size() == 0) {
					System.out.println("No times for task " + task.description());
				}
				else {
					System.out.println("Times for task " + task.description());
					System.out.println();

					long totalTime = 0;
					for (TaskTimes time : task.getStartStopTimes()) {
						System.out.println(time.description(osInterface.getZoneId()));

						totalTime += time.getDuration(osInterface);
					}

					System.out.println();
					System.out.print("Total time: ");

					printTotalTime(System.out, totalTime, false);
					System.out.println();
				}
			}
			else {
				System.out.println("Task does not exist.");
			}
			System.out.println();
		}
		else if (tasks && today) {
			long epochSecond = osInterface.currentSeconds();

			Instant instant = Instant.ofEpochSecond(epochSecond);

			TaskFilter filter = taskFilterBuilder.createFilter(tasksData);

			if (list != null) {
				filter.filterForList(list);
			}
			LocalDate day = LocalDate.ofInstant(instant, osInterface.getZoneId());

			filter.filterForDay(day.getMonth().getValue(), day.getDayOfMonth(), day.getYear());

			displayTimesForDay(System.out, instant, filter);
		}
		else if (tasks) {
			long epochSecond = osInterface.currentSeconds();

			Instant epochInstant = Instant.ofEpochSecond(epochSecond);

			ZoneId zoneId = osInterface.getZoneId();

			// if there's only times --tasks then print all time numbers
			if (list == null && day == null){//result.getArgCount() == 1) {
				System.out.println("Times");
				System.out.println();
				printTasks(System.out, new TaskFilter(tasksData));
			}
			else {
				int day = this.day;
				int month = this.month != null ? this.month : epochInstant.atZone(zoneId).getMonthValue();
				int year = this.year != null ? this.year : epochInstant.atZone(zoneId).getYear();

				LocalDate of = LocalDate.of(year, month, day);

				Instant instant = of.atStartOfDay(zoneId).toInstant();

				TaskFilter filter = taskFilterBuilder.createFilter(tasksData);

				if (list != null) {
					filter.filterForList(list);
				}

				filter.filterForDay(month, day, year);

				displayTimesForDay(System.out, instant, filter);
			}
		}
		else if (proj_feat) {
			TaskFilter filter = new TaskFilter(tasksData);

			Map<String, Long> totals = new HashMap<>();
			long totalTime = 0;

			for (Task task : filter.getTasks()) {
				String project = tasksData.getProjectForTask(task.id);
				String feature = tasksData.getFeatureForTask(task.id);

				if (project.isEmpty()) {
					project = "None";
				}
				if (feature.isEmpty()) {
					feature = "None";
				}
				String projfeat = project + " / " + feature;

				totals.put(projfeat, totals.getOrDefault(projfeat, 0L) + task.getElapsedTime(osInterface));
				totalTime += task.getElapsedTime(osInterface);
			}

			List<String> str = new ArrayList<>(totals.keySet());
			str.sort(String::compareTo);

			Optional<String> longest = str.stream()
					.max(Comparator.comparingInt(String::length));

			for (String s1 : str) {
				String projFeat = s1;
				if (projFeat.contains("None")) {
					projFeat = projFeat.replaceAll("None", ANSI_REVERSED + "None" + ANSI_RESET);
				}
				System.out.print(projFeat);
				System.out.print(String.join("", Collections.nCopies(longest.get().length() - s1.length(), " ")));
				System.out.print(" ");

				printTotalTime(System.out, totals.get(s1), true);
				System.out.println();
			}
			System.out.println();
			System.out.print("Total ");
			System.out.print(String.join("", Collections.nCopies(longest.get().length() - "Total".length(), " ")));
			printTotalTime(System.out, totalTime, true);
			System.out.println();
			System.out.println();
		}
		else {
			System.out.println("Invalid command.");
			System.out.println();
		}
	}
}
