// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.ConsoleColors;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class Commands {
	private static final int MAX_DISPLAYED_TASKS = 20;
	private final Tasks tasks;
	private final PrintStream output;
	private final Map<String, Command> commands = new HashMap<>();

	private boolean debugEnabled = false;

	Commands(Tasks tasks, PrintStream output) {
		this.tasks = tasks;
		this.output = output;

		commands.put("finish", command -> finishCommand());
		commands.put("start", this::startCommand);
		commands.put("stop", command -> stopCommand());
		commands.put("add", this::addCommand);
		commands.put("active", command -> activeCommand());
		commands.put("list", this::listCommand);
		commands.put("times", this::timesCommand);
		commands.put("debug", this::debugCommand);
		commands.put("create-list", this::listCreateCommand);
		commands.put("switch-list", this::listSwitchCommand);
		commands.put("rename", this::renameCommand);
		commands.put("search", this::searchCommand);
		commands.put("clear", command -> tasks.osInterface.clearScreen());
		commands.put("exit", command -> tasks.osInterface.exit());
	}

	private void searchCommand(String command) {
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

	private void renameCommand(String command) {
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

	private void finishCommand() {
		Task task = tasks.finishTask();

		output.println("Finished task " + task.description());
		output.println();
		output.print("Task finished in: ");
		output.println(new TaskDuration(task.getTimes()));
		output.println();
	}

	private void startCommand(String command) {
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

	private void stopCommand() {
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

	private void addCommand(String command) {
		String taskTitle = command.substring(5, command.lastIndexOf('"'));

		Task task = tasks.addTask(taskTitle);

		output.println("Added task " + task.description());
		output.println();
	}

	private void activeCommand() {
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

	private void listCommand(String command) {
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
					.forEach(this::printList);
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
					.forEach(this::printTask);

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

	private void printList(String list) {
		if (list.equals(tasks.getCurrentList())) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, list);
		}
		else {
			output.print("  ");
			output.println(list);
		}
	}

	private void printTask(Task task) {
		if (task.id == tasks.getActiveTaskID()) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleForegroundColor.ANSI_FG_GREEN, task.description());
		}
		else {
			output.print("  ");
			output.println(task.description());
		}
	}

	private void timesCommand(String command) {
		String[] s = command.split(" ");

		// TODO Usage output if format is wrong, can we regex the format or something to verify it?
//		if (s.length != 2) {
//			output.println("Invalid command.");
//			output.println();
//			return;
//		}

		if (s[1].equals("--list")) {
			String list = s[2];
			output.println("Times for list '" + list + "'");
			output.println();

			long totalTime = 0;
			for (Task task : tasks.getTasksForList(list)) {
				for (TaskTimes time : task.getTimes()) {
					totalTime += getTotalTime(time);
				}
			}

			output.print("Total time spent on list: ");

			printTotalTime(totalTime);

			output.println();
		}
		else if (s[1].equals("--task")) {
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

					printTotalTime(totalTime);
				}
			}
			else {
				output.println("Task not found.");
			}
			output.println();
		}
		else {
			output.println("Invalid command.");
			output.println();
		}
	}

	private long getTotalTime(TaskTimes time) {
		long totalTime = time.getDuration();

		if (time.stop == TaskTimes.TIME_NOT_SET) {
			totalTime += tasks.osInterface.currentSeconds() - time.start;
		}
		return totalTime;
	}

	private void printTotalTime(long totalTime) {
		long hours = totalTime / (60 * 60);
		long minutes = (totalTime - (hours * 60 * 60)) / 60;
		long seconds = (totalTime - (hours * 60 * 60) - (minutes * 60));

		output.println(String.format("%02dh %02dm %02ds", hours, minutes, seconds));
	}

	private void debugCommand(String command) {
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

	private void listCreateCommand(String command) {
		String[] s = command.split(" ");

		if (s.length != 2) {
			output.println("Invalid command.");
			output.println();
			return;
		}

		String list = s[1].toLowerCase();

		boolean added = tasks.addList(list);

		if (added) {
			output.println("Created new list '" + list + "'");
		}
		else {
			output.println("List '" + list + "' already exists.");
		}
		output.println();
	}

	private void listSwitchCommand(String command) {
		String[] s = command.split(" ");

		if (s.length != 2) {
			output.println("Invalid command.");
			output.println();
			return;
		}

		String list = s[1].toLowerCase();

		boolean exists = tasks.setCurrentList(list);

		if (exists) {
			output.println("Switched to list '" + list + "'");
		}
		else {
			output.println("List '" + list + "' does not exist.");
		}
		output.println();
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	public void execute(String command) {
		try {
			commands.keySet().stream()
					.filter(command::startsWith)
					.findFirst()
					.ifPresentOrElse(name -> commands.get(name).execute(command),
							() -> {
								output.println("Unknown command.");
								output.println();
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

	private interface Command {
		void execute(String command);
	}
}
