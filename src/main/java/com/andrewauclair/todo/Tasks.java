// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;

import java.util.*;

class Tasks {
//	private final List<Task> tasks = new ArrayList<>();
	
	private final Map<String, List<Task>> tasks = new HashMap<>();
	
	private final TaskWriter writer;
	final OSInterface osInterface;
	private int startingID = 1;
	private int activeTaskID = -1;
	
	private String currentList = "default";
	
	// this constructor should only be used in the tests
	Tasks() {
		// create an empty writer
		writer = new TaskWriter(new OSInterface()) {
			@Override
			boolean writeTask(Task task, String fileName) {
				return true;
			}
		};
		osInterface = new OSInterface() {
			@Override
			public boolean runGitCommand(String command) {
				return true;
			}
		};
		tasks.put("default", new ArrayList<>());
	}

	public Tasks(TaskWriter writer, OSInterface osInterface) {
		this.writer = writer;
		this.osInterface = osInterface;
		
		tasks.put("default", new ArrayList<>());
	}

	Task addTask(String task) {
		Task newTask = new Task(startingID++, task);
		tasks.get(currentList).add(newTask);

		writeTask(newTask);

		addAndCommit(newTask, "Added task");

		return newTask;
	}

	private void writeTask(Task task) {
		writer.writeTask(task, "git-data/" + task.id + ".txt");
	}

	private void addAndCommit(Task task, String comment) {
		osInterface.runGitCommand("git add " + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"" + comment + " " + task.description().replace("\"", "\\\"") + "\"");
	}

	Task startTask(int id) {
		Optional<Task> first = tasks.get(currentList).stream()
				.filter(task -> task.id == id)
				.findFirst();

		if (first.isPresent()) {
			if (activeTaskID != -1) {
				stopTask();
			}
			activeTaskID = first.get().id;
			Task activeTask = first.get();

			Task newActiveTask = activeTask.activate(osInterface.currentSeconds());
			
			tasks.get(currentList).remove(activeTask);
			tasks.get(currentList).add(newActiveTask);

			writeTask(newActiveTask);
			addAndCommit(newActiveTask, "Started task");

			return newActiveTask;
		}
		throw new RuntimeException("com.andrewauclair.todo.Task " + id + " was not found.");
	}

	Task stopTask() {
		Task activeTask = getActiveTask();
		activeTaskID = -1;

		Task stoppedTask = activeTask.stop(osInterface.currentSeconds());
		tasks.get(currentList).remove(activeTask);
		tasks.get(currentList).add(stoppedTask);

		writeTask(stoppedTask);
		addAndCommit(stoppedTask, "Stopped task");

		return stoppedTask;
	}
	
	Task getActiveTask() {
		Optional<Task> first = tasks.get(currentList).stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();
		
		if (first.isPresent()) {
			return first.get();
		}
		throw new RuntimeException("No active task.");
	}

	Task finishTask() {
		Optional<Task> first = tasks.get(currentList).stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();

		if (first.isPresent()) {
			activeTaskID = -1;

			Task activeTask = first.get();

			Task finishedTask = activeTask.finish(osInterface.currentSeconds());
			
			tasks.get(currentList).remove(activeTask);
			tasks.get(currentList).add(finishedTask);

			writeTask(finishedTask);
			addAndCommit(finishedTask, "Finished task");

			return finishedTask;
		}
		throw new RuntimeException("No active task.");
	}

	List<Task> getTasks() {
		return tasks.get(currentList);
	}

	public void addTask(Task task) {
		tasks.get(currentList).add(task);
	}
	
	void addList(String listName) {
		tasks.put(listName, new ArrayList<>());
	}
	
	boolean hasListWithName(String listName) {
		return tasks.containsKey(listName);
	}
	
	String getCurrentList() {
		return currentList;
	}
	
	void setCurrentList(String listName) {
		currentList = listName;
	}
}
