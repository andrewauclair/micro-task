// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;

import java.util.*;

public final class TaskList implements TaskContainer {
	private final String name;
	private final String fullPath;
	private final OSInterface osInterface;
	
	private final List<Task> tasks = new ArrayList<>();
	
	TaskList(String name, OSInterface osInterface) {
		fullPath = name;
		this.name = name.substring(name.lastIndexOf('/') + 1);
		this.osInterface = osInterface;
	}
	
	public String getName() {
		return name;
	}

	@Override
	public String getFullPath() {
		return fullPath;
	}

	public TaskList rename(String name) {
		TaskList list = new TaskList(name, osInterface);
		list.tasks.addAll(tasks);
		
		return list;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, fullPath, tasks, osInterface);
	}
	
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaskList taskList = (TaskList) o;
		return Objects.equals(name, taskList.name) &&
				Objects.equals(fullPath, taskList.fullPath) &&
				Objects.equals(tasks, taskList.tasks) &&
				Objects.equals(osInterface, taskList.osInterface);
	}
	
	public void addTask(Task task) {
		tasks.add(task);
	}
	
	public Task addTask(long id, String name, TaskWriter writer) {
		Task task = new Task(id, name, TaskState.Inactive, Collections.singletonList(new TaskTimes(osInterface.currentSeconds())));
		
		tasks.add(task);
		
		writeTask(task, writer);
		addAndCommit(task, "Added task");
		
		return task;
	}
	
	public Task finishTask(long id, TaskWriter writer) {
		Optional<Task> optionalTask = getTask(id);
		
		if (optionalTask.isPresent()) {
			if (optionalTask.get().isRecurring()) {
				throw new RuntimeException("Recurring tasks cannot be finished.");
			}
			Task finishedTask = new TaskBuilder(optionalTask.get()).finish(osInterface.currentSeconds());
			
			replaceTask(optionalTask.get(), finishedTask);
			
			writeTask(finishedTask, writer);
			addAndCommit(finishedTask, "Finished task");
			
			return finishedTask;
		}
		
		throw new RuntimeException("Task not found.");
	}
	
	private void replaceTask(Task oldTask, Task newTask) {
		removeTask(oldTask);
		addTask(newTask);
	}
	
	private void writeTask(Task task, TaskWriter writer) {
		writer.writeTask(task, "git-data/tasks" + getFullPath() + "/" + task.id + ".txt");
	}
	
	private void addAndCommit(Task task, String comment) {
		osInterface.runGitCommand("git add tasks" + getFullPath() + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"" + comment + " " + task.description().replace("\"", "\\\"") + "\"");
	}

	void removeTask(Task task) {
		tasks.remove(task);
	}

	boolean containsTask(long taskID) {
		return tasks.stream()
				.anyMatch(task -> task.id == taskID);
	}
	
	Optional<Task> getTask(long id) {
		return tasks.stream()
				.filter(task -> task.id == id)
				.findFirst();
	}

	public List<Task> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	Task renameTask(long id, String task, TaskWriter writer) {
		Optional<Task> optionalTask = getTask(id);
		
		if (optionalTask.isPresent()) {
			Task currentTask = optionalTask.get();
			Task renamedTask = new TaskBuilder(currentTask).rename(task);
			
			replaceTask(currentTask, renamedTask);
			
			writeTask(renamedTask, writer);
			addAndCommit(renamedTask, "Renamed task");
			
			return renamedTask;
		}
		throw new RuntimeException("Task " + id + " was not found.");
	}

	@Override
	public String toString() {
		return "TaskList{" +
				"name='" + name + '\'' +
				", fullPath='" + fullPath + '\'' +
				", tasks=" + tasks +
				'}';
	}
}
