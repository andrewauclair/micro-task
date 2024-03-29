// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;
import com.andrewauclair.microtask.project.Projects;
import com.andrewauclair.microtask.task.build.TaskBuilder;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public final class TaskList implements TaskContainer {
	private final String name;
	private final String fullPath;
	private final TaskGroup parent;
	private final String parentPath;

	private final OSInterface osInterface;
	private final TaskWriter writer;

	private final TaskContainerState state;

	private final String timeCategory;

	private final List<Task> tasks = new ArrayList<>();

	public TaskList(String name, TaskGroup parent, OSInterface osInterface, TaskWriter writer, TaskContainerState state, String timeCategory) {
		Objects.requireNonNull(parent);

		this.name = name;
		this.parent = parent;
		this.osInterface = osInterface;
		this.writer = writer;
		this.state = state;
		this.timeCategory = timeCategory;

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
//		return new ArrayList<>(tasks);
		return Collections.unmodifiableList(tasks);
	}

	@Override
	public Optional<TaskList> findListForTask(ExistingID id) {
		if (containsTask(id)) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	@Override
	public Optional<Task> findTask(ExistingID id) {
		return tasks.stream()
				.filter(task -> task.ID().equals(id))
				.findFirst();
	}

	@Override
	public Optional<Task> findTask(RelativeTaskID id) {
		return tasks.stream()
				.filter(task -> task.shortID().equals(id))
				.findFirst();
	}

	boolean containsTask(ExistingID taskID) {
		return tasks.stream()
				.anyMatch(task -> task.ID().equals(taskID));
	}

	@Override
	public TaskContainerState getState() {
		return state;
	}

	public String getTimeCategory() {
		if (timeCategory.isEmpty()) {
			return parent.getTimeCategory();
		}
		return timeCategory;
	}

	public TaskWriter getWriter() {
		return writer;
	}

	public TaskList rename(String name) {
		TaskList list = new TaskList(name, parent, osInterface, writer, state, timeCategory);
		list.tasks.addAll(tasks);

		return list;
	}

	TaskList changeState(TaskContainerState state) {
		TaskList list = new TaskList(name, parent, osInterface, writer, state, timeCategory);
		list.tasks.addAll(tasks);

		return list;
	}

	TaskList changeParent(TaskGroup parent) {
		TaskList list = new TaskList(name, parent, osInterface, writer, state, timeCategory);
		list.tasks.addAll(tasks);

		return list;
	}

	TaskList changeTimeCategory(String timeCategory) {
		TaskList list = new TaskList(name, parent, osInterface, writer, state, timeCategory);
		list.tasks.addAll(tasks);

		return list;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, fullPath, parentPath, tasks, osInterface, writer, state, timeCategory);
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
				Objects.equals(state, taskList.state) &&
				Objects.equals(timeCategory, taskList.timeCategory);
	}

	@Override
	public String toString() {
		return "TaskList{" +
				"name='" + name + '\'' +
				", fullPath='" + fullPath + '\'' +
				", state=" + state +
				", timeCategory='" + timeCategory + '\'' +
				", tasks=" + tasks +
				'}';
	}

	public boolean canAddTask() {
		return getState() != TaskContainerState.Finished;
	}

	public Task addTask(IDValidator idValidator, NewID id, String name) {
		if (getState() == TaskContainerState.Finished) {
			throw new TaskException("Task '" + name + "' cannot be created because list '" + getFullPath() + "' has been finished.");
		}

		long addTime = osInterface.currentSeconds();

		Task task = new TaskBuilder(idValidator, id)
				.withTask(name)
				.withState(TaskState.Inactive)
				.withAddTime(addTime)
				.withRecurring(false)
				.withDueTime(addTime + 604_800L)
				.build();

		tasks.add(task);

		writeTask(task);
		addAndCommit(task, "Added task");

		return task;
	}

	private void writeTask(Task task) {
		if (task.state == TaskState.Finished) {
			osInterface.removeFile("git-data/tasks" + getFullPath() + "/" + task.ID() + ".txt");
			writeArchive();
		}
		else {
			writer.writeTask(task, "git-data/tasks" + getFullPath() + "/" + task.ID() + ".txt");
		}
	}

	public void writeArchive() {
		List<Task> finished = tasks.stream()
				.filter(task -> task.state == TaskState.Finished)
				.collect(Collectors.toList());

		if (finished.size() > 0) {
			try (DataOutputStream outputStream = osInterface.createOutputStream("git-data/tasks" + getFullPath() + "/archive.txt")) {
				for (final Task task : finished) {
					String fileName = "git-data/tasks" + getFullPath() + "/" + task.ID() + ".txt";
					osInterface.removeFile(fileName);

					outputStream.writeBytes(fileName);
					outputStream.writeBytes("\n");
					writer.writeTask(task, outputStream);
				}
			}
			catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}
		else {
			// delete an archive.txt if it exists
			osInterface.removeFile("git-data/tasks" + getFullPath() + "/archive.txt");
		}
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
				.filter(task -> task.ID().equals(id))
				.findFirst();

		if (optionalTask.isPresent()) {
			return optionalTask.get();
		}
		throw new TaskException("Task " + id + " does not exist.");
	}

	private void replaceTask(Task oldTask, Task newTask) {
		tasks.remove(oldTask);
		tasks.add(newTask);
	}

	void removeTask(Task task) {
		tasks.remove(task);
	}

	public void addTaskNoWriteCommit(Task task) {
		tasks.add(task);
	}

	public void addTask(Task task) {
		tasks.add(task);

		writeTask(task);
		addAndCommit(task, "Added task");
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

		if (currentTask.recurring) {
			throw new TaskException("Recurring tasks cannot be finished.");
		}

		if (currentTask.state == TaskState.Finished) {
			throw new TaskException("Task " + id.get() + " has already been finished.");
		}

		Task finishedTask = new TaskBuilder(currentTask).finish(osInterface.currentSeconds());

		replaceTask(currentTask, finishedTask);

		writeArchive();
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

		tasks.remove(task);
		list.tasks.add(task);

		osInterface.removeFile("git-data/tasks" + getFullPath() + "/" + task.ID() + ".txt");

		list.writeTask(task);
		osInterface.gitCommit("Moved task " + task.description().replace("\"", "\\\"") + " to list '" + list.getFullPath() + "'");

		return task;
	}

	Task renameTask(ExistingID id, String task) {
		Task currentTask = getTask(id);

		Task renamedTask = new TaskBuilder(currentTask)
				.withTask(task)
				.build();

		replaceTask(currentTask, renamedTask);

		writeTask(renamedTask);
		addAndCommit(renamedTask, "Renamed task");

		return renamedTask;
	}
}
