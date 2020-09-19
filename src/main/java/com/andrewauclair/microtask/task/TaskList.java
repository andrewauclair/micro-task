// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.build.TaskBuilder;

import java.util.*;

public final class TaskList implements TaskContainer {
	private final String name;
	private final String fullPath;
	private final TaskGroup parent;
	private final String parentPath;

	private final OSInterface osInterface;
	private final TaskWriter writer;

//	private final String project;
//	private final String feature;

	private final TaskContainerState state;

	private final List<Task> tasks = new ArrayList<>();

	public TaskList(String name, TaskGroup parent, OSInterface osInterface, TaskWriter writer, TaskContainerState state) {
		Objects.requireNonNull(parent);

		this.name = name;
		this.parent = parent;
		this.osInterface = osInterface;
		this.writer = writer;
//		this.project = project;
//		this.feature = feature;
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

	@Override
	public List<Task> getTasks() {
		return Collections.unmodifiableList(tasks);
	}

	@Override
	public Optional<TaskList> findListForTask(ExistingID id) {
		if (containsTask(id.get())) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	boolean containsTask(long taskID) {
		return tasks.stream()
				.anyMatch(task -> task.id == taskID);
	}

//	@Override
//	public String getProject() {
//		return project;
//	}
//
//	@Override
//	public String getFeature() {
//		return feature;
//	}

	@Override
	public TaskContainerState getState() {
		return state;
	}

	public TaskWriter getWriter() {
		return writer;
	}

	public TaskList rename(String name) {
		TaskList list = new TaskList(name, parent, osInterface, writer, state);
		list.tasks.addAll(tasks);

		return list;
	}

//	TaskList changeProject(String project) {
//		TaskList list = new TaskList(name, parent, osInterface, writer, project, feature, state);
//		list.tasks.addAll(tasks);
//
//		return list;
//	}
//
//	TaskList changeFeature(String feature) {
//		TaskList list = new TaskList(name, parent, osInterface, writer, project, feature, state);
//		list.tasks.addAll(tasks);
//
//		return list;
//	}

	TaskList changeState(TaskContainerState state) {
		TaskList list = new TaskList(name, parent, osInterface, writer, state);
		list.tasks.addAll(tasks);

		return list;
	}

	TaskList changeParent(TaskGroup parent) {
		TaskList list = new TaskList(name, parent, osInterface, writer, state);
		list.tasks.addAll(tasks);

		return list;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, fullPath, parentPath, tasks, osInterface, writer, state);
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
//				Objects.equals(project, taskList.project) &&
//				Objects.equals(feature, taskList.feature) &&
				Objects.equals(state, taskList.state);
	}

	@Override
	public String toString() {
		return "TaskList{" +
				"name='" + name + '\'' +
				", fullPath='" + fullPath + '\'' +
				", tasks=" + tasks +
//				", project='" + project + '\'' +
//				", feature='" + feature + '\'' +
				'}';
	}

	public Task addTask(NewID id, String name) {
		if (getState() == TaskContainerState.Finished) {
			throw new TaskException("Task '" + name + "' cannot be created because list '" + getFullPath() + "' has been finished.");
		}

		Task task = new Task(id.get(), name, TaskState.Inactive, Collections.singletonList(new TaskTimes(osInterface.currentSeconds())), false, Collections.emptyList());

		tasks.add(task);

		writeTask(task);
		addAndCommit(task, "Added task");

		return task;
	}

	private void writeTask(Task task) {
		writer.writeTask(task, "git-data/tasks" + getFullPath() + "/" + task.id + ".txt");
	}

	private void addAndCommit(Task task, String comment) {
		osInterface.gitCommit(comment + " " + task.description().replace("\"", "\\\""));
	}

	public Task startTask(ExistingID id, long startTime, Tasks tasks, Projects projects) {
		Task currentTask = getTask(id);

		Task newActiveTask = new TaskBuilder(currentTask)
				.start(startTime, tasks, projects);

		replaceTask(currentTask, newActiveTask);

		writeTask(newActiveTask);
		addAndCommit(newActiveTask, "Started task");

		return newActiveTask;
	}

	public Task getTask(ExistingID id) {
		Optional<Task> optionalTask = tasks.stream()
				.filter(task -> task.id == id.get())
				.findFirst();

		if (optionalTask.isPresent()) {
			return optionalTask.get();
		}
		throw new TaskException("Task " + id + " does not exist.");
	}

	private void replaceTask(Task oldTask, Task newTask) {
		removeTask(oldTask);
		addTask(newTask);
	}

	void removeTask(Task task) {
		tasks.remove(task);
	}

	public void addTask(Task task) {
		tasks.add(task);
	}

	public Task stopTask(ExistingID id) {
		Task currentTask = getTask(id);

		Task stoppedTask = new TaskBuilder(currentTask)
				.stop(osInterface.currentSeconds());

		replaceTask(currentTask, stoppedTask);

		writeTask(stoppedTask);
		addAndCommit(stoppedTask, "Stopped task");

		return stoppedTask;
	}

	public Task finishTask(ExistingID id) {
		Task currentTask = getTask(id);

		if (currentTask.isRecurring()) {
			throw new TaskException("Recurring tasks cannot be finished.");
		}

		if (currentTask.state == TaskState.Finished) {
			throw new TaskException("Task " + id.get() + " has already been finished.");
		}

		Task finishedTask = new TaskBuilder(currentTask).finish(osInterface.currentSeconds());

		replaceTask(currentTask, finishedTask);

		writeTask(finishedTask);
		addAndCommit(finishedTask, "Finished task");

		return finishedTask;
	}

	public Task moveTask(ExistingID id, TaskList list) {
		Task task = getTask(id);

		if (list.equals(this)) {
			throw new TaskException("Task " + id.get() + " is already on list '" + getFullPath() + "'.");
		}

		if (getState() == TaskContainerState.Finished) {
			throw new TaskException("Task " + id.get() + " cannot be moved because list '" + getFullPath() + "' has been finished.");
		}
		else if (list.getState() == TaskContainerState.Finished) {
			throw new TaskException("Task " + id.get() + " cannot be moved because list '" + list.getFullPath() + "' has been finished.");
		}
		else if (task.state == TaskState.Finished) {
			throw new TaskException("Task " + id.get() + " cannot be moved because it has been finished.");
		}

		removeTask(task);
		list.addTask(task);

		osInterface.removeFile("git-data/tasks" + getFullPath() + "/" + task.id + ".txt");

		list.writeTask(task);
		osInterface.gitCommit("Moved task " + task.description().replace("\"", "\\\"") + " to list '" + list.getFullPath() + "'");

		return task;
	}

	Task renameTask(ExistingID id, String task) {
		Task currentTask = getTask(id);

		Task renamedTask = new TaskBuilder(currentTask)
				.withName(task)
				.build();

		replaceTask(currentTask, renamedTask);

		writeTask(renamedTask);
		addAndCommit(renamedTask, "Renamed task");

		return renamedTask;
	}
}
