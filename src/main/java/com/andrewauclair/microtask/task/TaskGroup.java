// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;
import com.andrewauclair.microtask.os.OSInterface;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public final class TaskGroup implements TaskContainer {
	private static final String ROOT_PATH = "/";

	private final String name;
	private final String fullPath;
	private final TaskGroup parent;
	private final String parentPath;

	private final String project;
	private final String feature;

	private final TaskContainerState state;

	private final List<TaskContainer> children = new ArrayList<>();

	public TaskGroup(String name) {
		this.name = name;
		fullPath = name;
		project = "";
		feature = "";
		parent = null;
		parentPath = null;
		state = TaskContainerState.InProgress;
	}

	// name is relative and the parent is the absolute path of the parent
	TaskGroup(String name, TaskGroup parent, String project, String feature, TaskContainerState state) {
		this.name = name;
		this.parent = parent;

		this.project = project;
		this.feature = feature;

		this.state = state;

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
	public Optional<TaskList> findListForTask(long id) {
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
	public String getProject() {
		if (parent != null && project.isEmpty()) {
			return parent.getProject();
		}
		return project;
	}

	@Override
	public String getFeature() {
		if (parent != null && feature.isEmpty()) {
			return parent.getFeature();
		}
		return feature;
	}

	@Override
	public TaskContainerState getState() {
		return state;
	}

	@Override
	public int hashCode() {
		return Objects.hash(name, fullPath, parentPath, children, project, feature, state);
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
				Objects.equals(project, taskGroup.project) &&
				Objects.equals(feature, taskGroup.feature) &&
				Objects.equals(state, taskGroup.state);
	}

	@Override
	public String toString() {
		return "TaskGroup{" +
				"name='" + name + '\'' +
				", fullPath='" + fullPath + '\'' +
				", parent='" + (parent == null ? "" : parent.getFullPath()) + '\'' +
				", children=" + children +
				", project='" + project + '\'' +
				", feature='" + feature + '\'' +
				'}';
	}

	public String getParent() {
		return parent.getFullPath();
	}

	boolean containsListAbsolute(String name) {
		return children.stream()
				.filter(child -> child instanceof TaskList)
				.map(child -> (TaskList) child)
				.anyMatch(list -> list.getFullPath().equals(name));
	}

	// finds lists by their absolute name
	TaskList getListAbsolute(String path) {
		Optional<TaskList> optionalList = children.stream()
				.filter(child -> child instanceof TaskList)
				.map(child -> (TaskList) child)
				.filter(list -> list.getFullPath().equals(path))
				.findFirst();

		if (!optionalList.isPresent()) {
			throw new TaskException("List '" + path + "' does not exist.");
		}
		return optionalList.get();
	}

	Optional<TaskGroup> getGroupAbsolute(String path) {
		for (TaskContainer child : children) {
			if (child instanceof TaskGroup group) {
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
		TaskGroup group = new TaskGroup(newName, parent, project, feature, state);

		buildNewChildren(group);

		return group;
	}

	private void buildNewChildren(TaskGroup group) {
		for (TaskContainer child : children) {
			if (child instanceof TaskList list) {
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

	TaskGroup moveGroup(TaskGroup group, TaskGroup destGroup, PrintStream output, OSInterface osInterface) {
		removeChild(group);

		TaskGroup newGroup = new TaskGroup(group.getName(), destGroup, group.getProject(), group.getFeature(), group.state);
		group.getChildren().forEach(newGroup::addChild);

		destGroup.addChild(newGroup);

		try {
			osInterface.moveFolder(group.getFullPath(), newGroup.getFullPath());
		}
		catch (IOException e) {
			e.printStackTrace(output);
			throw new TaskException("Failed to move group folder.");
		}

		osInterface.runGitCommand("git add .", false);
		osInterface.runGitCommand("git commit -m \"Moved group '" + group.getFullPath() + "' to group '" + destGroup.getFullPath() + "'\"", false);

		return newGroup;
	}

	TaskList moveList(TaskList list, TaskGroup group, PrintStream output, OSInterface osInterface) {
		removeChild(list);

		TaskList newList = new TaskList(list.getName(), group, osInterface, list.getWriter(), list.getProject(), list.getFeature(), TaskContainerState.InProgress);

		group.addChild(newList);

		try {
			osInterface.moveFolder(list.getFullPath(), newList.getFullPath());
		}
		catch (IOException e) {
			e.printStackTrace(output);
			throw new TaskException("Failed to move list folder.");
		}

		osInterface.runGitCommand("git add .", false);
		osInterface.runGitCommand("git commit -m \"Moved list '" + list.getFullPath() + "' to group '" + group.getFullPath() + "'\"", false);

		return newList;
	}

	void removeChild(TaskContainer child) {
		children.remove(child);
	}

	public void addChild(TaskContainer child) {
		children.add(child);
	}

	TaskGroup changeProject(String project) {
		TaskGroup group = new TaskGroup(name, parent, project, feature, state);
		group.children.addAll(children);

		return group;
	}

	TaskGroup changeFeature(String feature) {
		TaskGroup group = new TaskGroup(name, parent, project, feature, state);
		group.children.addAll(children);

		return group;
	}

	TaskGroup changeState(TaskContainerState state) {
		TaskGroup group = new TaskGroup(name, parent, project, feature, state);
		group.children.addAll(children);

		return group;
	}

	private TaskGroup changeParent(TaskGroup parent) {
		TaskGroup group = new TaskGroup(name, parent, project, feature, state);

		buildNewChildren(group);

		return group;
	}
}
