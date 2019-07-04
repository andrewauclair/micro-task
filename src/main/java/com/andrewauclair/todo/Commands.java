// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.ConsoleColors;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
	}

	private void finishCommand() {
		Task task = tasks.finishTask();

		output.println("Finished task " + task.description());
		output.println();
		output.print("Task finished in: ");
		output.println(new TaskDuration(task.getTimes()));
	}

	private void startCommand(String command) {
		String[] s = command.split(" ");
		int taskID = Integer.parseInt(s[1]);

		Task task = tasks.startTask(taskID);

		output.println("Started task " + task.description());
		output.println();

		List<TaskTimes> times = task.getTimes();
		TaskTimes startTime = times.get(times.size() - 1);

		output.println(startTime.description(tasks.osInterface.getZoneId()));
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
	}

	private void addCommand(String command) {
		String taskTitle = command.substring(5, command.length() - 1);

		Task task = tasks.addTask(taskTitle);

		output.println("Added task " + task.description());
	}

	private void activeCommand() {
		Task task = tasks.getActiveTask();

		output.println("Active task is " + task.description());
		output.println();

		List<TaskTimes> times = task.getTimes();
		TaskTimes activeTime = times.get(times.size() - 1);

		activeTime = new TaskTimes(activeTime.start, tasks.osInterface.currentSeconds());
		output.print("Current time elapsed: ");
		output.println(new TaskDuration(activeTime));
	}

	private void listCommand(String command) {
		String[] s = command.split(" ");

		boolean all = false;
		boolean lists = false;
		if (s.length == 2 && s[1].equals("--all")) {
			all = true;
		}
		else if (s.length == 2 && s[1].equals("--lists")) {
			lists = true;
		}

		if (lists) {
			tasks.getListNames().stream()
					.sorted()
					.forEach(this::printList);
		}
		else {
			List<Task> tasksList = tasks.getTasks().stream()
					.filter(task -> task.state != Task.TaskState.Finished)
					.collect(Collectors.toList());

			final int limit = all ? Integer.MAX_VALUE : MAX_DISPLAYED_TASKS;
			tasksList.stream()
					.limit(limit)
					.forEach(this::printTask);

			if (tasksList.size() > limit) {
				output.println("(" + (tasksList.size() - MAX_DISPLAYED_TASKS) + " more tasks.)");
			}

			if (tasksList.size() == 0) {
				output.println("No tasks.");
			}
		}
	}

	private void printList(String list) {
		if (list.equals(tasks.getCurrentList())) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleColor.ANSI_GREEN, list);
		}
		else {
			output.print("  ");
			output.println(list);
		}
	}

	private void printTask(Task task) {
		if (task.id == tasks.getActiveTaskID()) {
			output.print("* ");
			ConsoleColors.println(output, ConsoleColors.ConsoleColor.ANSI_GREEN, task.description());
		}
		else {
			output.print("  ");
			output.println(task.description());
		}
	}

	private void timesCommand(String command) {
		String[] s = command.split(" ");

		if (s.length != 2) {
			output.println("Invalid command.");
			return;
		}

		long taskID;
		if (s[1].equals("active")) {
			taskID = tasks.getActiveTask().id;
		}
		else {
			taskID = Long.parseLong(s[1]);
		}

		Optional<Task> firstTask = tasks.getTasks().stream()
				.filter(task -> task.id == taskID)
				.findFirst();

		if (firstTask.isPresent()) {
			Task task = firstTask.get();

			if (task.getTimes().size() == 0) {
				output.println("No times for task " + task.description());
			}
			else {
				output.println("Times for " + task.description());
				output.println();

				long totalTime = 0;
				for (TaskTimes time : task.getTimes()) {
					output.println(time.description(tasks.osInterface.getZoneId()));

					totalTime += time.getDuration();

					if (time.stop == TaskTimes.TIME_NOT_SET) {
						totalTime += tasks.osInterface.currentSeconds() - time.start;
					}
				}

				output.println();
				output.print("Total time: ");

				long hours = totalTime / (60 * 60);
				long minutes = (totalTime - (hours * 60 * 60)) / 60;
				long seconds = (totalTime - (hours * 60 * 60) - (minutes * 60));

				output.println(String.format("%02dh %02dm %02ds", hours, minutes, seconds));
			}
		}
		else {
			output.println("Task not found.");
		}
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
		}
	}

	private void listCreateCommand(String command) {
		String[] s = command.split(" ");

		if (s.length != 2) {
			output.println("Invalid command.");
			return;
		}

		String list = s[1].toLowerCase();

		boolean added = tasks.addList(list);

		if (added) {
			output.println("Created new list \"" + list + "\"");
		}
		else {
			output.println("List \"" + list + "\" already exists.");
		}
	}

	private void listSwitchCommand(String command) {
		String[] s = command.split(" ");

		if (s.length != 2) {
			output.println("Invalid command.");
			return;
		}

		String list = s[1].toLowerCase();

		boolean exists = tasks.setCurrentList(list);

		if (exists) {
			output.println("Switched to list \"" + list + "\"");
		}
		else {
			output.println("List \"" + list + "\" does not exist.");
		}
	}

	public boolean isDebugEnabled() {
		return debugEnabled;
	}

	void execute(String command) {
		commands.keySet().stream()
				.filter(command::startsWith)
				.findFirst()
				.ifPresentOrElse(name -> commands.get(name).execute(command),
						() -> output.println("Unknown command."));
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
