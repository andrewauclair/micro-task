// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

class Tasks {
	final OSInterface osInterface;
	private final PrintStream output;
	private final Map<String, List<Task>> tasks = new HashMap<>();
	private final TaskWriter writer;
	private long startingID;
	private long activeTaskID = -1;

	private String currentList = "default";
	private String activeTaskList = currentList;

	public Tasks(long startID, TaskWriter writer, PrintStream output, OSInterface osInterface) {
		this.startingID = startID;
		this.writer = writer;
		this.output = output;
		this.osInterface = osInterface;

		tasks.put("default", new ArrayList<>());
	}

	Task addTask(String task) {
		Task newTask = new Task(startingID++, task);
		tasks.get(currentList).add(newTask);

		writeNextId();
		writeTask(newTask);

		osInterface.runGitCommand("git add next-id.txt");
		addAndCommit(newTask, "Added task");

		return newTask;
	}

	private void writeNextId() {
		try (OutputStream outputStream = osInterface.createOutputStream("git-data/next-id.txt")) {
			outputStream.write(String.valueOf(startingID).getBytes());
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
	}

	private void writeTask(Task task) {
		writer.writeTask(task, "git-data/tasks/" + currentList + "/" + task.id + ".txt");
	}

	private void addAndCommit(Task task, String comment) {
		osInterface.runGitCommand("git add tasks/" + currentList + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"" + comment + " " + task.description().replace("\"", "\\\"") + "\"");
	}

	Task startTask(long id) {
		Optional<Task> first = tasks.get(currentList).stream()
				.filter(task -> task.id == id)
				.findFirst();

		if (first.isPresent()) {
			if (activeTaskID != -1) {
				stopTask();
			}
			activeTaskList = currentList;
			activeTaskID = first.get().id;
			Task activeTask = first.get();

			Task newActiveTask = activeTask.activate(osInterface.currentSeconds());

			replaceTask(currentList, activeTask, newActiveTask);

			writeTask(newActiveTask);
			addAndCommit(newActiveTask, "Started task");

			return newActiveTask;
		}
		throw new RuntimeException("Task " + id + " was not found.");
	}

	Task stopTask() {
		Task activeTask = getActiveTask();
		activeTaskID = -1;

		Task stoppedTask = activeTask.stop(osInterface.currentSeconds());

		replaceTask(activeTaskList, activeTask, stoppedTask);

		writeTask(stoppedTask);
		addAndCommit(stoppedTask, "Stopped task");

		return stoppedTask;
	}

	Task getActiveTask() {
		Optional<Task> first = tasks.get(activeTaskList).stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();

		if (first.isPresent()) {
			return first.get();
		}
		throw new RuntimeException("No active task.");
	}

	Task finishTask() {
		Task activeTask = getActiveTask();

		activeTaskID = -1;

		Task finishedTask = activeTask.finish(osInterface.currentSeconds());

		replaceTask(activeTaskList, activeTask, finishedTask);

		writeTask(finishedTask);
		addAndCommit(finishedTask, "Finished task");

		return finishedTask;
	}

	List<Task> getTasks() {
		return Collections.unmodifiableList(tasks.get(currentList));
	}

	List<Task> getTasksForList(String listName) {
		return Collections.unmodifiableList(tasks.get(listName));
	}

	Optional<Task> getTask(int id) {
		return tasks.get(currentList).stream()
				.filter(task -> task.id == id)
				.findFirst();
	}

	Set<String> getListNames() {
		return tasks.keySet();
	}

	public void addTask(Task task) {
		tasks.get(currentList).add(task);
	}

	boolean addList(String listName) {
		if (tasks.containsKey(listName)) {
			return false;
		}
		tasks.put(listName, new ArrayList<>());
		return true;
	}

	boolean hasListWithName(String listName) {
		return tasks.containsKey(listName);
	}

	String getCurrentList() {
		return currentList;
	}

	boolean setCurrentList(String listName) {
		boolean exists = tasks.containsKey(listName);
		if (exists) {
			currentList = listName;
		}
		return exists;
	}

	private void replaceTask(String list, Task oldTask, Task newTask) {
		tasks.get(list).remove(oldTask);
		tasks.get(list).add(newTask);
	}
}
