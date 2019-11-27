// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public final class TaskGroup implements TaskContainer {
	private final String name;
	private final String fullPath;
	private final TaskGroup parent;
	private final String parentPath;

	private final String project;
	private final String feature;
	
	private final List<TaskContainer> children = new ArrayList<>();

	public TaskGroup(String name) {
		this.name = name;
		fullPath = name;
		project = "";
		feature = "";
		parent = null;
		parentPath = null;
	}

	// name is relative and the parent is the absolute path of the parent
	TaskGroup(String name, TaskGroup parent, String project, String feature) {
		this.name = name;
		this.parent = parent;
		
		this.project = project;
		this.feature = feature;

		if (parent != null) {
			parentPath = parent.getFullPath();
		}
		else {
			parentPath = "";
		}

		if (parent != null) {
			if (parent.getFullPath().equals("/")) {
				fullPath = "/" + name + "/";
			}
			else {
				fullPath = parent.getFullPath() + name + "/";
			}
		}
		else {
			fullPath = "/";
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
	public int hashCode() {
		return Objects.hash(name, fullPath, parentPath, children, project, feature);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TaskGroup taskGroup = (TaskGroup) o;
		return Objects.equals(name, taskGroup.name) &&
				Objects.equals(fullPath, taskGroup.fullPath) &&
				Objects.equals(parentPath, taskGroup.parentPath) &&
				Objects.equals(children, taskGroup.children) &&
				Objects.equals(project, taskGroup.project) &&
				Objects.equals(feature, taskGroup.feature);
	}
	
	@Override
	public String toString() {
		return "TaskGroup{" +
				"name='" + name + '\'' +
				", fullPath='" + fullPath + '\'' +
				", parent=" + (parent == null ? "" : parent.getFullPath()) +
				", children=" + children +
				", project='" + project + '\'' +
				", feature='" + feature + '\'' +
				'}';
	}

	public String getParent() {
		return parent.getFullPath();
	}
	
	void addChild(TaskContainer child) {
		children.add(child);
	}
	
	void removeChild(TaskContainer child) {
		children.remove(child);
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
			if (child instanceof TaskGroup) {
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

	public List<TaskContainer> getChildren() {
		return Collections.unmodifiableList(children);
	}
	
	public TaskGroup rename(String newName) {
		TaskGroup group = new TaskGroup(newName, parent, project, feature);
		group.children.addAll(children);
		return group;
	}
	
	boolean containsGroup(TaskGroup newGroup) {
		return children.stream()
				.filter(child -> child instanceof TaskGroup)
				.map((child -> (TaskGroup) child))
				.anyMatch(group -> group.getFullPath().equals(newGroup.getFullPath()));
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
	
	TaskGroup moveGroup(TaskGroup group, TaskGroup destGroup, PrintStream output, OSInterface osInterface) {
		removeChild(group);
		
		TaskGroup newGroup = new TaskGroup(group.getName(), destGroup, group.getProject(), group.getFeature());
		group.getChildren().forEach(newGroup::addChild);
		
		destGroup.addChild(newGroup);
		
		try {
			osInterface.moveFolder(group.getFullPath(), newGroup.getFullPath());
		}
		catch (IOException e) {
			e.printStackTrace(output);
			throw new RuntimeException("Failed to move group folder.");
		}
		
		osInterface.runGitCommand("git add .", false);
		osInterface.runGitCommand("git commit -m \"Moved group '" + group.getFullPath() + "' to group '" + destGroup.getFullPath() + "'\"", false);
		
		return newGroup;
	}
	
	TaskList moveList(TaskList list, TaskGroup group, PrintStream output, OSInterface osInterface) {
		removeChild(list);
		
		TaskList newList = list.rename(group.getFullPath() + list.getName());
		
		group.addChild(newList);
		
		try {
			osInterface.moveFolder(list.getFullPath(), newList.getFullPath());
		}
		catch (IOException e) {
			e.printStackTrace(output);
			throw new RuntimeException("Failed to move list folder.");
		}
		
		osInterface.runGitCommand("git add .", false);
		osInterface.runGitCommand("git commit -m \"Moved list '" + list.getFullPath() + "' to group '" + group.getFullPath() + "'\"", false);
		
		return newList;
	}
	
	TaskGroup changeProject(String project) {
		TaskGroup group = new TaskGroup(name, parent, project, feature);
		getChildren().forEach(group::addChild);
		
		return group;
	}
	
	TaskGroup changeFeature(String feature) {
		TaskGroup group = new TaskGroup(name, parent, project, feature);
		getChildren().forEach(group::addChild);
		
		return group;
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
}
