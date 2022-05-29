// Copyright (C) 2019-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public final class TaskGroup implements TaskContainer {
	public static final String ROOT_PATH = "/";

	private final String name;
	private final String fullPath;
	private final TaskGroup parent;
	private final String parentPath;

	private final TaskContainerState state;
	private final String timeCategory;

	private final List<TaskContainer> children = new ArrayList<>();

	public TaskGroup(String name) {
		this.name = name;
		fullPath = name;
		parent = null;
		parentPath = null;
		state = TaskContainerState.InProgress;
		timeCategory = "";
	}

	TaskGroup(String name, TaskGroup parent, TaskContainerState state) {
		this(name, parent, state, "");
	}

	// name is relative and the parent is the absolute path of the parent
	TaskGroup(String name, TaskGroup parent, TaskContainerState state, String timeCategory) {
		this.name = name;
		this.parent = parent;

		this.state = state;
		this.timeCategory = timeCategory;

		if (parent != null) {
			parentPath = parent.getFullPath();

			if (parentPath.equals(ROOT_PATH)) {
				fullPath = ROOT_PATH + name + "/";
			}
			else {
				fullPath = parentPath + name + "/";
			}
		}
		else {
			parentPath = "";
			fullPath = ROOT_PATH;
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
		List<Task> tasks = new ArrayList<>();
		children.forEach(child -> tasks.addAll(child.getTasks()));

		return tasks;
	}

	@Override
	public Optional<TaskList> findListForTask(ExistingID id) {
		for (TaskContainer child : getChildren()) {
			Optional<TaskList> list = child.findListForTask(id);

			if (list.isPresent()) {
				return list;
			}
		}
		return Optional.empty();
	}

	public List<TaskContainer> getChildren() {
		return Collections.unmodifiableList(children);
	}

	@Override
	public TaskContainerState getState() {
		return state;
	}

	public String getTimeCategory() {
		if (parent != null && timeCategory.isEmpty()) {
			return parent.getTimeCategory();
		}
		return timeCategory;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, fullPath, parentPath, children, state, timeCategory);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		TaskGroup taskGroup = (TaskGroup) o;
		return Objects.equals(name, taskGroup.name) &&
				Objects.equals(fullPath, taskGroup.fullPath) &&
				Objects.equals(parentPath, taskGroup.parentPath) &&
				Objects.equals(children, taskGroup.children) &&
				Objects.equals(state, taskGroup.state) &&
				Objects.equals(timeCategory, taskGroup.timeCategory);
	}

	@Override
	public String toString() {
		return "TaskGroup{" +
				"name='" + name + '\'' +
				", state='" + state + '\'' +
				", timeCategory='" + timeCategory + '\'' +
				", fullPath='" + fullPath + '\'' +
				", parent='" + (parent == null ? "" : parent.getFullPath()) + '\'' +
				", children=" + children +
				'}';
	}

	public String getParent() {
		return parent.getFullPath();
	}

	public boolean containsListAbsolute(String name) {
		for (final TaskContainer child : children) {
			if (child instanceof TaskList) {
				// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
				TaskList list = (TaskList) child;

				if (list.getFullPath().equals(name)) {
					return true;
				}
			}
			else if (child instanceof TaskGroup) {
				// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
				TaskGroup group = (TaskGroup) child;

				if (group.containsListAbsolute(name)) {
					return true;
				}
			}
		}
		return false;
	}

	// finds lists by their absolute name
	TaskList getListAbsolute(String path) {
		Optional<TaskList> optionalList = children.stream()
				.filter(child -> child instanceof TaskList)
				.map(child -> (TaskList) child)
				.filter(list -> list.getFullPath().equals(path))
				.findFirst();

		if (optionalList.isEmpty()) {
			throw new TaskException("List '" + path + "' does not exist.");
		}
		return optionalList.get();
	}

	Optional<TaskGroup> getGroupAbsolute(String path) {
		for (TaskContainer child : children) {
			if (child instanceof TaskGroup) {
				// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
				TaskGroup group = (TaskGroup) child;

				Optional<TaskGroup> result = group.getGroupAbsolute(path);

				if (result.isPresent()) {
					return result;
				}
			}
		}
		if (getFullPath().equals(path)) {
			return Optional.of(this);
		}
		return Optional.empty();
	}

	public TaskGroup rename(String newName) {
		TaskGroup group = new TaskGroup(newName, parent, state, timeCategory);

		buildNewChildren(group);

		return group;
	}

	private void buildNewChildren(TaskGroup group) {
		for (TaskContainer child : children) {
			if (child instanceof TaskList) {
				// TODO Pattern matching instanceof (JDK 16+), had to remove --enable-preview for now
				TaskList list = (TaskList) child;

				group.children.add(list.changeParent(group));
			}
			else {
				TaskGroup newGroup = ((TaskGroup) child).changeParent(group);
				group.children.add(newGroup);
			}
		}
	}

	boolean containsGroup(TaskGroup newGroup) {
		return children.stream()
				.filter(child -> child instanceof TaskGroup)
				.map((child -> (TaskGroup) child))
				.anyMatch(group -> group.getFullPath().equals(newGroup.getFullPath()));
	}

	public TaskGroup moveGroup(TaskGroup group, TaskGroup destGroup, PrintStream output, OSInterface osInterface) {
		removeChild(group);

		TaskGroup newGroup = new TaskGroup(group.getName(), destGroup, group.state, group.timeCategory);
		group.getChildren().forEach(newGroup::addChild);

		destGroup.addChild(newGroup);

		try {
			osInterface.moveFolder(group.getFullPath(), newGroup.getFullPath());
		}
		catch (IOException e) {
			e.printStackTrace(output);
			throw new TaskException("Failed to move group folder.");
		}

		osInterface.gitCommit("Moved group '" + group.getFullPath() + "' to group '" + destGroup.getFullPath() + "'");

		return newGroup;
	}

	public TaskList moveList(TaskList list, TaskGroup group, PrintStream output, OSInterface osInterface) {
		removeChild(list);

		TaskList newList = new TaskList(list.getName(), group, osInterface, list.getWriter(), TaskContainerState.InProgress, list.getTimeCategory());
		list.getTasks().forEach(newList::addTask);

		group.addChild(newList);

		try {
			osInterface.moveFolder(list.getFullPath(), newList.getFullPath());
		}
		catch (IOException e) {
			e.printStackTrace(output);
			throw new TaskException("Failed to move list folder.");
		}

		osInterface.gitCommit("Moved list '" + list.getFullPath() + "' to group '" + group.getFullPath() + "'");

		return newList;
	}

	void removeChild(TaskContainer child) {
		children.remove(child);
	}

	public void addChild(TaskContainer child) {
		children.add(child);
	}

	TaskGroup changeState(TaskContainerState state) {
		TaskGroup group = new TaskGroup(name, parent, state, timeCategory);
		group.children.addAll(children);

		return group;
	}

	TaskGroup changeTimeCategory(String timeCategory) {
		TaskGroup group = new TaskGroup(name, parent, state, timeCategory);
		group.children.addAll(children);

		return group;
	}

	private TaskGroup changeParent(TaskGroup parent) {
		TaskGroup group = new TaskGroup(name, parent, state, timeCategory);

		buildNewChildren(group);

		return group;
	}
}
