// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

public class Tasks {
	private static final int NO_ACTIVE_TASK = -1;
	
	final OSInterface osInterface;
	private final PrintStream output;
	private final Map<String, List<Task>> tasks = new HashMap<>();
	private final TaskWriter writer;
	private long startingID;
	private long activeTaskID = NO_ACTIVE_TASK;
	
	private String currentList = "default";
	private String activeTaskList = currentList;
	
	Tasks(long startID, TaskWriter writer, PrintStream output, OSInterface osInterface) {
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
	
	Task renameTask(long id, String task) {
		String listForTask = findListForTask(id);
		Optional<Task> optionalTask = getTask(id, listForTask);
		
		if (optionalTask.isPresent()) {
			Task currentTask = optionalTask.get();
			Task renamedTask = new Task(id, task, currentTask.state, currentTask.getTimes());
			
			replaceTask(listForTask, currentTask, renamedTask);
			return renamedTask;
		}
		throw new RuntimeException("Task " + id + " was not found.");
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
	
	private String findListForTask(long id) {
		for (String key : tasks.keySet()) {
			Optional<Task> first = tasks.get(key).stream()
					.filter(task -> task.id == id)
					.findFirst();
			
			if (first.isPresent()) {
				return key;
			}
		}
		return currentList;
	}
	
	private Optional<Task> getTask(long id, String list) {
		return tasks.get(list).stream()
				.filter(task -> task.id == id)
				.findFirst();
	}
	
	Task startTask(long id) {
		String taskList = findListForTask(id);
		
		Optional<Task> first = tasks.get(taskList).stream()
				.filter(task -> task.id == id)
				.findFirst();
		
		if (first.isPresent()) {
			if (activeTaskID != NO_ACTIVE_TASK) {
				stopTask();
			}
			activeTaskList = taskList;
			activeTaskID = first.get().id;
			Task activeTask = first.get();
			
			Task newActiveTask = activeTask.activate(osInterface.currentSeconds());
			
			replaceTask(taskList, activeTask, newActiveTask);
			
			setCurrentList(taskList);
			
			writeTask(newActiveTask);
			addAndCommit(newActiveTask, "Started task");
			
			return newActiveTask;
		}
		throw new RuntimeException("Task " + id + " was not found.");
	}
	
	Task stopTask() {
		Task activeTask = getActiveTask();
		activeTaskID = NO_ACTIVE_TASK;
		
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
	
	List<Task> getTasks() {
		return Collections.unmodifiableList(tasks.get(currentList));
	}
	
	List<Task> getTasksForList(String listName) {
		return Collections.unmodifiableList(tasks.get(listName));
	}
	
	Task finishTask() {
		Task activeTask = getActiveTask();
		
		activeTaskID = NO_ACTIVE_TASK;
		
		Task finishedTask = activeTask.finish(osInterface.currentSeconds());
		
		replaceTask(activeTaskList, activeTask, finishedTask);
		
		writeTask(finishedTask);
		addAndCommit(finishedTask, "Finished task");
		
		return finishedTask;
	}
	
	public void addTask(Task task) {
		if (getTask(task.id).isPresent()) {
			throw new RuntimeException("Task with ID " + task.id + " already exists.");
		}
		
		tasks.get(currentList).add(task);
		
		if (task.state == Task.TaskState.Active) {
			activeTaskList = currentList;
			activeTaskID = task.id;
		}
	}
	
	Set<String> getListNames() {
		return tasks.keySet();
	}

	public Optional<Task> getTask(long id) {
		return getTask(id, currentList);
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
	
	String getActiveTaskList() {
		return activeTaskList;
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
	
	long getActiveTaskID() {
		return activeTaskID;
	}
}
