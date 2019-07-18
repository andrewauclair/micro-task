// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.command.ListCreateCommand;
import com.andrewauclair.todo.command.ListSwitchCommand;
import com.andrewauclair.todo.os.ConsoleColors;
import org.jline.builtins.Completers.TreeCompleter.Node;

import java.io.PrintStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Commands {
	private static final int MAX_DISPLAYED_TASKS = 20;
	private final Tasks tasks;
	private final Map<String, Command> commands = new HashMap<>();
	
	private final Map<String, com.andrewauclair.todo.command.Command> newCommands = new HashMap<>();
	
	private boolean debugEnabled = false;
	
	Commands(Tasks tasks) {
		this.tasks = tasks;
		
		newCommands.put("create-list", new ListCreateCommand(tasks));
		newCommands.put("switch-list", new ListSwitchCommand(tasks));
		
		commands.put("finish", this::finishCommand);
		commands.put("start", this::startCommand);
		commands.put("stop", this::stopCommand);
		commands.put("add", this::addCommand);
		commands.put("active", this::activeCommand);
		commands.put("list", this::listCommand);
		commands.put("times", this::timesCommand);
		commands.put("debug", this::debugCommand);
		commands.put("rename", this::renameCommand);
		commands.put("search", this::searchCommand);
		commands.put("clear", (output1, command) -> tasks.osInterface.clearScreen());
		commands.put("exit", (output1, command) -> tasks.osInterface.exit());
	}
	
	private void finishCommand(PrintStream output, String command) {
		String[] s = command.split(" ");

		Task task;

		if (s.length == 2) {
			task = tasks.finishTask(Long.parseLong(s[1]));
		}
		else {
			task = tasks.finishTask();
		}

		output.println("Finished task " + task.description());
		output.println();
		output.print("Task finished in: ");
		output.println(new TaskDuration(task.getTimes()));
		output.println();
	}
	
	private void startCommand(PrintStream output, String command) {
		String[] s = command.split(" ");
		int taskID = Integer.parseInt(s[1]);

		if (tasks.hasActiveTask()) {
			Task activeTask = tasks.getActiveTask();

			output.println("Stopped task " + activeTask.description());
			output.println();
		}

		Task task = tasks.startTask(taskID);

		output.println("Started task " + task.description());
		output.println();

		List<TaskTimes> times = task.getTimes();
		TaskTimes startTime = times.get(times.size() - 1);

		output.println(startTime.description(tasks.osInterface.getZoneId()));
		output.println();
	}
	
	private void stopCommand(PrintStream output, String command) {
		Task task = tasks.stopTask();

		output.println("Stopped task " + task.description());
		output.println();

		List<TaskTimes> times = task.getTimes();
		TaskTimes stopTime = times.get(times.size() - 1);

		output.println(stopTime.description(tasks.osInterface.getZoneId()));
		output.println();
		output.print("Task was active for: ");
		output.println(new TaskDuration(stopTime));
		output.println();
	}
	
	private void addCommand(PrintStream output, String command) {
		String taskTitle = command.substring(5, command.lastIndexOf('"'));

		Task task = tasks.addTask(taskTitle);

		output.println("Added task " + task.description());
		output.println();
	}
	
	private void activeCommand(PrintStream output, String command) {
		Task task = tasks.getActiveTask();

		output.println("Active task is " + task.description());
		output.println();
		output.println("Active task is on the '" + tasks.getActiveTaskList() + "' list");
		output.println();

		List<TaskTimes> times = task.getTimes();
		TaskTimes activeTime = times.get(times.size() - 1);

		activeTime = new TaskTimes(activeTime.start, tasks.osInterface.currentSeconds());
		output.print("Current time elapsed: ");
		output.println(new TaskDuration(activeTime));
		output.println();
	}
	
	private void listCommand(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		List<String> parameters = Arrays.asList(s);
		
		boolean all = parameters.contains("--all");
		boolean showTasks = parameters.contains("--tasks");
		boolean showLists = parameters.contains("--lists");
		
		String list = tasks.getCurrentList();
		
		if (parameters.contains("--list")) {
			list = parameters.get(parameters.indexOf("--list") + 1);
		}
		
		if (showLists) {
			tasks.getListNames().stream()
					.sorted()
					.forEach(str -> printList(output, str));
			output.println();
		}
		else if (showTasks) {
			if (!list.equals(tasks.getCurrentList())) {
				output.println("Tasks on list '" + list + "'");
				output.println();
			}
			
			List<Task> tasksList = tasks.getTasksForList(list).stream()
					.filter(task -> task.state != Task.TaskState.Finished)
					.collect(Collectors.toList());
			
			final int limit = all ? Integer.MAX_VALUE : MAX_DISPLAYED_TASKS;
			tasksList.stream()
					.limit(limit)
					.forEach(str -> printTask(output, str));

			if (tasksList.size() > limit) {
				output.println("(" + (tasksList.size() - MAX_DISPLAYED_TASKS) + " more tasks.)");
			}
			else if (tasksList.size() == 0) {
				output.println("No tasks.");
			}
			output.println();
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}
	
	private void timesCommand(PrintStream output, String command) {
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
					for (TaskTimes time : task.getTimes()) {
						totalTime += getTotalTime(time);
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
				
				if (task.getTimes().size() == 0) {
					output.println("No times for task " + task.description());
				}
				else {
					output.println("Times for task " + task.description());
					output.println();
					
					long totalTime = 0;
					for (TaskTimes time : task.getTimes()) {
						output.println(time.description(tasks.osInterface.getZoneId()));
						
						totalTime += getTotalTime(time);
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
			// get date and print it
			output.print("Times for day ");
			
			DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			
			long epochSecond = tasks.osInterface.currentSeconds();
			
			ZoneId zoneId = tasks.osInterface.getZoneId();
			
			output.println(Instant.ofEpochSecond(epochSecond).atZone(zoneId).format(dateTimeFormatter));
			
			LocalTime midnight = LocalTime.MIDNIGHT;
			LocalDate today = LocalDate.ofInstant(Instant.ofEpochSecond(epochSecond), zoneId);//LocalDate.now(zoneId);
			LocalDateTime todayMidnight = LocalDateTime.of(today, midnight);
			LocalDateTime tomorrowMidnight = todayMidnight.plusDays(1);
			
			long midnightStart = todayMidnight.atZone(zoneId).toEpochSecond();
			long midnightStop = tomorrowMidnight.atZone(zoneId).toEpochSecond();
			
			output.println();
			
			long totalTime = 0;
			
			// TODO We should still use getTasksForList if --list was provided, going to skip that for now
			List<Task> listTasks = new ArrayList<>(tasks.getAllTasks());//tasks.getTasksForList(list));
			
			listTasks.sort(Comparator.comparingLong(this::getTotalTaskTime).reversed());
			
			for (Task task : listTasks) {
				boolean include = false;
				long totalTaskTime = 0;
				
				for (TaskTimes time : task.getTimes()) {
					if (time.start >= midnightStart && time.stop < midnightStop) {
						include = true;
						totalTaskTime += getTotalTime(time);
					}
				}
				
				if (include) {
					printTotalTime(output, totalTaskTime, true);
					output.print("   ");
					output.println(task.description());
					
					totalTime += totalTaskTime;
				}
			}
			
			output.println();
			output.print("Total time: ");
			printTotalTime(output, totalTime, false);
			output.println();
			output.println();
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}
	
	private void debugCommand(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		if (s[1].equals("enable")) {
			debugEnabled = true;
		}
		else if (s[1].equals("disable")) {
			debugEnabled = false;
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}
	
	private void renameCommand(PrintStream output, String command) {
		String[] s = command.split(" ");
		
		List<String> parameters = Arrays.asList(s);
		
		if (parameters.contains("--list")) {
			String newName = command.substring(command.indexOf("\"") + 1, command.lastIndexOf("\""));
			tasks.renameList(s[2], newName);
			
			output.println("Renamed list '" + s[2] + "' to '" + newName + "'");
			output.println();
		}
		else if (parameters.contains("--task")) {
			String newName = command.substring(command.indexOf("\"") + 1, command.lastIndexOf("\""));
			long taskID = Long.parseLong(s[2]);
			
			Task task = tasks.renameTask(taskID, newName);
			
			output.println("Renamed task " + task.description());
			output.println();
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}
	
	private void searchCommand(PrintStream output, String command) {
		String searchText = command.substring(8, command.lastIndexOf("\""));
		
		List<Task> searchResults = tasks.getTasks().stream()
				.filter(task -> task.task.contains(searchText))
				.collect(Collectors.toList());
		
		output.println("Search Results (" + searchResults.size() + "):");
		output.println();
		
		for (Task task : searchResults) {
			output.println(task.description().replace(searchText, ConsoleColors.ANSI_BOLD + ConsoleColors.ANSI_REVERSED + searchText + ConsoleColors.ANSI_RESET));
		}
		output.println();
	}
	
	private void printList(PrintStream output, String list) {
		if (list.equals(tasks.getCurrentList())) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, list);
		}
		else {
			output.print("  ");
			output.println(list);
		}
	}

	private long getTotalTime(TaskTimes time) {
		long totalTime = time.getDuration();

		if (time.stop == TaskTimes.TIME_NOT_SET) {
			totalTime += tasks.osInterface.currentSeconds() - time.start;
		}
		return totalTime;
	}

	private long getTotalTaskTime(Task task) {
		long totalTime = 0;

		for (TaskTimes time : task.getTimes()) {
			totalTime += getTotalTime(time);
		}
		return totalTime;
	}
	
	private void printTask(PrintStream output, Task task) {
		if (task.id == tasks.getActiveTaskID()) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, task.description());
		}
		else {
			output.print("  ");
			output.println(task.description());
		}
	}
	
	private void printTotalTime(PrintStream output, long totalTime, boolean printExtraSpace) {
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

	public boolean isDebugEnabled() {
		return debugEnabled;
	}
	
	public void execute(PrintStream output, String command) {
		try {
			commands.keySet().stream()
					.filter(command::startsWith)
					.findFirst()
					.ifPresentOrElse(name -> commands.get(name).execute(output, command),
							() -> {
								Optional<String> newCommand = newCommands.keySet().stream()
										.filter(command::startsWith)
										.findFirst();
								
								if (newCommand.isPresent()) {
									newCommands.get(newCommand.get()).print(output, command);
								}
								else {
									output.println("Unknown command.");
									output.println();
								}
							});
		}
		catch (RuntimeException e) {
			output.println(e.getMessage());
			output.println();
		}
	}

	String getListName() {
		return tasks.getCurrentList();
	}

	boolean hasListWithName(String listName) {
		return tasks.hasListWithName(listName);
	}

	String getPrompt() {
		String prompt = tasks.getCurrentList() + " - ";
		try {
			prompt += tasks.getActiveTask().id;
		}
		catch (RuntimeException e) {
			prompt += "none";
		}
		return prompt + ">";
	}
	
	public List<Node> getAutoCompleteNodes() {
		List<Node> nodes = new ArrayList<>();
		
		for (com.andrewauclair.todo.command.Command value : newCommands.values()) {
			nodes.addAll(value.getAutoCompleteNodes());
		}
		
		return nodes;
	}
	
	private interface Command {
		void execute(PrintStream output, String command);
	}
}
