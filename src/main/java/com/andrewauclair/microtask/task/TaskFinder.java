// Copyright (C) 2020-2022 Andrew Auclair - All Rights Reserved
package com.andrewauclair.microtask.task;

import com.andrewauclair.microtask.TaskException;

import java.util.Optional;

public class TaskFinder {
	private final Tasks tasks;

	public TaskFinder(Tasks tasks) {
		this.tasks = tasks;
	}

//	public boolean hasTaskWithID(long id) {
//		for (String listName : tasks.getAllListNames()) {
//			if (tasks.getList(new ExistingListName(tasks, listName)).containsTask(id)) {
//				return true;
//			}
//		}
//		return false;
//	}

	// TODO Is there a way that we could create a custom stream to do stuff like this where we loop through everything?
	public TaskList findListForTask(ExistingID id) {
		Optional<TaskList> listForTask = tasks.getRootGroup().findListForTask(id);
		if (listForTask.isEmpty()) {
			throw new TaskException("List for task " + id + " was not found.");
		}
		return listForTask.get();
	}

//	public String getProjectForTask(ExistingID taskID) {
//		TaskList listForTask = findListForTask(taskID);
//
//		String project = "";//listForTask.getProject();
//
//		TaskGroup group = tasks.getGroupForList(new ExistingListName(tasks, listForTask.getFullPath()));
//
////		return projects.getProjectForList(listForTask);
////		while (project.isEmpty()) {
////			project = group.getProject();
////
////			if (group.getFullPath().equals("/")) {
////				break;
////			}
////			group = tasks.getGroup(group.getParent());
////		}
//		return project;
//	}
//
//	public String getFeatureForTask(ExistingID taskID) {
//		TaskList listForTask = findListForTask(taskID);
//
//		String feature = "";//listForTask.getFeature();
//
//		TaskGroup group = tasks.getGroupForList(new ExistingListName(tasks, listForTask.getFullPath()));
//
////		return projects.getFeatureForList(listForTask);
////		while (feature.isEmpty()) {
////			feature = group.getFeature();
////
////			if (group.getFullPath().equals("/")) {
////				break;
////			}
////			group = tasks.getGroup(group.getParent());
////		}
//		return feature;
//	}
}
