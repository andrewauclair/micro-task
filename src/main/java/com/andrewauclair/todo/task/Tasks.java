// Copyright (C) 2019-2020 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.TaskException;
import com.andrewauclair.todo.Utils;
import com.andrewauclair.todo.command.Commands;
import com.andrewauclair.todo.os.ConsoleColors;
import com.andrewauclair.todo.os.OSInterface;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

public class Tasks {
	public static final int NO_ACTIVE_TASK = -1;
	
	final OSInterface osInterface;
	private final PrintStream output;
	private final TaskWriter writer;
	
	private TaskGroup rootGroup = new TaskGroup("/");
	
	private TaskGroup activeGroup = rootGroup;
	private long activeTaskID = NO_ACTIVE_TASK;
	private String activeList = "/default";
	
	private long nextID = 1;
	
	private TaskFilterBuilder filterBuilder = new TaskFilterBuilder();
	
	public Tasks(TaskWriter writer, PrintStream output, OSInterface osInterface) {
		this.writer = writer;
		this.output = output;
		this.osInterface = osInterface;
		
		activeGroup.addChild(new TaskList("default", rootGroup, osInterface, writer, "", "", TaskContainerState.InProgress));
	}
	
	public TaskFilterBuilder getFilterBuilder() {
		return filterBuilder;
	}
	
	public void setFilterBuilder(TaskFilterBuilder filterBuilder) {
		this.filterBuilder = filterBuilder;
	}
	
	public TaskWriter getWriter() {
		return writer;
	}
	
	public Task addTask(String task) {
		return addTask(task, activeList);
	}
	
	// TODO I'm thinking we can start removing these train wrecks by moving all the addTask testing to TaskList tests and thoroughly testing getList in the Tasks tests, then in the few real places we use this addTask method we can get the right list first and call it directly. Just not sure how to do the incrementID part.
	public Task addTask(String task, String list) {
		return getList(list).addTask(incrementID(), task);
	}
	
	private void throwTaskNotFound(long id) {
		throw new TaskException("Task " + id + " does not exist.");
	}
	
	private long incrementID() {
		long nextID = this.nextID++;
		
		try (OutputStream outputStream = osInterface.createOutputStream("git-data/next-id.txt")) {
			outputStream.write(String.valueOf(this.nextID).getBytes());
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
		osInterface.runGitCommand("git add next-id.txt", false);
		
		return nextID;
	}
	
	public long nextID() {
		return nextID;
	}
	
	public TaskList findListForTask(long id) {
		Optional<TaskList> listForTask = rootGroup.findListForTask(id);
		if (!listForTask.isPresent()) {
			throw new TaskException("List for task " + id + " was not found.");
		}
		return listForTask.get();
	}
	
	public void moveList(String list, String group) {
		TaskList currentList = getList(list);
		TaskList newList = getGroupForList(list).moveList(currentList, getGroup(group), output, osInterface);
		
		if (activeList.equals(currentList.getFullPath())) {
			activeList = newList.getFullPath();
		}
	}
	
	public void moveGroup(String group, String destGroup) {
		TaskGroup groupToMove = getGroup(group);
		
		TaskGroup newGroup = getGroup(groupToMove.getParent()).moveGroup(groupToMove, getGroup(destGroup), output, osInterface);
		
		if (activeGroup.getFullPath().equals(groupToMove.getFullPath())) {
			activeGroup = newGroup;
		}
	}
	
	public Task moveTask(long id, String list) {
		return getListForTask(id).moveTask(id, getList(list));
	}
	
	public TaskList getListForTask(long id) {
		if (!hasTaskWithID(id)) {
			throwTaskNotFound(id);
		}
		return findListForTask(id);
	}
	
	public Task renameTask(long id, String task) {
		return getListForTask(id).renameTask(id, task);
	}
	
	// TODO Should this stuff go in the TaskGroup class?
	public boolean addList(String name, boolean createFiles) {
		TaskGroup group;
		
		String absoluteList = getAbsoluteListName(name);
		String groupName = getGroupNameForList(absoluteList);
		
		// create any groups in the path that don't exist
		createGroup(groupName, createFiles);
		
		group = getGroup(groupName);
		
		if (group.containsListAbsolute(absoluteList)) {
			return false;
		}
		TaskList newList = new TaskList(absoluteList.substring(absoluteList.lastIndexOf('/') + 1), group, osInterface, writer, "", "", TaskContainerState.InProgress);
		
		group.addChild(newList);
		
		osInterface.createFolder("git-data/tasks" + newList.getFullPath());
		
		return true;
	}
	
	public boolean hasActiveTask() {
		return activeTaskID != NO_ACTIVE_TASK;
	}
	
	public Task finishTask() {
		Task task = finishTask(getActiveTask().id);
		
		activeTaskID = NO_ACTIVE_TASK;
		
		return task;
	}
	
	public List<Task> getTasksForList(String listName) {
		return getList(listName).getTasks();
	}
	
	public TaskList getListByName(String name) {
		return getList(name);
	}
	
	private TaskList getList(String name) {
		String absoluteList = getAbsoluteListName(name);
		
		return getGroupForList(absoluteList).getListAbsolute(absoluteList);
	}
	
	public Set<String> getListNames() {
		return getListNames(rootGroup);
	}
	
	private Set<String> getListNames(TaskGroup group) {
		Set<String> lists = group.getChildren().stream()
				.filter(child -> child instanceof TaskList)
				.map(TaskContainer::getFullPath)
				.collect(Collectors.toSet());
		
		group.getChildren().stream()
				.filter(child -> child instanceof TaskGroup)
				.map(child -> (TaskGroup) child)
				.forEach(nestedGroup -> lists.addAll(getListNames(nestedGroup)));
		
		return lists;
	}
	
	public Set<String> getGroupNames() {
		return getGroupNames(rootGroup);
	}
	
	private Set<String> getGroupNames(TaskGroup group) {
		Set<String> lists = group.getChildren().stream()
				.filter(child -> child instanceof TaskGroup)
				.map(TaskContainer::getFullPath)
				.collect(Collectors.toSet());
		
		group.getChildren().stream()
				.filter(child -> child instanceof TaskGroup)
				.map(child -> (TaskGroup) child)
				.forEach(nestedGroup -> lists.addAll(getGroupNames(nestedGroup)));
		
		return lists;
	}
	
	public Task startTask(long id, boolean finishActive) {
		Task currentTask = getListForTask(id).getTask(id);
		
		if (currentTask.state == TaskState.Finished) {
			throw new TaskException("Task has already been finished.");
		}
		
		if (activeTaskID == currentTask.id) {
			throw new TaskException("Task is already active.");
		}
		
		Optional<Task> lastTask = Optional.empty();
		
		if (activeTaskID != NO_ACTIVE_TASK) {
			if (finishActive) {
				lastTask = Optional.of(finishTask());
			}
			else {
				lastTask = Optional.of(stopTask());
			}
		}
		activeTaskID = currentTask.id;
		
		setActiveList(getActiveTaskList());
		switchGroup(getGroupNameForList(getActiveTaskList()));
		
		long startTime = osInterface.currentSeconds();
		
		if (lastTask.isPresent()) {
			int size = lastTask.get().getStartStopTimes().size();
			startTime = lastTask.get().getStartStopTimes().get(size - 1).stop;
		}
		
		return getList(getActiveTaskList()).startTask(activeTaskID, startTime, this);
	}
	
	public Task stopTask() {
		Task stoppedTask = getList(getActiveTaskList()).stopTask(activeTaskID);
		
		activeTaskID = NO_ACTIVE_TASK;
		return stoppedTask;
	}
	
	// TODO Move this into the TaskList class
	private void replaceTask(String listName, Task oldTask, Task newTask) {
		TaskList list = getList(listName);
		list.removeTask(oldTask);
		list.addTask(newTask);
	}

	private String getAbsoluteGroupName(String name) {
		if (!name.startsWith("/")) {
			return activeGroup.getFullPath() + name;
		}
		return name;
	}

	public String getAbsoluteListName(String name) {
		if (!name.startsWith("/")) {
			return activeGroup.getFullPath() + name;
		}
		return name;
	}
	
	public void setActiveList(String name) {
		activeList = getList(name).getFullPath();
	}
	
	public List<Task> getTasks() {
		return getList(activeList).getTasks();
	}
	
	public TaskGroup getGroupForList(String name) {
		String groupName = getGroupNameForList(name);
		
		return getGroup(groupName);
	}
	
	public String getActiveTaskList() {
		if (activeTaskID == NO_ACTIVE_TASK) {
			throw new TaskException("No active task.");
		}
		return getListForTask(activeTaskID).getFullPath();
	}
	
	public Task finishTask(long id) {
		return getListForTask(id).finishTask(id);
	}
	
	public List<Task> getAllTasks() {
		return rootGroup.getTasks();
	}
	
	private String getGroupNameForList(String name) {
		String absoluteList = getAbsoluteListName(name);
		
		String groupName = absoluteList.substring(0, absoluteList.lastIndexOf('/') + 2);
		
		if (!groupName.equals("/")) {
			groupName = groupName.substring(0, groupName.length() - 1);
		}
		return groupName;
	}
	
	public Task getActiveTask() {
		if (activeTaskID == NO_ACTIVE_TASK) {
			throw new TaskException("No active task.");
		}
		TaskList list = findListForTask(activeTaskID);
		
		return list.getTask(activeTaskID);
	}
	
	public void renameList(String oldName, String newName) {
		String absoluteOldList = getAbsoluteListName(oldName);
		String absoluteNewList = getAbsoluteListName(newName);
		
		TaskGroup group = getGroupForList(absoluteOldList);
		
		if (!group.containsListAbsolute(absoluteOldList)) {
			throw new TaskException("List '" + absoluteOldList + "' does not exist.");
		}
		
		if (activeList.equals(absoluteOldList)) {
			activeList = absoluteNewList;
		}
		
		TaskList oldList = group.getListAbsolute(absoluteOldList);
		group.removeChild(oldList);
		group.addChild(oldList.rename(newName));
		
		try {
			osInterface.moveFolder(absoluteOldList, absoluteNewList);
		}
		catch (IOException e) {
			e.printStackTrace(output);
			return;
		}
		
		osInterface.runGitCommand("git add .", false);
		osInterface.runGitCommand("git commit -m \"Renamed list '" + absoluteOldList + "' to '" + absoluteNewList + "'\"", false);
	}
	
	public void renameGroup(String oldName, String newName) {
		oldName = getAbsoluteGroupName(oldName);
		newName = getAbsoluteGroupName(newName);

		String oldFolder = oldName;
		String newFolder = newName;

		newName = newName.substring(newName.substring(0, newName.length() - 2).lastIndexOf('/') + 1);
		newName = newName.substring(0, newName.length() - 1);

		TaskGroup group = getGroup(oldName);

		boolean isActiveGroup = activeGroup.equals(group);

		TaskGroup parent = getGroup(group.getParent());
		TaskGroup newGroup = group.rename(newName);

		parent.removeChild(group);
		parent.addChild(newGroup);
		
		try {
			osInterface.moveFolder(oldFolder, newFolder);
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}

		if (isActiveGroup) {
			activeGroup = newGroup;
		}
	}
	
	public void addTask(Task task) {
		if (hasTaskWithID(task.id)) {
			throw new TaskException("Task with ID " + task.id + " already exists.");
		}
		
		getList(activeList).addTask(task);
		
		// used to set the active task when reloading from the files
		if (task.state == TaskState.Active) {
			activeTaskID = task.id;
		}
	}
	
	public String getActiveList() {
		return activeList;
	}
	
	public boolean hasListWithName(String name) {
		String absoluteList = getAbsoluteListName(name);
		
		return getGroupForList(absoluteList).containsListAbsolute(absoluteList);
	}
	
	public TaskGroup getGroup(String name) {
		name = getAbsoluteGroupName(name);
//		if (!name.startsWith("/")) {
//			name = activeGroup.getFullPath() + name;
//		}
		Optional<TaskGroup> optionalGroup = rootGroup.getGroupAbsolute(name);
		if (!optionalGroup.isPresent()) {
			throw new TaskException("Group '" + name + "' does not exist.");
		}
		return optionalGroup.get();
	}
	
	public TaskGroup addGroup(String groupName) {
		return createGroup(groupName, false);
	}
	
	private TaskGroup createGroup(String groupName, boolean createFiles) {
		if (!groupName.startsWith("/")) {
			groupName = activeGroup.getFullPath() + groupName;
		}
		String currentParent = "/";
		TaskGroup newGroup = null;
		for (String group : groupName.substring(1).split("/")) {
			if (group.isEmpty()) {
				continue;
			}
			TaskGroup parentGroup = getGroup(currentParent);
			newGroup = new TaskGroup(group, parentGroup, "", "", TaskContainerState.InProgress);
			currentParent += group + "/";
			
			if (!parentGroup.containsGroup(newGroup)) {
				if (createFiles) {
					try {
						osInterface.createOutputStream("git-data/tasks" + newGroup.getFullPath() + "group.txt");
					}
					catch (IOException e) {
						e.printStackTrace(output);
					}
				}
				parentGroup.addChild(newGroup);
			}
		}
		
		if (createFiles) {
			osInterface.runGitCommand("git add .", false);
			osInterface.runGitCommand("git commit -m \"Created group '" + groupName + "'\"", false);
		}
		
		return newGroup;
	}
	
	public TaskGroup createGroup(String groupName) {
		return createGroup(groupName, true);
	}
	
	// TODO This could be a method on the TaskContainer interface
	public boolean hasTaskWithID(long id) {
		for (String listName : getListNames()) {
			if (getList(listName).containsTask(id)) {
				return true;
			}
		}
		return false;
	}
	
	public long getActiveTaskID() {
		return activeTaskID;
	}
	
	public Task setRecurring(long id, boolean recurring) {
		Task optionalTask = getTask(id);
		
		Task task = new TaskBuilder(optionalTask)
				.withRecurring(recurring)
				.build();
		
		String list = findListForTask(task.id).getFullPath();
		replaceTask(list, optionalTask, task);
		
		writer.writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");
		
		osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt", false);
		osInterface.runGitCommand("git commit -m \"Set recurring for task " + task.id + " to " + recurring + "\"", false);
		
		return task;
	}
	
	public Task setTaskState(long id, TaskState state) {
		Task optionalTask = getTask(id);
		
		Task task = new TaskBuilder(optionalTask)
				.withState(state)
				.build();
		
		String list = findListForTask(task.id).getFullPath();
		replaceTask(list, optionalTask, task);
		
		writer.writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");
		
		osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt", false);
		osInterface.runGitCommand("git commit -m \"Set state for task " + task.id + " to " + state + "\"", false);
		
		return task;
	}
	
	public Task getTask(long id) {
		Optional<Task> optionalTask = getAllTasks().stream()
				.filter(task -> task.id == id)
				.findFirst();
		
		if (!optionalTask.isPresent()) {
			throwTaskNotFound(id);
		}
		return optionalTask.get();
	}
	
	public void setProject(TaskList list, String project, boolean createFiles) {
		String listName = list.getFullPath();
		
		TaskGroup group = getGroupForList(listName);
		
		group.removeChild(list);
		
		TaskList newList = list.changeProject(project);
		group.addChild(newList);
		
		if (createFiles) {
			writeListInfoFile(newList, "git-data");
			
			osInterface.runGitCommand("git add .", false);
			osInterface.runGitCommand("git commit -m \"Set project for list '" + listName + "' to '" + project + "'\"", false);
		}
	}
	
	public void writeListInfoFile(TaskList list, String folder) {
		try (DataOutputStream outputStream = osInterface.createOutputStream(folder + "/tasks" + list.getFullPath() + "/list.txt")) {
			outputStream.write(list.getProject().getBytes());
			outputStream.write(Utils.NL.getBytes());
			outputStream.write(list.getFeature().getBytes());
			outputStream.write(Utils.NL.getBytes());
			outputStream.write(list.getState().toString().getBytes());
			outputStream.write(Utils.NL.getBytes());
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
	}
	
	public void setProject(TaskGroup group, String project, boolean createFiles) {
		TaskGroup newGroup;
		if (group == rootGroup) {
			rootGroup = group.changeProject(project);
			newGroup = rootGroup;
		}
		else {
			TaskGroup parent = getGroup(group.getParent());
			
			parent.removeChild(group);
			
			newGroup = group.changeProject(project);
			parent.addChild(newGroup);
		}
		
		if (createFiles) {
			writeGroupInfoFile(newGroup, "git-data");
			
			osInterface.runGitCommand("git add .", false);
			osInterface.runGitCommand("git commit -m \"Set project for group '" + group.getFullPath() + "' to '" + project + "'\"", false);
		}
	}
	
	public void writeGroupInfoFile(TaskGroup group, String folder) {
		try (DataOutputStream outputStream = osInterface.createOutputStream(folder + "/tasks" + group.getFullPath() + "group.txt")) {
			outputStream.write(group.getProject().getBytes());
			outputStream.write(Utils.NL.getBytes());
			outputStream.write(group.getFeature().getBytes());
			outputStream.write(Utils.NL.getBytes());
			outputStream.write(group.getState().toString().getBytes());
			outputStream.write(Utils.NL.getBytes());
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
	}
	
	public void setFeature(TaskList list, String feature, boolean createFiles) {
		String listName = list.getFullPath();
		
		TaskGroup group = getGroupForList(listName);
		
		group.removeChild(list);
		
		TaskList newList = list.changeFeature(feature);
		group.addChild(newList);
		
		if (createFiles) {
			writeListInfoFile(newList, "git-data");
			
			osInterface.runGitCommand("git add .", false);
			osInterface.runGitCommand("git commit -m \"Set feature for list '" + listName + "' to '" + feature + "'\"", false);
		}
	}
	
	public void setFeature(TaskGroup group, String feature, boolean createFiles) {
		TaskGroup newGroup;
		if (group == rootGroup) {
			rootGroup = group.changeFeature(feature);
			newGroup = rootGroup;
		}
		else {
			TaskGroup parent = getGroup(group.getParent());
			
			parent.removeChild(group);
			
			newGroup = group.changeFeature(feature);
			parent.addChild(newGroup);
		}
		
		if (createFiles) {
			writeGroupInfoFile(newGroup, "git-data");
			
			osInterface.runGitCommand("git add .", false);
			osInterface.runGitCommand("git commit -m \"Set feature for group '" + newGroup.getFullPath() + "' to '" + feature + "'\"", false);
		}
	}
	
	public void setGroupState(TaskGroup group, TaskContainerState state, boolean createFiles) {
		TaskGroup parent = getGroup(group.getParent());
		
		parent.removeChild(group);
		
		TaskGroup taskGroup = group.changeState(state);
		
		parent.addChild(taskGroup);
	}
	
	public void setListState(TaskList list, TaskContainerState state, boolean createFiles) {
		TaskGroup parent = getGroupForList(list.getFullPath());
		
		parent.removeChild(list);
		
		TaskList newList = list.changeState(state);
		
		parent.addChild(newList);
	}
	
	public String getProjectForTask(long taskID) {
		TaskList listForTask = findListForTask(taskID);
		
		String project = listForTask.getProject();
		
		TaskGroup group = getGroupForList(listForTask.getFullPath());
		
		while (project.isEmpty()) {
			project = group.getProject();
			
			if (group.getFullPath().equals("/")) {
				break;
			}
			group = getGroup(group.getParent());
		}
		return project;
	}
	
	public String getFeatureForTask(long taskID) {
		TaskList listForTask = findListForTask(taskID);
		
		String feature = listForTask.getFeature();
		
		TaskGroup group = getGroupForList(listForTask.getFullPath());
		
		while (feature.isEmpty()) {
			feature = group.getFeature();
			
			if (group.getFullPath().equals("/")) {
				break;
			}
			group = getGroup(group.getParent());
		}
		return feature;
	}
	
	String getGroupPath() {
		return activeGroup.getFullPath();
	}
	
	public boolean hasGroupPath(String groupName) {
		if (!groupName.startsWith("/")) {
			throw new TaskException("Group path must start with root (/).");
		}
		return rootGroup.getGroupAbsolute(groupName).isPresent();
	}
	
	public TaskGroup switchGroup(String groupName) {
		activeGroup = getGroup(groupName);
		
		return activeGroup;
	}
	
	public TaskGroup getActiveGroup() {
		return activeGroup;
	}
	
	public TaskGroup getRootGroup() {
		return rootGroup;
	}
	
	public TaskList finishList(String list) {
		String absoluteName = getAbsoluteListName(list);
		
		TaskList taskList = getList(absoluteName);
		
		TaskGroup parent = getGroupForList(taskList.getFullPath());
		
		TaskList newList = taskList.changeState(TaskContainerState.Finished);
		
		parent.removeChild(taskList);
		parent.addChild(newList);
		
		writeListInfoFile(newList, "git-data");
		
		osInterface.runGitCommand("git add tasks" + newList.getFullPath() + "/list.txt", false);
		osInterface.runGitCommand("git commit -m \"Finished list '" + newList.getFullPath() + "'\"", false);
		
		return newList;
	}
	
	public TaskGroup finishGroup(String group) {
		
		TaskGroup origGroup = getGroup(group);
		TaskGroup parent = getGroup(origGroup.getParent());
		
		TaskGroup taskGroup = origGroup.changeState(TaskContainerState.Finished);
		
		parent.removeChild(origGroup);
		parent.addChild(taskGroup);
		
		writeGroupInfoFile(taskGroup, "git-data");
		
		osInterface.runGitCommand("git add tasks" + taskGroup.getFullPath() + "group.txt", false);
		osInterface.runGitCommand("git commit -m \"Finished group '" + taskGroup.getFullPath() + "'\"", false);
		
		return taskGroup;
	}
	
	public boolean load(TaskLoader loader, Commands commands) {
		rootGroup = new TaskGroup("/");
		
		try {
			nextID = getStartingID();
			loader.load();
			commands.loadAliases();
			
			Optional<Task> activeTask = getAllTasks().stream()
					.filter(task -> task.state == TaskState.Active)
					.findFirst();
			
			if (activeTask.isPresent()) {
				activeTaskID = activeTask.get().id;
				activeList = findListForTask(activeTaskID).getFullPath();
				activeGroup = getGroupForList(activeList);
			}
		}
		catch (Exception e) {
			System.out.println(ConsoleColors.ConsoleForegroundColor.ANSI_FG_RED + "Failed to read tasks." + ConsoleColors.ANSI_RESET);
			e.printStackTrace(System.out);
			
			System.out.println();
			System.out.println("Last file: " + osInterface.getLastInputFile());
			
			return false;
		}
		
		return true;
	}
	
	private long getStartingID() {
		try (InputStream inputStream = osInterface.createInputStream("git-data/next-id.txt")) {
			Scanner scanner = new Scanner(inputStream);
			return scanner.nextLong();
		}
		catch (Exception ignored) {
		}
		return 1;
	}
}
