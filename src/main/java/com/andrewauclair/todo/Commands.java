// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Commands {
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
		commands.put("list", command -> listCommand());
		commands.put("debug", this::debugCommand);
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

	private void listCommand() {
		List<Task> tasksList = tasks.getTasks().stream()
				.filter(task -> task.state != Task.TaskState.Finished)
				.collect(Collectors.toList());

		Task activeTask = new Task(-1, "");
		try {
			activeTask = tasks.getActiveTask();
		}
		catch (RuntimeException ignored) {
		}

		for (Task task : tasksList) {
			output.print(task.id == activeTask.id ? "* " : "  ");
			output.println(task.description());
		}

		if (tasksList.size() == 0) {
			output.println("No tasks.");
		}
	}

	private void activeCommand() {
		Task task = tasks.getActiveTask();

		output.println("Active task is " + task.description());
	}

	private void addCommand(String command) {
		String taskTitle = command.substring(5, command.length() - 1);

		Task task = tasks.addTask(taskTitle);

		output.println("Added task " + task.description());
	}

	private void stopCommand() {
		Task task = tasks.stopTask();

		output.println("Stopped task " + task.description());
	}

	private void startCommand(String command) {
		String[] s = command.split(" ");
		int taskID = Integer.parseInt(s[1]);

		Task task = tasks.startTask(taskID);

		output.println("Started task " + task.description());
	}

	private void finishCommand() {
		Task task = tasks.finishTask();

		output.println("Finished task " + task.description());
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

	private interface Command {
		void execute(String command);
	}
}
