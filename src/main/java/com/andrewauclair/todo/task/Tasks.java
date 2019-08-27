// Copyright (C) 2019 Andrew Auclair - All Rights Reserved
package com.andrewauclair.todo.task;

import com.andrewauclair.todo.os.OSInterface;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class Tasks {
	public static final int NO_ACTIVE_TASK = -1;

	final OSInterface osInterface;
	private final PrintStream output;
	private final TaskWriter writer;

	private final TaskGroup rootGroup = new TaskGroup("/");

	private TaskGroup activeGroup = rootGroup;
	private long activeTaskID = NO_ACTIVE_TASK;
	private String activeList = "/default";

	private long nextID;

	public Tasks(long nextID, TaskWriter writer, PrintStream output, OSInterface osInterface) {
		this.nextID = nextID;
		this.writer = writer;
		this.output = output;
		this.osInterface = osInterface;

		activeGroup.addChild(new TaskList(activeList, osInterface, writer));
	}

	public TaskWriter getWriter() {
		return writer;
	}

	public Task addTask(String task) {
		return addTask(task, activeList);
	}

	public Task addTask(String task, String list) {
		return getList(list).addTask(incrementID(), task);
	}

	private long incrementID() {
		long nextID = this.nextID++;

		try (OutputStream outputStream = osInterface.createOutputStream("git-data/next-id.txt")) {
			outputStream.write(String.valueOf(this.nextID).getBytes());
		}
		catch (IOException e) {
			e.printStackTrace(output);
		}
		osInterface.runGitCommand("git add next-id.txt");

		return nextID;
	}

	// TODO This should mostly be moved into TaskList
	// TODO I'm thinking the signature for the TaskList.java version is moveTask(Task task, TaskList list)
	public Task moveTask(long id, String list) {
		if (!hasTaskWithID(id)) {
			throw new RuntimeException("Task " + id + " was not found.");
		}

		String absoluteList = getAbsoluteListName(list);

		String listForTask = findListForTask(id);
		Optional<Task> optionalTask = getTask(id);

		Task task = optionalTask.get();

		if (!getListNames().contains(absoluteList)) {
			throw new RuntimeException("List '" + absoluteList + "' was not found.");
		}

		getList(listForTask).removeTask(task);
		getList(absoluteList).addTask(task);


		// TODO This can be replaced with moveFile
		osInterface.removeFile("git-data/tasks" + listForTask + "/" + task.id + ".txt");

		writeTask(task, absoluteList);
		osInterface.runGitCommand("git add tasks" + listForTask + "/" + task.id + ".txt");
		osInterface.runGitCommand("git add tasks" + absoluteList + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"Moved task " + task.description().replace("\"", "\\\"") + " to list '" + absoluteList + "'\"");

		return optionalTask.get();
	}

	// TODO Create this inside TaskGroup.java with signature moveList(TaskList list, TaskGroup group)
	public void moveList(String list, String group) {
		String absoluteList = getAbsoluteListName(list);

		Optional<TaskGroup> currentGroup = getGroup(groupNameFromList(absoluteList));

		Optional<TaskGroup> newGroup = getGroup(group);

		if (currentGroup.isPresent() && newGroup.isPresent()) {
			TaskList listAbsolute = getList(absoluteList);

			currentGroup.get().removeChild(listAbsolute);
			String groupPath = newGroup.get().getFullPath();

			TaskList newList = listAbsolute.rename(getFullName(groupPath, listAbsolute.getName()));
			newGroup.get().addChild(newList);

			try {
				osInterface.moveFolder(absoluteList, newList.getFullPath());
			}
			catch (IOException e) {
				e.printStackTrace(output);
				return;
			}

			osInterface.runGitCommand("git add .");
			osInterface.runGitCommand("git commit -m \"Moved list '" + absoluteList + "' to group '" + groupPath + "'\"");

			if (activeList.equals(absoluteList)) {
				activeList = newList.getFullPath();
			}
		}
		else if (!currentGroup.isPresent()) {
			throw new RuntimeException("List '" + absoluteList + "' does not exist.");
		}
		else {
			throw new RuntimeException("Group '" + group + "' does not exist.");
		}
	}

	// TODO Create this inside TaskGroup.java signature moveGroup(TaskGroup group, TaskGroup destGroup)
	public void moveGroup(String group, String destGroup) {
		Optional<TaskGroup> groupToMove = getGroup(group);
		Optional<TaskGroup> optDestGroup = getGroup(destGroup);

		if (groupToMove.isPresent() && optDestGroup.isPresent()) {
			getGroup(groupToMove.get().getParent()).get().removeChild(groupToMove.get());
			String groupPath = optDestGroup.get().getFullPath();

			TaskGroup newGroup = new TaskGroup(groupToMove.get().getName(), groupPath);
			groupToMove.get().getChildren().forEach(newGroup::addChild);

			optDestGroup.get().addChild(newGroup);

			try {
				osInterface.moveFolder(groupToMove.get().getFullPath(), newGroup.getFullPath());
			}
			catch (IOException e) {
				e.printStackTrace(output);
				return;
			}

			osInterface.runGitCommand("git add .");
			osInterface.runGitCommand("git commit -m \"Moved group '" + groupToMove.get().getFullPath() + "' to group '" + optDestGroup.get().getFullPath() + "'\"");

			if (activeGroup.getFullPath().equals(groupToMove.get().getFullPath())) {
				activeGroup = newGroup;
			}
		}
		else if (!groupToMove.isPresent()) {
			throw new RuntimeException("Group '" + group + "' does not exist.");
		}
		else {
			throw new RuntimeException("Group '" + destGroup + "' does not exist.");
		}
	}

	private TaskList getListForTask(long id) {
		if (!hasTaskWithID(id)) {
			throw new RuntimeException("Task " + id + " was not found.");
		}
		return getList(findListForTask(id));
	}

	public String findListForTask(long id) {
		return rootGroup.findListForTask(id).get();
	}

	public Task renameTask(long id, String task) {
		if (!hasTaskWithID(id)) {
			throw new RuntimeException("Task " + id + " was not found.");
		}
		return getListForTask(id).renameTask(id, task);
	}

	private void writeTask(Task task, String list) {
		writer.writeTask(task, "git-data/tasks" + list + "/" + task.id + ".txt");
	}

	private TaskList getList(String name) {
		String absoluteList = getAbsoluteListName(name);

		String groupName = groupNameFromList(absoluteList);

		Optional<TaskGroup> taskGroup = getGroup(groupName);

		if (!taskGroup.isPresent()) {
			throw new RuntimeException("Group '" + groupName + "' not found.");
		}
		return getList(taskGroup.get(), absoluteList);
	}

	public String getAbsoluteListName(String name) {
		if (!name.startsWith("/")) {
			return getFullName(activeGroup.getFullPath(), name);
		}
		return name;
	}

	public String groupNameFromList(String name) {
		String absoluteList = getAbsoluteListName(name);
		
		String groupName = absoluteList.substring(0, absoluteList.lastIndexOf('/') + 2);

		if (!groupName.equals("/")) {
			groupName = groupName.substring(0, groupName.length() - 1);
		}

		return groupName;
	}
	
	public TaskGroup createGroup(String groupName) {
		if (!groupName.startsWith("/")) {
			groupName = getFullName(activeGroup.getFullPath(), groupName);
		}
		String currentParent = "/";
		TaskGroup newGroup = null;
		for (String group : groupName.substring(1).split("/")) {
			if (group.isEmpty()) {
				continue;
			}
			Optional<TaskGroup> parentGroup = getGroup(currentParent);
			newGroup = new TaskGroup(group, currentParent);
			currentParent += group + "/";
			
			if (!parentGroup.get().containsGroup(newGroup)) {
				try {
					osInterface.createOutputStream("git-data/tasks" + newGroup.getFullPath() + "group.txt");
				}
				catch (IOException e) {
					e.printStackTrace(output);
				}
				parentGroup.get().addChild(newGroup);
			}
		}
		
		osInterface.runGitCommand("git add .");
		osInterface.runGitCommand("git commit -m \"Created group '" + groupName + "'\"");
		
		return newGroup;
	}

	private TaskList getList(TaskGroup group, String name) {
		Optional<TaskList> optionalList = group.getListAbsolute(name);

		if (!optionalList.isPresent()) {
			throw new RuntimeException("List '" + name + "' does not exist.");
		}
		return optionalList.get();
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

		if (activeTaskID == currentTask.id) {
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
		activeTaskID = currentTask.id;

		setActiveList(getActiveTaskList());

		return getList(getActiveTaskList()).startTask(activeTaskID);
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

	public boolean hasActiveTask() {
		return activeTaskID != -1;
	}

	public boolean setActiveList(String name) {
		String absoluteList = getAbsoluteListName(name);

		String groupName = groupNameFromList(absoluteList);

		Optional<TaskGroup> group = getGroup(groupName);
		if (group.isPresent()) {
			boolean exists = group.get().containsListAbsolute(absoluteList);
			if (exists) {
				activeList = absoluteList;
			}
			return exists;
		}
		return false;
	}

	public List<Task> getTasks() {
		return getList(activeGroup, activeList).getTasks();
	}

	public Task finishTask() {
		getActiveTask();
		Task task = finishTask(activeTaskID);

		activeTaskID = NO_ACTIVE_TASK;

		return task;
	}

	public Task getActiveTask() {
		if (activeTaskID == NO_ACTIVE_TASK) {
			throw new RuntimeException("No active task.");
		}
		TaskList list = getList(findListForTask(activeTaskID));

		return list.getTask(activeTaskID);
	}

	public Task finishTask(long id) {
		return getListForTask(id).finishTask(id);
	}

	public List<Task> getAllTasks() {
		return rootGroup.getTasks();
	}

	public List<Task> getTasksForList(String listName) {
		return getList(getAbsoluteListName(listName)).getTasks();
	}

	public Optional<Task> getTask(long id) {
		return getAllTasks().stream()
				.filter(task -> task.id == id)
				.findFirst();
	}

	public void renameList(String oldName, String newName) {
		String absoluteOldList = getAbsoluteListName(oldName);
		String absoluteNewList = getAbsoluteListName(newName);

		String groupName = groupNameFromList(absoluteOldList);

		Optional<TaskGroup> group = getGroup(groupName);
		if (!group.isPresent()) {
			throw new RuntimeException("Group '" + groupName + "' not found.");
		}
		TaskGroup theGroup = group.get();
		if (!theGroup.containsListAbsolute(absoluteOldList)) {
			throw new RuntimeException("List '" + absoluteOldList + "' not found.");
		}

		if (activeList.equals(absoluteOldList)) {
			activeList = absoluteNewList;
		}

		TaskList oldList = getList(theGroup, absoluteOldList);
		theGroup.removeChild(oldList);
		theGroup.addChild(oldList.rename(absoluteNewList));

		try {
			osInterface.moveFolder(absoluteOldList, absoluteNewList);
		}
		catch (IOException e) {
			e.printStackTrace(output);
			return;
		}

		osInterface.runGitCommand("git add .");
		osInterface.runGitCommand("git commit -m \"Renamed list '" + absoluteOldList + "' to '" + absoluteNewList + "'\"");
	}

	public void addTask(Task task) {
		if (hasTaskWithID(task.id)) {
			throw new RuntimeException("Task with ID " + task.id + " already exists.");
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

	public String getActiveTaskList() {
		if (activeTaskID == NO_ACTIVE_TASK) {
			throw new RuntimeException("No active task.");
		}
		return rootGroup.findListForTask(activeTaskID).orElse("");
	}

	public boolean hasListWithName(String name) {
		String absoluteList = getAbsoluteListName(name);
		String groupName = groupNameFromList(absoluteList);

		return getGroup(groupName).get().containsListAbsolute(absoluteList);
	}

	public boolean hasTaskWithID(long id) {
		for (String listName : getListNames()) {
			if (getList(listName).containsTask(id)) {
				return true;
			}
		}
		return false;
	}

	// TODO Should this stuff go in the TaskGroup class?
	public boolean addList(String name) {
		TaskGroup group;

		String absoluteList = getAbsoluteListName(name);
		String groupName = groupNameFromList(absoluteList);

		// create any groups in the path that don't exist
		createGroup(groupName);

		group = getGroup(groupName).get();

		if (group.containsListAbsolute(absoluteList)) {
			return false;
		}
		TaskList newList = new TaskList(absoluteList, osInterface, writer);

		group.addChild(newList);

		return true;
	}

	public long getActiveTaskID() {
		return activeTaskID;
	}

	public Task setRecurring(long id, boolean recurring) {
		Optional<Task> optionalTask = getTask(id);

		Task task = new TaskBuilder(optionalTask.get())
				.withRecurring(recurring)
				.build();

		String list = findListForTask(task.id);
		replaceTask(list, optionalTask.get(), task);

		writeTask(task, list);

		osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"Set recurring for task " + task.id + " to " + recurring + "\"");

		return task;
	}

	public Task setProject(long id, String project) {
		Optional<Task> optionalTask = getTask(id);

		Task task = new TaskBuilder(optionalTask.get())
				.withProject(project)
				.build();

		String list = findListForTask(task.id);
		replaceTask(list, optionalTask.get(), task);

		writeTask(task, list);

		osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"Set project for task " + task.id + " to '" + project + "'\"");

		return task;
	}

	public Task setFeature(long id, String feature) {
		Optional<Task> optionalTask = getTask(id);

		Task task = new TaskBuilder(optionalTask.get())
				.withFeature(feature)
				.build();

		String list = findListForTask(task.id);
		replaceTask(list, optionalTask.get(), task);

		writeTask(task, list);

		osInterface.runGitCommand("git add tasks" + list + "/" + task.id + ".txt");
		osInterface.runGitCommand("git commit -m \"Set feature for task " + task.id + " to '" + feature + "'\"");

		return task;
	}

	String getGroupPath() {
		return activeGroup.getFullPath();
	}

	public boolean hasGroupPath(String groupName) {
		if (!groupName.startsWith("/")) {
			throw new RuntimeException("Group path must start with root (/).");
		}
		return getGroup(groupName).isPresent();
	}

	private Optional<TaskGroup> getGroup(String name) {
		return rootGroup.getGroupAbsolute(name);
	}
	
	private String getFullName(String group, String path) {
		if (group.equals("/")) {
			return group + path;
		}
		return group + path;
	}

	public TaskGroup switchGroup(String groupName) {
		if (!groupName.startsWith("/")) {
			groupName = getFullName(activeGroup.getFullPath(), groupName);
		}
		if (!getGroup(groupName).isPresent()) {
			throw new RuntimeException("Group '" + groupName + "' does not exist.");
		}
		activeGroup = getGroup(groupName).get();

		return activeGroup;
	}

	public TaskGroup getActiveGroup() {
		return activeGroup;
	}

	public TaskGroup getRootGroup() {
		return rootGroup;
	}
}
