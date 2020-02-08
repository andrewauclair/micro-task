// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.os.OSInterface;

import java.util.*;

public final class TaskList implements TaskContainer {
	private final String name;
	private final String fullPath;
	private final TaskGroup parent;
	private final String parentPath;

	private final OSInterface osInterface;
	private final TaskWriter writer;
	
	private final String project;
	private final String feature;
	
	private final TaskContainerState state;
	
	private final List<Task> tasks = new ArrayList<>();
	
	TaskList(String name, TaskGroup parent, OSInterface osInterface, TaskWriter writer, String project, String feature, TaskContainerState state) {
		Objects.requireNonNull(parent);
		
		this.name = name;
		this.parent = parent;
//		this.name = name.substring(name.lastIndexOf('/') + 1);
		this.osInterface = osInterface;
		this.writer = writer;
		this.project = project;
		this.feature = feature;
		this.state = state;
		
		parentPath = parent.getFullPath();
		
		if (parent.getFullPath().equals("/")) {
			fullPath = "/" + name;
		}
		else {
			fullPath = parent.getFullPath() + name;
		}
	}

	public String getName() {
		return name;
	}

	@Override
	public String getFullPath() {
		return fullPath;
	}
	
	public TaskWriter getWriter() {
		return writer;
	}
	
	public TaskList rename(String name) {
		TaskList list = new TaskList(name, parent, osInterface, writer, project, feature, state);
		list.tasks.addAll(tasks);

		return list;
	}
	
	TaskList changeProject(String project) {
		TaskList list = new TaskList(name, parent, osInterface, writer, project, feature, state);
		list.tasks.addAll(tasks);

		return list;
	}
	
	TaskList changeFeature(String feature) {
		TaskList list = new TaskList(name, parent, osInterface, writer, project, feature, state);
		list.tasks.addAll(tasks);
		
		return list;
	}

	TaskList changeState(TaskContainerState state) {
		TaskList list = new TaskList(name, parent, osInterface, writer, project, feature, state);
		list.tasks.addAll(tasks);

		return list;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(name, fullPath, parentPath, tasks, osInterface, writer, project, feature, state);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TaskList taskList = (TaskList) o;
		return Objects.equals(name, taskList.name) &&
				Objects.equals(fullPath, taskList.fullPath) &&
				Objects.equals(parentPath, taskList.parentPath) &&
				Objects.equals(tasks, taskList.tasks) &&
				Objects.equals(osInterface, taskList.osInterface) &&
				Objects.equals(writer, taskList.writer) &&
				Objects.equals(project, taskList.project) &&
				Objects.equals(feature, taskList.feature) &&
				Objects.equals(state, taskList.state);
	}

	public void addTask(Task task) {
		tasks.add(task);
	}

	public Task addTask(long id, String name) {
		Task task = new Task(id, name, TaskState.Inactive, Collections.singletonList(new TaskTimes(osInterface.currentSeconds())));

		tasks.add(task);

		writeTask(task);
		addAndCommit(task, "Added task");

		return task;
	}
	
	public Task startTask(long id, long startTime, Tasks tasks) {
		Task currentTask = getTask(id);

		Task newActiveTask = new TaskBuilder(currentTask)
				.start(startTime, tasks);

		replaceTask(currentTask, newActiveTask);

		writeTask(newActiveTask);
		addAndCommit(newActiveTask, "Started task");

		return newActiveTask;
	}

	public Task stopTask(long id) {
		Task currentTask = getTask(id);

		Task stoppedTask = new TaskBuilder(currentTask).stop(osInterface.currentSeconds());

		replaceTask(currentTask, stoppedTask);

		writeTask(stoppedTask);
		addAndCommit(stoppedTask, "Stopped task");

		return stoppedTask;
	}

	public Task finishTask(long id) {
		Task currentTask = getTask(id);

		if (currentTask.isRecurring()) {
			throw new TaskException("Recurring tasks cannot be finished.");
		}
		Task finishedTask = new TaskBuilder(currentTask).finish(osInterface.currentSeconds());

		replaceTask(currentTask, finishedTask);

		writeTask(finishedTask);
		addAndCommit(finishedTask, "Finished task");

		return finishedTask;
	}

	private void replaceTask(Task oldTask, Task newTask) {
		removeTask(oldTask);
		addTask(newTask);
	}

	private void writeTask(Task task) {
		writer.writeTask(task, "git-data/tasks" + getFullPath() + "/" + task.id + ".txt");
	}

	private void addAndCommit(Task task, String comment) {
		osInterface.runGitCommand("git add tasks" + getFullPath() + "/" + task.id + ".txt", false);
		osInterface.runGitCommand("git commit -m \"" + comment + " " + task.description().replace("\"", "\\\"") + "\"", false);
	}

	void removeTask(Task task) {
		tasks.remove(task);
	}

	boolean containsTask(long taskID) {
		return tasks.stream()
				.anyMatch(task -> task.id == taskID);
	}

	@Override
	public Optional<TaskList> findListForTask(long id) {
		if (containsTask(id)) {
			return Optional.of(this);
		}
		return Optional.empty();
	}
	
	@Override
	public String toString() {
		return "TaskList{" +
				"name='" + name + '\'' +
				", fullPath='" + fullPath + '\'' +
				", tasks=" + tasks +
				", project='" + project + '\'' +
				", feature='" + feature + '\'' +
				'}';
	}
	
	@Override
	public String getProject() {
		return project;
	}
	
	@Override
	public List<Task> getTasks() {
		return Collections.unmodifiableList(tasks);
	}
	
	public Task moveTask(long id, TaskList list) {
		Task task = getTask(id);

		if (list.equals(this)) {
			throw new TaskException("Task " + id + " is already on list '" + getFullPath() + "'.");
		}

		removeTask(task);
		list.addTask(task);
		
		// TODO This can be replaced with moveFile
		osInterface.removeFile("git-data/tasks" + getFullPath() + "/" + task.id + ".txt");
		
		list.writeTask(task);
		osInterface.runGitCommand("git add tasks" + getFullPath() + "/" + task.id + ".txt", false);
		osInterface.runGitCommand("git add tasks" + list.getFullPath() + "/" + task.id + ".txt", false);
		osInterface.runGitCommand("git commit -m \"Moved task " + task.description().replace("\"", "\\\"") + " to list '" + list.getFullPath() + "'\"", false);
		
		return task;
	}

	Task renameTask(long id, String task) {
		Task currentTask = getTask(id);

		Task renamedTask = new TaskBuilder(currentTask).rename(task);

		replaceTask(currentTask, renamedTask);

		writeTask(renamedTask);
		addAndCommit(renamedTask, "Renamed task");

		return renamedTask;
	}
	
	Task getTask(long id) {
		Optional<Task> optionalTask = tasks.stream()
				.filter(task -> task.id == id)
				.findFirst();
		
		if (optionalTask.isPresent()) {
			return optionalTask.get();
		}
		throw new TaskException("Task " + id + " does not exist.");
	}
	
	@Override
	public String getFeature() {
		return feature;
	}
	
	@Override
	public TaskContainerState getState() {
		return state;
	}
}
