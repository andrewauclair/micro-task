// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo;

import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;

public class Tasks {
	private static final int NO_ACTIVE_TASK = -1;
	
	// TODO Don't make this public
	public final OSInterface osInterface;
	private final PrintStream output;
	private final Map<String, List<Task>> tasks = new HashMap<>();
	private final TaskWriter writer;
	private long startingID;
	private long activeTaskID = NO_ACTIVE_TASK;
	
	private String currentList = "default";
	private String activeTaskList = "";
	
	Tasks(long startID, TaskWriter writer, PrintStream output, OSInterface osInterface) {
		this.startingID = startID;
		this.writer = writer;
		this.output = output;
		this.osInterface = osInterface;
		
		tasks.put("default", new ArrayList<>());
	}
	
	public Task addTask(String task) {
		return addTask(task, currentList);
	}

	public Task addTask(String task, String list) {
		if (!tasks.containsKey(list)) {
			throw new RuntimeException("List '" + list + "' does not exist.");
		}

		Task newTask = new Task(startingID++, task, TaskState.Inactive, Collections.singletonList(new TaskTimes(osInterface.currentSeconds())));
		tasks.get(list).add(newTask);

		writeNextId();
		writeTask(newTask, list);

		osInterface.runGitCommand("git add next-id.txt");
		addAndCommit(newTask, "Added task", list);

		return newTask;
	}

	public Task renameTask(long id, String task) {
		String listForTask = findListForTask(id);
		Optional<Task> optionalTask = getTask(id, listForTask);
		
		if (optionalTask.isPresent()) {
			Task currentTask = optionalTask.get();
			Task renamedTask = new Task(id, task, currentTask.state, currentTask.getTimes());
			
			replaceTask(listForTask, currentTask, renamedTask);

			writeTask(renamedTask, currentList);
			addAndCommit(renamedTask, "Renamed task", currentList);

			return renamedTask;
		}
		throw new RuntimeException("Task " + id + " was not found.");
	}

	public Task moveTask(long id, String list) {
		String listForTask = findListForTask(id);
		Optional<Task> optionalTask = getTask(id, listForTask);

		if (optionalTask.isPresent()) {
			Task task = optionalTask.get();

			if (!getListNames().contains(list)) {
				throw new RuntimeException("List '" + list + "' was not found.");
			}

			tasks.get(listForTask).remove(task);
			tasks.get(list).add(task);
			
			if (task.state == TaskState.Active) {
				activeTaskList = list;
			}
			
			osInterface.removeFile("git-data/tasks/" + listForTask + "/" + task.id + ".txt");

			writeTask(task, list);
			osInterface.runGitCommand("git add tasks/" + listForTask + "/" + task.id + ".txt");
			osInterface.runGitCommand("git add tasks/" + list + "/" + task.id + ".txt");
			osInterface.runGitCommand("git commit -m \"Moved task " + task.description().replace("\"", "\\\"") + " to list '" + list + "'\"");


			return optionalTask.get();
		}
		throw new RuntimeException("Task " + id + " was not found.");
	}
	
	public String findListForTask(long id) {
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
	
	private void writeNextId() {
		try (OutputStream outputStream = osInterface.createOutputStream("git-data/next-id.txt")) {
			outputStream.write(String.valueOf(startingID).getBytes());
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
	}
	
	private void writeTask(Task task, String list) {
		writer.writeTask(task, "git-data/tasks/" + list + "/" + task.id + ".txt");
	}
	
	private void addAndCommit(Task task, String comment, String list) {
		osInterface.runGitCommand("git add tasks/" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"" + comment + " " + task.description().replace("\"", "\\\"") + "\"");
	}
	
	public void renameList(String oldName, String newName) {
		if (!tasks.containsKey(oldName)) {
			throw new RuntimeException("List '" + oldName + "' not found.");
		}

		if (currentList.equals(oldName)) {
			currentList = newName;
		}

		if (activeTaskList.equals(oldName)) {
			activeTaskList = newName;
		}

		tasks.put(newName, tasks.get(oldName));
		tasks.remove(oldName);

		for (Task task : tasks.get(newName)) {
			osInterface.removeFile("git-data/tasks/" + oldName + "/" + task.id + ".txt");
			writeTask(task, newName);

			// add the deleted file and the new file
			osInterface.runGitCommand("git add tasks/" + oldName + "/" + task.id + ".txt");
			osInterface.runGitCommand("git add tasks/" + newName + "/" + task.id + ".txt");
		}

		osInterface.removeFile("git-data/tasks/" + oldName);

		osInterface.runGitCommand("git commit -m \"Renamed list '" + oldName + "' to '" + newName + "'\"");
	}
	
	private Optional<Task> getTask(long id, String list) {
		return tasks.get(list).stream()
				.filter(task -> task.id == id)
				.findFirst();
	}

	public Task startTask(long id, boolean finishActive) {
		String taskList = findListForTask(id);
		
		Optional<Task> first = tasks.get(taskList).stream()
				.filter(task -> task.id == id)
				.findFirst();
		
		if (first.isPresent()) {
			if (activeTaskID == first.get().id) {
				throw new RuntimeException("Task is already active.");
			}
			if (activeTaskID != NO_ACTIVE_TASK) {
				if (finishActive) {
					finishTask();
				}
				else {
					stopTask();
				}
			}
			activeTaskList = taskList;
			activeTaskID = first.get().id;
			Task activeTask = first.get();
			
			Task newActiveTask = new TaskBuilder(activeTask).activate(osInterface.currentSeconds());
			
			replaceTask(taskList, activeTask, newActiveTask);
			
			setCurrentList(taskList);

			writeTask(newActiveTask, currentList);
			addAndCommit(newActiveTask, "Started task", currentList);
			
			return newActiveTask;
		}
		throw new RuntimeException("Task " + id + " was not found.");
	}
	
	public Task stopTask() {
		Task activeTask = getActiveTask();
		activeTaskID = NO_ACTIVE_TASK;
		
		Task stoppedTask = new TaskBuilder(activeTask).stop(osInterface.currentSeconds());
		
		replaceTask(activeTaskList, activeTask, stoppedTask);

		writeTask(stoppedTask, activeTaskList);
		addAndCommit(stoppedTask, "Stopped task", activeTaskList);

		activeTaskList = "";

		return stoppedTask;
	}
	
	public Task getActiveTask() {
		Optional<Task> first = tasks.getOrDefault(activeTaskList, Collections.emptyList()).stream()
				.filter(task -> task.id == activeTaskID)
				.findFirst();
		
		if (first.isPresent()) {
			return first.get();
		}
		throw new RuntimeException("No active task.");
	}

	public boolean hasActiveTask() {
		return activeTaskID != -1;
	}
	
	public List<Task> getTasks() {
		return Collections.unmodifiableList(tasks.get(currentList));
	}
	
	public List<Task> getAllTasks() {
		List<Task> tasks = new ArrayList<>();
		this.tasks.values().forEach(tasks::addAll);
		return tasks;
	}
	
	public List<Task> getTasksForList(String listName) {
		return Collections.unmodifiableList(tasks.get(listName));
	}
	
	public Task finishTask() {
		Task activeTask = getActiveTask();

		activeTaskID = NO_ACTIVE_TASK;

		Task finishedTask = new TaskBuilder(activeTask).finish(osInterface.currentSeconds());
		
		replaceTask(activeTaskList, activeTask, finishedTask);
		
		writeTask(finishedTask, activeTaskList);
		addAndCommit(finishedTask, "Finished task", activeTaskList);
		
		return finishedTask;
	}
	
	public Task finishTask(long id) {
		Optional<Task> optionalTask = getTask(id);

		if (optionalTask.isPresent()) {
			Task finishedTask = new TaskBuilder(optionalTask.get()).finish(osInterface.currentSeconds());

			String taskList = findListForTask(id);

			replaceTask(taskList, optionalTask.get(), finishedTask);

			writeTask(finishedTask, taskList);
			addAndCommit(finishedTask, "Finished task", taskList);

			return finishedTask;
		}
		else {
			throw new RuntimeException("Task not found.");
		}
	}

	public void addTask(Task task) {
		String existingList = findListForTask(task.id);
		if (getTask(task.id, existingList).isPresent()) {
			throw new RuntimeException("Task with ID " + task.id + " already exists.");
		}
		
		tasks.get(currentList).add(task);
		
		if (task.state == TaskState.Active) {
			activeTaskList = currentList;
			activeTaskID = task.id;
		}
	}
	
	public Set<String> getListNames() {
		return tasks.keySet();
	}

	public Optional<Task> getTask(long id) {
		return getTask(id, currentList);
	}
	
	public boolean addList(String listName) {
		if (tasks.containsKey(listName)) {
			return false;
		}
		tasks.put(listName, new ArrayList<>());
		return true;
	}
	
	boolean hasListWithName(String listName) {
		return tasks.containsKey(listName);
	}

	public String getCurrentList() {
		return currentList;
	}
	
	public String getActiveTaskList() {
		return activeTaskList;
	}
	
	public boolean setCurrentList(String listName) {
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

	public long getActiveTaskID() {
		return activeTaskID;
	}
	
	public Task setIssue(long id, long issue) {
		Optional<Task> optionalTask = getTask(id);
		
		Task task = new TaskBuilder(optionalTask.get())
				.withIssue(issue)
				.build();
		
		String list = findListForTask(optionalTask.get().id);
		replaceTask(list, optionalTask.get(), task);
		
		writeTask(task, list);
		
		osInterface.runGitCommand("git add tasks/" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"Set issue for task " + task.id + " to " + issue + "\"");
		
		return task;
	}
	
	public Task setCharge(long id, String charge) {
		Optional<Task> optionalTask = getTask(id);
		
		Task task = new TaskBuilder(optionalTask.get())
				.withCharge(charge)
				.build();
		
		String list = findListForTask(task.id);
		replaceTask(list, optionalTask.get(), task);
		
		writeTask(task, list);
		
		osInterface.runGitCommand("git add tasks/" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"Set charge for task " + task.id + " to '" + charge + "'\"");
		
		return task;
	}
}
