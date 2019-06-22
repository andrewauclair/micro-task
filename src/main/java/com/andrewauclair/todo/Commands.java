// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class Commands {
	private final Tasks tasks;
	private final PrintStream output;
	private final Map<String, Command> commands = new HashMap<>();
	private final List<String> listNames = new ArrayList<>();
	
	private boolean debugEnabled = false;
	
	Commands(Tasks tasks, PrintStream output) {
		this.tasks = tasks;
		this.output = output;
		
		listNames.add("default");
		
		commands.put("finish", command -> finishCommand());
		commands.put("start", this::startCommand);
		commands.put("stop", command -> stopCommand());
		commands.put("add", this::addCommand);
		commands.put("active", command -> activeCommand());
		commands.put("list", command -> listCommand());
		commands.put("times", this::timesCommand);
		commands.put("debug", this::debugCommand);
		commands.put("create-list", this::listCreateCommand);
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
	
	private void finishCommand() {
		Task task = tasks.finishTask();
		
		output.println("Finished task " + task.description());
	}
	
	private void startCommand(String command) {
		String[] s = command.split(" ");
		int taskID = Integer.parseInt(s[1]);
		
		Task task = tasks.startTask(taskID);
		
		output.println("Started task " + task.description());
	}
	
	private void stopCommand() {
		Task task = tasks.stopTask();
		
		output.println("Stopped task " + task.description());
	}
	
	private void addCommand(String command) {
		String taskTitle = command.substring(5, command.length() - 1);
		
		Task task = tasks.addTask(taskTitle);
		
		output.println("Added task " + task.description());
	}
	
	private void activeCommand() {
		Task task = tasks.getActiveTask();
		
		output.println("Active task is " + task.description());
	}
	
	private void listCommand() {
		List<Task> tasksList = tasks.getTasks().stream()
				.filter(task -> task.state != Task.TaskState.Finished)
				.collect(Collectors.toList());
		
		Task activeTask = null;
		try {
			activeTask = tasks.getActiveTask();
		}
		catch (RuntimeException ignored) {
		}
		
		final int activeTaskID = activeTask == null ? -1 : activeTask.id;
		
		tasksList.stream()
				.limit(20)
				.forEach(task -> {
					output.print(task.id == activeTaskID ? "* " : "  ");
					output.println(task.description());
				});
		
		if (tasksList.size() > 20) {
			output.println("(" + (tasksList.size() - 20) + " more tasks.)");
		}
		
		if (tasksList.size() == 0) {
			output.println("No tasks.");
		}
	}
	
	private void timesCommand(String command) {
		String[] s = command.split(" ");
		int taskID = Integer.parseInt(s[1]);
		
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
	
	private void listCreateCommand(String command) {
		String[] s = command.split(" ");
		
		listNames.add(s[1]);
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
	
	String getListName() {
		return "default";
	}
	
	boolean hasListWithName(String listName) {
		return listNames.contains(listName);
	}
	
	private interface Command {
		void execute(String command);
	}
}
